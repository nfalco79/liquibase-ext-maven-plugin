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

import static com.github.nfalco79.maven.liquibase.plugin.validator.ValidatorFactory.*;

/**
 * Specific validator for Oracle 11g database.
 */
@Validator(name = "oracleLength")
public class Oracle11gLengthValidator extends RangeLengthValidator {

    /**
     * Instantiates a new Oracle11gConstraintLength validator.
     */
    public Oracle11gLengthValidator() {
        super(0, 30, CONSTRAINT_NAME, INDEX_NAME, BASE_COLUMN_NAMES, BASE_TABLE_NAME, REFERENCED_TABLE_NAME, TABLE_NAME, COLUMN_NAME, SEQUENCE_NAME, NEW_COLUMN_NAME, OLD_COLUMN_NAME, NEW_SEQUENCE_NAME, OLD_SEQUENCE_NAME);
    }
}