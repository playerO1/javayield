# javayield
Python-style yield iterator (generator) for Java

Do you see python yield for return part of function work, like as generator? Now you can do this on Java with my little class.

## Example source:

        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // generate objects
            }
        };
        
        // Collect iterated data
        while (iterator.hasNext()) {
            Integer i=iterator.next();
            System.out.print(" "+i);
        }

See javadoc in classes.
'YieldIterator' using thread for support return part of work. 

You should be ensure close it. By default, internal thread close after finish 'generator()' and all iterator.next() called.
Butstrongly recomendation call 'close()' for ensure that thread will be close.

Implementation
==============
All implementation is thread safe for read (as any Iterator).
Inner thread will be closed automaticly after generator() has finished and all item will be read by iterator.next().
For force close inner thread use 'close()' method.
Measure of perfomance see in unit-test.

YieldIterator.java
------------------
Based on synchronization via Object.wait() and Object.notify().
Thread safe for externalcode call 'hasNext()' and 'next()'
Not thread safe for call 'yield(T)' from 'generator()'

Perfomance: 6351-11771 nanosecond per 'yield()' exchange

YieldIteratorQueueImpl.java
---------------------------
Based on BlockingQueue. Default is unfair queue with capacity=2.
Thread safe for externalcode call 'hasNext()' and 'next()'.
Thread safe for call 'yield(T)' from 'generator()'.

Perfomance: 2633-6449 nanosecond per 'yield()' exchange
Can be spam CPU context switch ('System.yield()') when 'generator()' thread slowly that 'iterator.next()' thread.

Native Iterator implementation
------------------------------
Perfomance: 4-213 nanosecond per 'yield(T)' exchange

