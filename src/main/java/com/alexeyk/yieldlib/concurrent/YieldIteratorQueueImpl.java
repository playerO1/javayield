/*
 * License GNU GPL v3
 * (C) A.K. 2022
 */
package com.alexeyk.yieldlib.concurrent;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * This abstract class provide Iterator with python-like yield style implementation.
 * 
 * This is not lightweight object as "yield" on python.
 * It using thread for management "yield". 
 * Implementation via blocking queue (default capacity=2 fair=false it for increase perfomance, but can generate more objects that you take from iterator)
 * Perfomance: if you'r generator call yield() for simple i++ it can be slow ower 32x that you implement native Iterator. But if it was highweight operation (over 10 ms) it can be increase perfomance by execute in separate thread.
 * 
 * Operation hasNext() and next() is thread-safe, but not botch - it is 2 atomic operation. 
 * You can be take hasNext()==true but another threadcan take next() before first thread take.
 * 
 * Implementation should override <code>generator()</code> when call many times method yield(T).
 * By defauld internal thread start in the constructor by call protected start().You can override start() and call generator.start() later.
 * ToDo it is bad practicle start thread into constructor.
 * 
 * 
 * (C) A.K. 2022
 * @author A.K.
 */
public abstract class YieldIteratorQueueImpl<T> implements Iterator<T>, Closeable {
    protected static final Object STOP_OBJECT = new Object();
    protected final Thread generator;
    protected volatile Throwable nextErr; // Error or RuntimeException
    protected volatile BlockingQueue yieldWindow;

    public YieldIteratorQueueImpl() {
        // // ArrayBlockingQueue, LinkedBlockingDequeue, LinkedTransferQueue
        this(new ArrayBlockingQueue(2, false));
        // fair = true decrease perfomance down to /3 but you will not work with multiple thread read iterator, isn't it?
    }
    public YieldIteratorQueueImpl(BlockingQueue withQueue) {
        Objects.nonNull(withQueue);
        yieldWindow = withQueue;
        String threadName="iterator-generator-"+getClass().getSimpleName();  // fixme Can not see class name on thread list. Why?
        generator = new Thread(threadName) {
            @Override
            public void run() {
                try {//todo refactor try-catch block
                    YieldIteratorQueueImpl.this.generator();
                    yieldWindow.put(STOP_OBJECT);
                } catch (InterruptedException ei) {
                    try {
                        // ignore interrupt - it is raw thread body.
                        if (yieldWindow.remainingCapacity()<1) yieldWindow.clear();
                        yieldWindow.put(STOP_OBJECT);
                        // can be data lost of previous wyieldWindow!
                    } catch (InterruptedException ei2) {
                        System.err.print(getClass().getName() + ": Error put stop object to queue - interrup two. External thread can deadlock!");
                    }
                } catch (Throwable e) {
                    nextErr = e;
                    try {
                        yieldWindow.put(STOP_OBJECT);
                    } catch (InterruptedException ei) {
                        if (yieldWindow.remainingCapacity()<1) yieldWindow.clear();
                        try {
                          yieldWindow.put(STOP_OBJECT);
                        } catch (InterruptedException ei2) {
                            System.err.print(getClass().getName() + ": Error put stop object to queue - interrup two. External thread can deadlock!");
                        }
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
        //todo check it closed status?
        yieldWindow.put(item);
    }

    
    @Override
    public boolean hasNext() {
        if (yieldWindow.isEmpty()) {
            while (yieldWindow.size()==0) {
                Thread.yield();
                /*try {
                Thread.sleep(10L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Cannot wait queue data", ie);
                }*/
                //todo wait till data more correct. todo yse bidirection wueue + take()?
            }
        }
        Object inQueue = yieldWindow.peek(); // todo it throw any exception?
        return (inQueue != STOP_OBJECT || nextErr != null);
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
        Object inQueue;
        try {
            inQueue = yieldWindow.take();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted.", ie);
        }
        if (inQueue==STOP_OBJECT) {
            if (!yieldWindow.add(inQueue)) { // return stop marker
                System.err.printf(getClass().getName() + ": Can not return stop object to queue.");
            } 
            if (nextErr!=null) { // throw exception from generator
                Throwable doThrow = nextErr;
                nextErr = null; // throw once
                if (doThrow instanceof RuntimeException) throw (RuntimeException) doThrow;
                if (doThrow instanceof Error) throw (Error) doThrow;
                throw new RuntimeException("Iteratorthread stop with error", doThrow); // never
            } else
                throw new NoSuchElementException();
        }
        return (T) inQueue;
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
