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
package com.github.nfalco79.maven.liquibase.plugin.rule;

import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

/**
 * Rule to check if the give value is in lower case.
 *
 * @author Nikolas Falco
 */
public class LowerCaseRule implements IRule {

    @Override
    public boolean isValid(final String value) {
        return StringUtil.isLowerCase(value);
    }

    @Override
    public String getMessage(String field, String value) {
        return value + " must be lowercase";
    }
}