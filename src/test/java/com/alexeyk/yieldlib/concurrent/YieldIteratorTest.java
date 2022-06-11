/*
 * License GNU GPL v3
 * (C) A.K. 2022
 */
package com.alexeyk.yieldlib.concurrent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for YieldIterator
 * @author A.K.
 */
public class YieldIteratorTest {
    
    public YieldIteratorTest() {
    }

    // --- Simple iteration test ---

    /**
     * Test of generator method, check simple sequence generated
     */
    @Test
    public void testSimpleSequence() throws Exception {
        System.out.println("testSimpleSequence");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // test implementation
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        while (iterator.hasNext()) {
            Integer i=iterator.next();
            actuals.add(i);
        }
        // verify
        assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9), actuals);
    }

    /**
     * Test of many call of hasNext
     */
    @Test
    public void testManyHasNextCall() throws Exception {
        System.out.println("testManyHasNextCall");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // test implementation
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            Integer i=iterator.next();
            actuals.add(i);
        }
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        // verify
        assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9), actuals);
    }

    /**
     * Test of many call of next() over element limit
     */
    @Test
    public void testNoSuchElement() throws Exception {
        System.out.println("testNoSuchElement");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // test implementation
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            Integer i=iterator.next();
            actuals.add(i);
        }
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        // verify
        assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9), actuals);
        // Not enought element
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        assertFalse(iterator.hasNext());
    }
    
    /**
     * Iterated without hasNext. Way 1.
     * @throws Exception 
     */
    @Test
    public void testSimpleSequenceWithoutHasNext1() throws Exception {
        System.out.println("testSimpleSequenceWithoutHasNext1");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // test implementation
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        for (int i=0;i<10;i++) {
            Integer x=iterator.next();
            actuals.add(x);
        }
        // verify
        assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9), actuals);
        assertFalse(iterator.hasNext());
    }

    /**
     * Iterated without hasNext. Way 2.
     * @throws Exception 
     */
    @Test
    public void testSimpleSequenceWithoutHasNext2() throws Exception {
        System.out.println("testSimpleSequenceWithoutHasNext2");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<10;i++) yield(i); // test implementation
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        for (int i=0;i<10;i++) {
            Integer x=iterator.next();
            actuals.add(x);
        }
        // verify
        assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9), actuals);
        // Not enought element
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        assertFalse(iterator.hasNext());
    }

    /**
     * Test of empty 2
     */
    @Test
    public void testEmptySequence1() throws Exception {
        System.out.println("testEmptySequence1");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                // none
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        assertFalse(iterator.hasNext());
    }
    /**
     * Test of empty 2
     */
    @Test
    public void testEmptySequence2() throws Exception {
        System.out.println("testEmptySequence2");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                // none
            }
        };
        LinkedList<Integer> actuals = new LinkedList();
        // Collect iterated data
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        assertFalse(iterator.hasNext());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop. Warning: it is 100% stop now?
    }
    
    
    // --- Unexpected throwable test ---

    /**
     * Expected exception on last iteration
     */
    @Test
    public void testSimpleSequenceAndThrow1() throws Exception {
        System.out.println("testSimpleSequenceAndThrow1");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                yield(1);
                yield(2);
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        // Collect iterated data
        assertTrue(iterator.hasNext());
        assertEquals((Integer)1,iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals((Integer)2,iterator.next());
        // verify expected exception
        assertEquals("Ups, some throuble happend.", assertThrows(RuntimeException.class, ()-> iterator.next()).getMessage());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop.
    }
    
    /**
     * Expected exception on last iteration
     */
    @Test
    public void testSimpleSequenceAndThrow2() throws Exception {
        System.out.println("testSimpleSequenceAndThrow2");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                yield(1);
                yield(2);
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        // Collect iterated data
        assertTrue(iterator.hasNext());
        assertEquals((Integer)1,iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals((Integer)2,iterator.next());
        // verify expected exception
        assertTrue(iterator.hasNext()); // should not throw
        assertEquals("Ups, some throuble happend.", assertThrows(RuntimeException.class, ()-> iterator.next()).getMessage());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop.
    }
    
        
    /**
     * Expected exception on first iteration
     */
    @Test
    public void testSimpleSequenceAndThrow3() throws Exception {
        System.out.println("testSimpleSequenceAndThrow3");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        // verify expected exception
        assertEquals("Ups, some throuble happend.", assertThrows(RuntimeException.class, ()-> iterator.next()).getMessage());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, ()-> iterator.next());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop.
    }
    
    /**
     * Expected exception on first iteration
     */
    @Test
    public void testSimpleSequenceAndThrow4() throws Exception {
        System.out.println("testSimpleSequenceAndThrow4");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        // verify expected exception
        assertTrue(iterator.hasNext()); // should not throw
        assertEquals("Ups, some throuble happend.", assertThrows(RuntimeException.class, ()-> iterator.next()).getMessage());

        assertFalse(iterator.hasNext());
        assertThrows(RuntimeException.class, ()-> iterator.next());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop.
    }
    
    @Test
    public void testClose1() throws Exception {
        System.out.println("testClose1");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                yield(1);
                yield(2);
                yield(3);
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        // Collect notfull iterated data
        assertTrue(iterator.hasNext());
        // close thread
        iterator.close();
        Thread.sleep(100);// for >99,9%
        //assertTrue(iterator.hasNext()); // can be take first element
        
        assertFalse(iterator.hasNext());
        Thread.sleep(100);// for >99,9%
        assertFalse(iterator.generator.isAlive()); // verify implementation stop.
    }
    
    /*@Test
    public void testClose2() throws Exception {
        System.out.println("testClose2");
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                yield(1);
                yield(2);
                yield(3);
                throw new RuntimeException("Ups, some throuble happend.");
            }
        };
        //Thread internalThread = iterator.generator;
        iterator = null;
        String megaSTR="MEGA LARGE TEXT FOR MEMORY lalalalal ";
        for(int i=0;i<15;i++) {
            System.out.println("Try wait finalizer..."+i);
            System.gc(); // try call finalize. 50%/50%
            Thread.sleep(100);
            megaSTR+=megaSTR;
        }
        //assertFalse(internalThread.isAlive()); // verify implementation stop.
    }*/
    
    // --- Multi-threading safe test ---
    //todo MT test
    
    // --- Hight-Load test ---
    @Test
    public void testLoadSimpleSequence1() throws Exception {
        System.out.println("testLoadSimpleSequence1");
        final int N=10_000;
        long time1=System.nanoTime(); // nanoTime() 10^9s or currentTimeMillis() 10^3s
        YieldIterator<Integer> iterator = new YieldIterator<>() {
            @Override
            protected void generator() throws InterruptedException {
                for (int i=0;i<N;i++) yield(i); // test implementation
            }
        };
        int count=0;
        long summ=0;
        // Collect iterated data
        while (iterator.hasNext()) {
            count++;
            summ+=iterator.next();
        }
        long time2=System.nanoTime();
        System.out.println("Iterated "+N+" number time: "+ (time2-time1) +" ("+ (time2-time1)/N +" per yield call)");
        // verify
        assertEquals(N, count);
        long Nx = N-1; // begin from 0
        assertEquals(Nx*(Nx+1)/2L, summ);
    }
    /*
testLoadSimpleSequence1
Iterated 10000 number time: 117715301 (11771 per yield call)
Iterated 10000 number time: 116538417 (11653 per yield call)
Iterated 10000 number time: 89240038 (8924 per yield call)
Iterated 10000 number time: 108075861 (10807 per yield call)
Iterated 100000 number time: 618512420 (6185 per yield call)
Iterated 1000000 number time: 6350485816 (6350 per yield call)
Iterated 10000000 number time: 63510912999 (6351 per yield call)
    */

}
