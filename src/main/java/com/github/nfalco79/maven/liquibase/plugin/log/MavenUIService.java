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

import liquibase.AbstractExtensibleObject;
import liquibase.ui.InputHandler;
import liquibase.ui.UIService;

public class MavenUIService extends AbstractExtensibleObject implements UIService {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendErrorMessage(String message) {
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
    }

    @Override
    public <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
        return valueIfNoEntry;
    }

    @Override
    public void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
        if (allowPrompt) {
            throw new IllegalArgumentException("allowPrompt=true not allowed in " + getClass().getSimpleName());
        }
    }

    @Override
    public boolean getAllowPrompt() {
        return false;
    }

}