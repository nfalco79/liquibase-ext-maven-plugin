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

import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.change.core.ModifyDataTypeChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class Oracle11gModifyDataTypeValidatorTest {

    @Test
    public void test_that_modify_data_type_to_clob_is_not_permitted() throws Exception {
        Collection<ValidationError> violations = getViolations("CLOB");
        Assert.assertEquals("unexpected violations", 1, violations.size());
        String message = violations.iterator().next().getMessage();
        Assert.assertThat(message, CoreMatchers.containsString("Oracle 11g not allows modify data type to CLOB"));
    }

    @Test
    public void test_that_modify_data_type_to_blob_is_not_permitted() throws Exception {
        Collection<ValidationError> violations = getViolations("BLOB");
        Assert.assertEquals("unexpected violations", 1, violations.size());
        String message = violations.iterator().next().getMessage();
        Assert.assertThat(message, CoreMatchers.containsString("Oracle 11g not allows modify data type to BLOB"));
    }

    @Test
    public void test_that_modify_data_type_to_sql_99_type_is_permitted() throws Exception {
        Collection<ValidationError> violations = getViolations("CHARACTER");
        Assert.assertEquals("unexpected violations", 0, violations.size());
    }

    private Collection<ValidationError> getViolations(String newDataType) {
        ModifyDataTypeChange change = new ModifyDataTypeChange();
        change.setChangeSet(new ChangeSet(new DatabaseChangeLog()));
        change.setNewDataType(newDataType);

        Oracle11gModifyDataTypeValidator validator = new Oracle11gModifyDataTypeValidator();

        return validator.validate(change);
    }

}
