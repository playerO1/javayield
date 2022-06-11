/*
 * License GNU GPL v3
 * (C) A.K. 2022
 */
package com.alexeyk.yieldlib.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Perfomance test. For compare with default Java iterator implementation.
 * @author A. K.
 */
public class NativeIteratorExampleTest {
    
    public NativeIteratorExampleTest() {
    }

    // --- Multi-threading safe test ---
    //todo MT test
    
    // --- Hight-Load test ---

    /**
     * Cor comparing with testLoadSimpleSequence1
     * @throws Exception 
     */
    @Test
    public void testLoadSimpleSequence1compareRaw() throws Exception {
        System.out.println("testLoadSimpleSequence1compareRaw");
        final int N=10_000;
        long time1=System.nanoTime(); // nanoTime() 10^9s or currentTimeMillis() 10^3s
        Iterator<Integer> iterator = new Iterator<>() {
            int i;
            @Override
            public boolean hasNext() {
                return i<N;
            }

            @Override
            public Integer next() {
                if (i>=N) throw new NoSuchElementException();
                return i++;
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
        System.out.println("Iterated "+N+" number time: "+ (time2-time1) +" ("+ (time2-time1)/N +" per next call)");
        // verify
        assertEquals(N, count);
        long Nx = N-1; // begin from 0
        assertEquals(Nx*(Nx+1)/2L, summ);
    }
    /*
Iterated 10000 number time: 2137918 (213 per next call)
Iterated 10000 number time: 1839081 (183 per next call)
Iterated 10000 number time: 2066664 (206 per next call)
Iterated 10000 number time: 2088816 (208 per next call)
Iterated 100000 number time: 8753237 (87 per next call)
Iterated 1000000 number time: 19190916 (19 per next call)
Iterated 10000000 number time: 112989157 (11 per next call)
Iterated 100000000 number time: 499464261 (4 per next call)
    */

}
