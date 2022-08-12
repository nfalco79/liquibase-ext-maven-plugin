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

import org.apache.maven.shared.utils.StringUtils;

/**
 * Utility class to check and manipulate String.
 *
 * @author Nikolas Falco
 *
 */
public final class StringUtil {

    private StringUtil() {
    }

    /**
     * Checks if a String has only lowercase characters.
     *
     * @param str
     *            the String to check
     * @return true if the String has only lowercase characters
     */
    public static boolean isLowerCase(String str) {
        return str == null || str.equals(str.toLowerCase()); // NOSONAR
    }

    /**
     * Remove params from type.
     *
     * @param type
     *            the type with params
     * @return type without params
     */
    public static String removeParam(final String type) {
        if (type != null) {
            int startIndex = type.indexOf('(');
            int endIndex = type.indexOf(')');

            String dt = type;
            if (startIndex != -1 && endIndex != -1) {
                dt = type.substring(0, startIndex) + type.substring(endIndex + 1);
            }
            return dt.trim();
        } else {
            return null;
        }
    }

    /**
     * Get params from type.
     *
     * @param type
     *            the type with params
     * @return params wuthout type
     */
    public static String getParam(final String type) {
        if (type != null) {
            int startIndex = type.indexOf('(');
            int endIndex = type.indexOf(')');

            String param = "";
            if (startIndex != -1 && endIndex != -1) {
                param = type.substring(startIndex + 1, endIndex);
            }
            return param.trim();
        } else {
            return null;
        }
    }

    /**
     * Divide the precision from the scale and get the precision value.
     *
     * @param numericValue
     *          length value of column type
     * @return the precision value
     */
    public static int getPrecision(String numericValue) {
        if (!StringUtils.isEmpty(numericValue)) {
            int commaIndex = numericValue.indexOf(',');
            if (commaIndex != -1) {
                String precision = numericValue.substring(0, commaIndex);
                return Integer.parseInt(precision);
            }
            return Integer.parseInt(numericValue);
        }
        return 0;
    }
}