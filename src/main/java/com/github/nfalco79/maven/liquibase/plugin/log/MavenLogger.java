/*
 * Copyright 2022 Falco Nikolas
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

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

import liquibase.logging.core.AbstractLogger;

/**
 * An implementation of the Liquibase logger that logs to the given Maven mojo.
 */
public class MavenLogger extends AbstractLogger {

    private static final String LOGGER_SERVICE_LOCATOR = "liquibase.servicelocator.";
    private static final String LOGGER_CHANGE_SET = "liquibase.changelog.ChangeSet";

    private final String name;
    private final Log log;
    private final Level logLevel;
    private boolean limitLog;

    /**
     * Default constructor.
     *
     * @param name
     *            log name
     * @param log
     *            maven log to delegate
     * @param logLevel
     *            set in the mojo
     */
    public MavenLogger(String name, Log log, Level logLevel) {
        this.name = name;
        this.log = log;
        this.logLevel = logLevel;
    }

    public boolean isLimitLog() {
        return limitLog;
    }

    public void setLimitLog(boolean limitLog) {
        this.limitLog = limitLog;
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        if (level.intValue() >= logLevel.intValue()) {
            // Note that maven log could use exception itself to print stacktrace
            if (name.equals(LOGGER_CHANGE_SET) && limitLog &&
                    (!message.startsWith("ChangeSet") || message.startsWith("Change set"))) {
                if (e != null) {
                    log.debug(message, e);
                } else {
                    log.debug(message);
                }
            } else if (name.startsWith(LOGGER_SERVICE_LOCATOR) && e == null) {
                // very useless messages from this class
                log.debug(message);
            } else if (Level.SEVERE == level) {
                if (e != null) {
                    log.error(message, e);
                } else {
                    log.error(message);
                }
            } else if (Level.WARNING == level) {
                if (e != null) {
                    log.warn(message, e);
                } else {
                    log.warn(message);
                }
            } else if (Level.INFO == level) {
                if (e != null) {
                    log.info(message, e);
                } else {
                    log.info(message);
                }
            } else if (level.intValue() <= Level.FINE.intValue()) {
                if (e != null) {
                    log.debug(message, e);
                } else {
                    log.debug(message);
                }
            }
        }
    }

}