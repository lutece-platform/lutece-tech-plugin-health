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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.health.test.TestHealthCheckResponseProvider;

class DatabaseCheckExecutorTest
{
    @BeforeAll
    static void setUp( )
    {
        HealthCheckResponse.setResponseProvider( new TestHealthCheckResponseProvider( ) );
    }

    @Test
    void testExecutorReusedAcrossCalls( )
    {
        DatabaseReadinessCheck check = new DatabaseReadinessCheck( );
        check.init( );
        ExecutorService executor = check.getExecutor( );
        assertNotNull( executor );

        // No database in the unit tests: the checks are DOWN, but they must not create a pool per call
        for ( int i = 0; i < 5; i++ )
        {
            assertEquals( HealthCheckResponse.Status.DOWN, check.call( ).getStatus( ) );
        }
        assertSame( executor, check.getExecutor( ) );
        assertFalse( executor.isShutdown( ) );

        boolean bNamedDaemonThread = Thread.getAllStackTraces( ).keySet( ).stream( )
                .anyMatch( t -> t.getName( ).startsWith( "health-database-check-" ) && t.isDaemon( ) );
        assertTrue( bNamedDaemonThread, "no daemon thread named health-database-check-*" );

        check.shutdown( );
        assertTrue( executor.isShutdown( ) );
    }

    @Test
    void testDownAfterShutdown( )
    {
        DatabaseReadinessCheck check = new DatabaseReadinessCheck( );
        check.init( );
        check.shutdown( );

        HealthCheckResponse response = check.call( );
        assertEquals( HealthCheckResponse.Status.DOWN, response.getStatus( ) );
        assertEquals( "Health check stopped", response.getData( ).get( ).get( "error" ) );
    }
}
