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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import fr.paris.lutece.portal.service.init.AppInit;

/**
 * MicroProfile Health Liveness check of the Lutece webapp itself, exposed on /health/live.
 * Down only when a restart of the instance can repair it: webapp not loaded, or threads in deadlock. It does not depend
 * on the database: an unavailable database is reported by the readiness check, which takes the instance out of the
 * traffic without restarting it (Lutece reconnects when the database is back).
 */
@Liveness
@ApplicationScoped
public class ApplicationLivenessCheck implements HealthCheck
{
    private static final String HEALTHCHECK_NAME = "application-liveness";
    private static final String DATA_WEBAPP_LOADED = "webappLoaded";
    private static final String DATA_DEADLOCKED_THREADS = "deadlockedThreads";

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthCheckResponse call( )
    {
        boolean bLoaded = AppInit.isWebappSuccessfullyLoaded( );
        int nDeadlocked = countDeadlockedThreads( ManagementFactory.getThreadMXBean( ) );

        HealthCheckResponseBuilder builder = HealthCheckResponse.named( HEALTHCHECK_NAME )
                .withData( DATA_WEBAPP_LOADED, bLoaded )
                .withData( DATA_DEADLOCKED_THREADS, nDeadlocked );

        return builder.status( bLoaded && nDeadlocked == 0 ).build( );
    }

    /**
     * Counts the threads in deadlock, waiting for monitors or ownable synchronizers
     *
     * @param threadMXBean
     *            the thread management bean of the JVM
     * @return the number of threads in deadlock, 0 if none
     */
    static int countDeadlockedThreads( ThreadMXBean threadMXBean )
    {
        long [ ] ids = threadMXBean.isSynchronizerUsageSupported( ) ? threadMXBean.findDeadlockedThreads( ) : threadMXBean.findMonitorDeadlockedThreads( );
        return ( ids == null ) ? 0 : ids.length;
    }
}
