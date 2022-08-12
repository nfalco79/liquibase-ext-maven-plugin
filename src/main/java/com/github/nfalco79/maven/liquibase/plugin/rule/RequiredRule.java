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

import org.codehaus.plexus.util.StringUtils;

/**
 * Rule to check if an attribute is not valued but should be.
 *
 * @author Nikolas Falco
 */
public class RequiredRule implements IRule {

    @Override
    public boolean isValid(String value) {
        return StringUtils.isNotBlank(value);
    }

    @Override
    public String getMessage(String field, String value) {
        return getMessage(field);
    }

    private String getMessage(String field) {
        return field + " is a required attribute";
    }

}
