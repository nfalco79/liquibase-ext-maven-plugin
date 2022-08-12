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
 * Rule to check if a value is shorter than a given length.
 *
 * @author Nikolas Falco
 */
public class MinLenghtRule implements IRule {

    private int min;

    public MinLenghtRule(int min) {
        this.min = min;
    }

    @Override
    public boolean isValid(final String value) {
        return value == null || value.trim().length() >= min;
    }

    @Override
    public String getMessage(String field, String value) {
        return value + " is shorter than " + min;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + min;
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
        MinLenghtRule other = (MinLenghtRule) obj;
        if (min != other.min) {
            return false;
        }
        return true;
    }
}