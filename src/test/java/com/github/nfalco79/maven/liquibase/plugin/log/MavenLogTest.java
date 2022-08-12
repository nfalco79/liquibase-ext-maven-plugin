/*
 * Copyright 2022 Nikolas Falco
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.maven.liquibase.plugin.log;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class MavenLogTest {

    @Test
    public void verify_no_print_when_maven_log_level_is_higher_than_mojo_logLevel_value() {
        Log mavenLog = mock(Log.class);
        when(mavenLog.isDebugEnabled()).thenReturn(false);
        when(mavenLog.isInfoEnabled()).thenReturn(false);
        when(mavenLog.isWarnEnabled()).thenReturn(true);
        when(mavenLog.isErrorEnabled()).thenReturn(true);

        MavenLogger logger = new MavenLogger(getClass().getName(), mavenLog, Level.WARNING);

        logger.info("test");
        verify(mavenLog, never()).info(anyString());

        logger.info("test", new Exception());
        verify(mavenLog, never()).info(anyString());

        logger.warning("test");
        verify(mavenLog).warn("test");
    }

    @Test
    public void verify_liquibase_logger_takes_care_of_mojo_logLevel_value() {
        Log mavenLog = mock(Log.class);
        when(mavenLog.isDebugEnabled()).thenReturn(true);
        when(mavenLog.isInfoEnabled()).thenReturn(true);
        when(mavenLog.isWarnEnabled()).thenReturn(true);
        when(mavenLog.isErrorEnabled()).thenReturn(true);

        MavenLogger logger = new MavenLogger(getClass().getName(), mavenLog, Level.INFO);

        logger.debug("test");
        verify(mavenLog, never()).debug(anyString());

        Exception e = new Exception();
        logger.debug("test", e);
        verify(mavenLog, never()).debug(anyString());

        logger.info("test");
        verify(mavenLog).info("test");

        logger.info("test", e);
        verify(mavenLog).info("test", e);
    }
}
