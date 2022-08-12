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

import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Store every information about Liquibase changes that the listeners detect.
 */
public class ChangeStorage extends HashMap<IStorageKey, IStorageInfo> {
    private static final long serialVersionUID = -5680492032263567777L;

    /**
     * Filter {@link IStorageKey} by implementation and with the matches key.
     *
     * @param <T> implementation type.
     * @param clazz used to filter interesting implementation
     * @param key to matches against.
     * @return a stream of found information
     */
    @SuppressWarnings("unchecked")
    public <T extends IStorageInfo> Stream<T> filterBy(Class<T> clazz, IStorageKey key) {
        return entrySet().stream() //
                .filter(e -> clazz.equals(e.getValue().getClass())) //
                .filter(e -> key.equals(e.getKey())) //
                .map(e -> (T) e.getValue());
    }

    @Override
    public boolean containsKey(Object key) {
        return keySet().stream().anyMatch(key::equals);
    }

    @Override
    public boolean containsValue(Object value) {
        return values().stream().anyMatch(value::equals);
    }
}
