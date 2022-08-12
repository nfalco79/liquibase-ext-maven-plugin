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

import com.github.nfalco79.maven.liquibase.plugin.util.ReflectionUtil;

public class ValidationContext {

    private Object subject;
    private String attributeName;
    private Object attributeValue;
    private boolean fromSubject;
    private ValidationContext parent;

    /**
     * Constructs an instance of this class.
     * 
     * @param subject
     *            the subject
     * @param attributeName
     *            the name of the attribute
     */
    public ValidationContext(Object subject, String attributeName) {
        this.subject = subject;
        this.attributeName = attributeName;
        this.fromSubject = true;
    }

    /**
     * Constructs an instance of this class.
     * 
     * @param subject
     *            the subject
     * @param attributeName
     *            the name of the attribute
     * @param attributeValue
     *            the value of the attribute
     */
    public ValidationContext(Object subject, String attributeName, Object attributeValue) {
        this(subject, attributeName);
        this.attributeValue = attributeValue;
        this.fromSubject = false;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Object getSubject() {
        return subject;
    }

    public ValidationContext getParent() {
        return parent;
    }

    public void setParent(ValidationContext parent) {
        this.parent = parent;
    }

    public String getAttributeValue() {
        if (fromSubject) {
            Object fieldValue = ReflectionUtil.getFieldValue(attributeName, subject);
            return fieldValue != null ? String.valueOf(fieldValue) : null;
        } else {
            return attributeValue != null ? String.valueOf(attributeValue) : null;
        }
    }

}