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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.ReflectionUtils;

/**
 * Utility class for validators.
 *
 * @author Nikolas Falco
 *
 */
public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    /**
     * Configure the field annotated as Configuration of the given validator.
     *
     * @param validator
     *            to configure
     * @param parameters
     *            the configMap passed
     */
    public static void configure(Object validator, Map<String, String> parameters) {
        Class<?> validatorClass = validator.getClass();
        String validatorName = LiquibaseUtil.getValidatorName(validatorClass);
        for (Field f : ReflectionUtils.getFieldsIncludingSuperclasses(validatorClass)) {
            Configuration configAnnotation = f.getAnnotation(Configuration.class);
            if (configAnnotation != null) {
                String key = validatorName + "." + configAnnotation.value();
                if (parameters.containsKey(key)) {
                    try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                            @Override
                            public Void run() throws Exception {
                                f.setAccessible(true);
                                f.set(validator, parameters.get(key));
                                return null;
                            }
                        });
                    } catch (PrivilegedActionException e) { // NOSONAR
                        throw new IllegalStateException("Can not configure validator " + validatorName + " with key " + key);
                    }
                }
            }
        }
    }
}
