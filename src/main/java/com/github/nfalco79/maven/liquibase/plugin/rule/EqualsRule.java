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

import org.codehaus.plexus.util.StringUtils;

/**
 * Rule to check if the give value matches exactly an expected value.
 *
 * @author Nikolas Falco
 */
public class EqualsRule implements IRule {

    private String expected;

    public EqualsRule(String expected) {
        if (StringUtils.isBlank(expected)) {
            throw new IllegalArgumentException("expected value can not be null or an empty string");
        }
        this.expected = expected;
    }

    @Override
    public boolean isValid(final String value) {
        return expected.equals(value);
    }

    @Override
    public String getMessage(String field, String value) {
        return value + " does not matches the expected value " + expected;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expected == null) ? 0 : expected.hashCode());
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
        EqualsRule other = (EqualsRule) obj;
        if (expected == null) {
            if (other.expected != null) {
                return false;
            }
        } else if (!expected.equals(other.expected)) {
            return false;
        }
        return true;
    }

}