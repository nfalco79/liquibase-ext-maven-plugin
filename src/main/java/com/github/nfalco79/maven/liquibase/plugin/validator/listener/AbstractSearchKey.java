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
 * Default base class for all key implementation used to lookup into the
 * storage.
 * <p>
 * For each subclass remember to provide an equal strategy in {@link IStorageKey} implementation.
 *
 * @author Nikolas Falco
 */
public abstract class AbstractSearchKey implements IStorageKey {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            // this object does not have a equals implementations so delegate to equals of other object
            return obj.equals(this);
        }
        return false;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /*
     * Delegates to other object the equals implementation.
     */
    @Override
    public boolean isEqualsTo(IStorageKey obj) {
        throw new UnsupportedOperationException();
    }
}
