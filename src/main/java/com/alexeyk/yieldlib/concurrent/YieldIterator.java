/*
 * License GNU GPL v3
 * (C) A.K. 2022
 */
package com.alexeyk.yieldlib.concurrent;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * This abstract class provide Iterator with python-like yield style implementation.
 * 
 * This is not lightweight object as "yield" on python.
 * It using thread for management "yield". 
 * Implementation via wait()/notify()
 * Perfomance: if you'r generator call yield() for simple i++ it can be slow ower 500x that you implement native Iterator. But if it was highweight operation (over 100 ms) it can be increase perfomance by execute in separate thread.
 * 
 * Operation hasNext() and next() is thread-safe, but not botch - it is 2 atomic operation. 
 * You can be take hasNext()==true but another threadcan take next() before first thread take.
 * 
 * Implementation should override <code>generator()</code> when call many times method yield(T).
 * By defauld internal thread start in the constructor by call protected start().You can override start() and call generator.start() later.
 * ToDo it is bad practicle start thread into constructor.
 * 
 * todo name: YieldIterator or YieldGenerator?
 * 
 * (C) A.K. 2022
 * @author A.K.
 */
public abstract class YieldIterator<T> implements Iterator<T>, Closeable {
    protected static final Object STOP_OBJECT = new Object();
    protected final Object WAIT_OBJECT = new Object(); // sync object
    protected final Thread generator;
    protected volatile Throwable nextErr; // Error or RuntimeException
    protected volatile Object yieldWindow;

    public YieldIterator() {
        yieldWindow = WAIT_OBJECT;
        String threadName="iterator-generator-"+getClass().getSimpleName();  // fixme Can not see class name on thread list. Why?
        generator = new Thread(threadName) {
            @Override
            public void run() {
                try {
                    YieldIterator.this.generator();
                    synchronized (WAIT_OBJECT) {
                        while (yieldWindow!=WAIT_OBJECT) WAIT_OBJECT.wait();
                        YieldIterator.this.yieldWindow=STOP_OBJECT;
                        WAIT_OBJECT.notifyAll();
                    }
                } catch (InterruptedException ei) {
                    // ignore interrupt - it is raw thread body.
                    yieldWindow = STOP_OBJECT;
                    // can be data lost of previous wyieldWindow!
                } catch (Throwable e) {
                    synchronized (WAIT_OBJECT) {
                        while (yieldWindow!=WAIT_OBJECT) try {
                            WAIT_OBJECT.wait();
                        } catch (InterruptedException ei) {
                            // ignore interrupt - it is raw thread body on exit status.
                        }
                        nextErr = e;
                        YieldIterator.this.yieldWindow=STOP_OBJECT;
                        WAIT_OBJECT.notifyAll();
                    }
                }
            }
        };
        generator.setDaemon(true);
        start();//todo when should start? Not in constructor.
    }
    
    protected void start() {
        generator.start();
    }
    
    /**
     * Generator for put yield(X).
     * @throws InterruptedException 
     */
    protected abstract void generator() throws InterruptedException;
    
    protected void yield(T item) throws InterruptedException {
        synchronized (WAIT_OBJECT) {
            if (yieldWindow==STOP_OBJECT) throw new IllegalStateException("Iterator closed");
            if (yieldWindow!=WAIT_OBJECT) {
                // wait till other thread take
                WAIT_OBJECT.wait();
                if (!(yieldWindow==WAIT_OBJECT || yieldWindow==STOP_OBJECT)) {
                    throw new IllegalStateException("Exchange error - yieldWindow is not empty");// never
                }
            }
            yieldWindow = item;
            WAIT_OBJECT.notify();
        }
    }

    
    @Override
    public boolean hasNext() {
        synchronized (WAIT_OBJECT) {
            try {
                while (yieldWindow==WAIT_OBJECT) WAIT_OBJECT.wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Can not wait iterator source", ex);
            }
            if (yieldWindow==STOP_OBJECT && nextErr==null) return false;
            return true;
        }
    }

    /**
     * 
     * @throws NoSuchElementException when no more element
     * @throws RuntimeException with InterruptedException if thread was interrupt. Interrupt flag has restored correctly.
     * @throws Error or RuntimeException - any exception from generator
     * @return next generated object
     */
    @Override
    public T next() {
        synchronized (WAIT_OBJECT) {
            while (yieldWindow==WAIT_OBJECT) {
                try {
                    WAIT_OBJECT.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Can not wait iterator source", ex);
                }
            }
            if (yieldWindow==STOP_OBJECT) {
                if (nextErr!=null) { // throw exception from generator
                    Throwable doThrow = nextErr;
                    nextErr = null; // throw once
                    if (doThrow instanceof RuntimeException) throw (RuntimeException) doThrow;
                    if (doThrow instanceof Error) throw (Error) doThrow;
                    throw new RuntimeException("Iteratorthread stop with error", doThrow); // never
                } else
                    throw new NoSuchElementException();
            }
            T take = (T)yieldWindow;
            yieldWindow=WAIT_OBJECT;
            WAIT_OBJECT.notifyAll();
            return take;
        }
    }
    
    /**
     * Ensure close thread.
     * Last element from next() will be removed ater call close().
     */
    @Override
    public void close() {
        if (generator.isAlive()) {
            generator.interrupt();
            //System.out.print(getClass().getSimpleName()+" thead success closing!"); // debug
        }
    }
    
    /**
     * Warning: can be not work. See java https://openjdk.java.net/jeps/421
     * Recomended execute <code>close()</code> or reaadall sequence from iterator.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable{
        close();
        super.finalize();
    }
}
