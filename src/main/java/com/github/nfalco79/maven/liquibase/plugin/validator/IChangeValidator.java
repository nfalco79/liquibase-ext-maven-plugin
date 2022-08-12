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
package com.github.nfalco79.maven.liquibase.plugin.validator;

import java.util.Collection;
import java.util.Map;

import com.github.nfalco79.maven.liquibase.plugin.util.ValidatorUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;

import liquibase.change.Change;

public interface IChangeValidator {

    Collection<ValidationError> validate(Change change);

    /**
     * Validate the given change with additional information about previous
     * changes.
     *
     * @param change
     *            the change to validate
     * @param storage
     *            information collected
     * @return the collected validation errors
     */
    default Collection<ValidationError> validate(Change change, ChangeStorage storage) {
        return validate(change);
    }

    /**
     * Configure the fields declared in the map.
     *
     * @param parameters
     *            the configMap passed
     */
    default void configure(Map<String, String> parameters) {
        ValidatorUtil.configure(this, parameters);
    }

}