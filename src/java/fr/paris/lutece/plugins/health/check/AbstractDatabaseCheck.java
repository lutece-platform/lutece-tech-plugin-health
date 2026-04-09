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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * Base class for database health checks.
 * Executes a validation query with a configurable timeout and reports connectivity and response time.
 */
abstract class AbstractDatabaseCheck implements HealthCheck
{
    private static final String PLUGIN_NAME = "rest-healthcheck";

    @Inject
    @ConfigProperty( name = "portal.checkvalidconnectionsql", defaultValue = "SELECT 1" )
    private String _strValidationQuery;

    @Inject
    @ConfigProperty( name = "healthcheck.database.timeoutMs", defaultValue = "5000" )
    private long _lTimeoutMs;

    protected abstract String getCheckName( );

    @Override
    public HealthCheckResponse call( )
    {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named( getCheckName( ) );

        long start = System.nanoTime( );

        ExecutorService executor = Executors.newSingleThreadExecutor( );
        Future<Boolean> future = executor.submit( ( ) -> {
            try ( DAOUtil daoUtil = new DAOUtil( _strValidationQuery, PluginService.getPlugin( PLUGIN_NAME ) ) )
            {
                daoUtil.executeQuery( );
                return daoUtil.next( );
            }
        } );

        try
        {
            boolean reachable = future.get( _lTimeoutMs, TimeUnit.MILLISECONDS );

            long durationMs = ( System.nanoTime( ) - start ) / 1_000_000;

            return builder.status( reachable )
                          .withData( "responseTimeMs", durationMs )
                          .withData( "validationQuery", _strValidationQuery )
                          .build( );
        }
        catch( TimeoutException e )
        {
            future.cancel( true );

            long durationMs = ( System.nanoTime( ) - start ) / 1_000_000;

            return builder.down( )
                          .withData( "responseTimeMs", durationMs )
                          .withData( "error", "Database query timed out after " + _lTimeoutMs + "ms" )
                          .build( );
        }
        catch( ExecutionException e )
        {
            long durationMs = ( System.nanoTime( ) - start ) / 1_000_000;

            String message = e.getCause( ) != null ? e.getCause( ).getMessage( ) : e.getMessage( );

            return builder.down( )
                          .withData( "responseTimeMs", durationMs )
                          .withData( "error", message )
                          .build( );
        }
        catch( InterruptedException e )
        {
            Thread.currentThread( ).interrupt( );

            return builder.down( )
                          .withData( "error", "Health check interrupted" )
                          .build( );
        }
        finally
        {
            executor.shutdownNow( );
        }
    }
}
