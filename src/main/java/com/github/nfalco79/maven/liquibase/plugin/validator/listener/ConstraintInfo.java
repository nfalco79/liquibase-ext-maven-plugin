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

import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Class to collect information about a column.
 */
public class ConstraintInfo implements IStorageInfo {
    private class ConstraintInfoKey implements IStorageKey {

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public boolean equals(Object obj) { // NOSONAR
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                ConstraintInfoKey other = (ConstraintInfoKey) obj;
                return Objects.equals(name, other.getName());
            }
            return false;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean isEqualsTo(IStorageKey obj) {
            return false;
        }
    }

    /**
     * Constraint affinity.
     *
     */
    public enum ConstraintType {
        PRIMARY_KEY, //
        FOREIGN_KEY, //
        INDEX;
    }

    private String name;
    private ConstraintType type;

    /**
     * Default constructor. Storage the basic column constraint informations to be used by
     * validators.
     *
     */
    public ConstraintInfo() {
        this(RandomStringUtils.randomAlphanumeric(8));
    }

    /**
     * Default constructor. Storage the basic column constraint informations to be used by
     * validators.
     *
     * @param name
     *            of this constraint.
     */
    public ConstraintInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConstraintType getType() {
        return type;
    }

    public void setType(ConstraintType type) {
        this.type = type;
    }

    @Override
    public IStorageKey getKey() {
        return new ConstraintInfoKey();
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public String toString() {
        return type.toString().toLowerCase() + " " + name;
    }
}