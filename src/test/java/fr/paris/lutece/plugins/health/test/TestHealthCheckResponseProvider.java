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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;

/**
 * Minimal HealthCheckResponseProvider for unit tests.
 * Replaces the OpenLiberty runtime SPI that is not available in the test classpath.
 */
public class TestHealthCheckResponseProvider implements HealthCheckResponseProvider
{
    @Override
    public HealthCheckResponseBuilder createResponseBuilder( )
    {
        return new TestHealthCheckResponseBuilder( );
    }

    private static class TestHealthCheckResponseBuilder extends HealthCheckResponseBuilder
    {
        private String _strName;
        private HealthCheckResponse.Status _status = HealthCheckResponse.Status.DOWN;
        private final Map<String, Object> _data = new LinkedHashMap<>( );

        @Override
        public HealthCheckResponseBuilder name( String name )
        {
            _strName = name;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData( String key, String value )
        {
            _data.put( key, value );
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData( String key, long value )
        {
            _data.put( key, value );
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData( String key, boolean value )
        {
            _data.put( key, value );
            return this;
        }

        @Override
        public HealthCheckResponseBuilder up( )
        {
            _status = HealthCheckResponse.Status.UP;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder down( )
        {
            _status = HealthCheckResponse.Status.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder status( boolean up )
        {
            _status = up ? HealthCheckResponse.Status.UP : HealthCheckResponse.Status.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponse build( )
        {
            return new HealthCheckResponse( _strName, _status, Optional.of( _data ) );
        }
    }
}
