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
package com.github.nfalco79.maven.liquibase.plugin.validator.listener;

/**
 * Storage interface to identify a specific Liquibase object.
 */
public interface IStorageKey {

    /**
     * Method that say if this key is equals to the given key.
     *
     * @param obj
     *            against compare.
     * @return true if key are considered logical equal or not.
     */
    boolean isEqualsTo(IStorageKey obj);

}
