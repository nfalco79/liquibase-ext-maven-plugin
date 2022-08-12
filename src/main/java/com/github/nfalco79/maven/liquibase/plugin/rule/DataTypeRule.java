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

import java.util.Arrays;
import java.util.List;

import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

/**
 * Rule to check if the give data type is one of the allowed SQL-99 types.
 *
 * @author Nikolas Falco
 */
public class DataTypeRule implements IRule {
    /* package */static final List<String> SQL99_DATATYPE = Arrays.asList("BOOLEAN", "INTEGER", "SMALLINT", "BIGINT", "REAL", "DOUBLE PRECISION", "DATE");
    /* package */static final List<String> SQL99_PARAMETRIC_DATATYPE = Arrays.asList("CHARACTER", "VARCHAR", "CLOB", "NCHAR", "NVARCHAR", "NCLOB", "BLOB", "DECIMAL", "NUMERIC", "FLOAT", "TIMESTAMP");

    @Override
    public boolean isValid(String dataType) {
        if (dataType == null) {
            return true;
        }

        String dt = dataType.trim().toUpperCase();
        if (SQL99_DATATYPE.contains(dt)) {
            return true;
        } else {
            for (String parametric : SQL99_PARAMETRIC_DATATYPE) {
                if (parametric.equals(StringUtil.removeParam(dt))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getMessage(String fieldType, String dataType) {
        return dataType + " isn't one of SQL-99 standard types";
    }
}