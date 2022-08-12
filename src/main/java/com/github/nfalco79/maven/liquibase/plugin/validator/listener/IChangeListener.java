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

import liquibase.change.Change;

/**
 * Listener interface for Liquibase changes.
 */
public interface IChangeListener {

    /**
     * Extract info from the given change and update the given storage.
     *
     * @param change
     *            the change to check
     * @param storage
     *            the storage to update
     */
    void updateStorage(Change change, ChangeStorage storage);

    /**
     * Tests if the listener apply to the given change.
     *
     * @param change
     *            the change to check
     * @return if the listener apply to the change.
     */
    boolean applyTo(Change change);
}
