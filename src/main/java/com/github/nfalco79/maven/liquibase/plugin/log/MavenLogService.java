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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;

/**
 * An implementation of the Liquibase LogFactory that logs all messages to the
 * given Maven mojo. This should only be used inside of maven execution.
 */
public class MavenLogService extends AbstractLogService {
    private final Map<String, Logger> loggers = new HashMap<String, Logger>();
    private final Log mavenLog;
    private final Level defaultLogLevel;
    private boolean limitLog;

    /**
     * Default constructor.
     *
     * @param mavenLog
     *            maven log to delegate
     * @param defaultLogLevel
     *            log level set in mojo
     */
    public MavenLogService(Log mavenLog, Level defaultLogLevel) {
        this.mavenLog = mavenLog;
        this.defaultLogLevel = defaultLogLevel;
    }

    @Override
    public Logger getLog(@SuppressWarnings("rawtypes") Class clazz) {
        String name = clazz.getName();
        if (!loggers.containsKey(name)) {
            MavenLogger logger = new MavenLogger(name, mavenLog, defaultLogLevel);
            logger.setLimitLog(limitLog);
            loggers.put(name, logger);
        }

        return loggers.get(name);
    }

    public boolean isLimitLog() {
        return limitLog;
    }

    public void setLimitLog(boolean limitLog) {
        this.limitLog = limitLog;
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
