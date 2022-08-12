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

/**
 * Rule to check if a value exceed a given length.
 *
 * @author Nikolas Falco
 */
public class MaxLenghtRule implements IRule {

    private int max;

    public MaxLenghtRule(int max) {
        this.max = max;
    }

    @Override
    public boolean isValid(final String fieldValue) {
        return fieldValue == null || fieldValue.trim().length() <= max;
    }

    @Override
    public String getMessage(String field, String value) {
        return value + " exceeds max lenght " + max;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + max;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MaxLenghtRule other = (MaxLenghtRule) obj;
        if (max != other.max) {
            return false;
        }
        return true;
    }
}