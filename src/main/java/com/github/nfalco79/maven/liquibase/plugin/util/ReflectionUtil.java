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
package com.github.nfalco79.maven.liquibase.plugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.codehaus.plexus.util.ReflectionUtils;

import com.github.nfalco79.maven.liquibase.plugin.validator.ChangeValidationException;

/**
 * Utility class to obtain fields and methods values of a Class.
 *
 * @author Nikolas Falco
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    /**
     * Returns the value of the field on the specified object, given the field's
     * name.
     * 
     * @param fieldName
     *            the field name for which extract its value by reflection
     * @param obj
     *            object instance where contains the field
     * @return the value for the specified field
     */
    public static Object getFieldValue(String fieldName, Object obj) {
        String value = null;
        if (obj != null) {
            try {
                Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses(fieldName, obj.getClass());
                if (field != null) {
                    field.setAccessible(true);
                    return field.get(obj);
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new ChangeValidationException(e);
            }
        }

        return value;
    }

    /**
     * Returns an object of type Method given the method's name.
     * 
     * @param methodName
     *            the name of the method
     * @param obj
     *            object instance that contains the method definition
     * @return a {@link Method} instance that represent the method specified
     */
    public static Method getMethod(String methodName, Object obj) {
        Method method = null;
        try {
            if (obj != null) {
                method = obj.getClass().getMethod(methodName);
            }
        } catch (NoSuchMethodException e) {
            // nothing to do
        }
        return method;
    }

}
