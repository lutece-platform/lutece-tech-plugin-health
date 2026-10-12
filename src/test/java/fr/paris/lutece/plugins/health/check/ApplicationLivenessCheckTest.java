/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.health.check;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.health.test.TestHealthCheckResponseProvider;

class ApplicationLivenessCheckTest
{
    @BeforeAll
    static void setUp( )
    {
        HealthCheckResponse.setResponseProvider( new TestHealthCheckResponseProvider( ) );
    }

    @Test
    void testName( )
    {
        assertEquals( "application-liveness", new ApplicationLivenessCheck( ).call( ).getName( ) );
    }

    @Test
    void testDownWhenWebappNotLoaded( )
    {
        // AppInit has not run in the tests: the webapp is not loaded
        HealthCheckResponse response = new ApplicationLivenessCheck( ).call( );

        assertEquals( HealthCheckResponse.Status.DOWN, response.getStatus( ) );
        assertEquals( Boolean.FALSE, response.getData( ).get( ).get( "webappLoaded" ) );
    }

    @Test
    void testDeadlockDetected( ) throws InterruptedException
    {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean( );
        int nBefore = ApplicationLivenessCheck.countDeadlockedThreads( threadMXBean );

        // Two daemon threads taking two monitors in opposite orders
        Object lockA = new Object( );
        Object lockB = new Object( );
        CountDownLatch bothHoldFirstLock = new CountDownLatch( 2 );
        startLocking( "deadlock-a", lockA, lockB, bothHoldFirstLock );
        startLocking( "deadlock-b", lockB, lockA, bothHoldFirstLock );
        bothHoldFirstLock.await( );

        int nAfter = nBefore;
        for ( int i = 0; i < 50 && nAfter < nBefore + 2; i++ )
        {
            Thread.sleep( 100 );
            nAfter = ApplicationLivenessCheck.countDeadlockedThreads( threadMXBean );
        }
        assertTrue( nAfter >= nBefore + 2, "deadlocked threads before " + nBefore + ", after " + nAfter );
        assertEquals( HealthCheckResponse.Status.DOWN, new ApplicationLivenessCheck( ).call( ).getStatus( ) );
    }

    private static void startLocking( String strName, Object first, Object second, CountDownLatch latch )
    {
        Thread thread = new Thread( ( ) -> {
            synchronized( first )
            {
                latch.countDown( );
                try
                {
                    latch.await( );
                }
                catch( InterruptedException e )
                {
                    Thread.currentThread( ).interrupt( );
                    return;
                }
                synchronized( second )
                {
                    // never reached
                    second.hashCode( );
                }
            }
        }, strName );
        thread.setDaemon( true );
        thread.start( );
    }
}
