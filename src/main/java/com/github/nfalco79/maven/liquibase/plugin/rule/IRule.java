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
package com.github.nfalco79.maven.liquibase.plugin.rule;

public interface IRule {

    /**
     * Returns the given value is accepted by the rule.
     *
     * @param value
     *            to test
     * @return {@literal true} if the value is valid for this rule,
     *         {@literal false} otherwise.
     */
    boolean isValid(String value);

    /**
     * Returns the violation message for the given value of the field.
     *
     * @param field
     *            object of rule validation
     * @param value
     *            tested
     * @return the violation message
     */
    String getMessage(String field, String value);

}