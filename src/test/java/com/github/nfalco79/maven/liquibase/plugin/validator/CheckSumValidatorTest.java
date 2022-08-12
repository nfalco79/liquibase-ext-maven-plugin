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

import static org.hamcrest.CoreMatchers.endsWithIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class CheckSumValidatorTest {

    @Test
    public void test_invalid_checksum() throws Exception {
        DatabaseChangeLog dbChangeLog = ValidatorUtil.load(getClass(), "changeset_checksum.xml");

        Collection<ValidationError> violations = new ArrayList<>();
        CheckSumValidator validator = new CheckSumValidator();

        for (ChangeSet changeSet : dbChangeLog.getChangeSets()) {
            violations.addAll(validator.validate(changeSet));
        }
        Assert.assertEquals("unexpected violations", 3, violations.size());
        List<String> messages = ValidatorUtil.extractMessage(violations);
        Assert.assertThat(messages, hasItems(endsWithIgnoringCase("the 1:any validateCheckSum is not allowed"), //
                endsWithIgnoringCase("the 1:all validateCheckSum is not allowed"), //
                endsWithIgnoringCase("1:* validateCheckSum is not allowed")));
    }

}