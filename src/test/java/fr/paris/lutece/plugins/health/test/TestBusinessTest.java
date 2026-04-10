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
package fr.paris.lutece.plugins.health.test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.health.check.WebappStartupCheck;
import fr.paris.lutece.plugins.health.test.TestHealthCheckResponseProvider;

class TestBusinessTest
{
    @BeforeAll
    static void setUp( )
    {
        // Register a test SPI provider since OpenLiberty runtime is not available in tests
        HealthCheckResponse.setResponseProvider( new TestHealthCheckResponseProvider( ) );
    }

    @Test
    void testWebappStartupCheckName( )
    {
        WebappStartupCheck check = new WebappStartupCheck( );
        HealthCheckResponse response = check.call( );

        assertNotNull( response );
        assertEquals( "webappSuccessfullyLoaded-startup", response.getName( ) );
    }

    @Test
    void testWebappStartupCheckReturnsDown_WhenNotInitialized( )
    {
        WebappStartupCheck check = new WebappStartupCheck( );
        HealthCheckResponse response = check.call( );

        assertEquals( HealthCheckResponse.Status.DOWN, response.getStatus( ) );
    }

    @Test
    void testWebappStartupCheckDoesNotThrow( )
    {
        WebappStartupCheck check = new WebappStartupCheck( );

        // Should never throw, even when AppInit has not been called
        HealthCheckResponse response = check.call( );
        assertNotNull( response );
        assertTrue( response.getName( ).length( ) > 0 );
    }
}
