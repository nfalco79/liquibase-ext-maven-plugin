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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import liquibase.changelog.ChangeSet;

public class CompositeChangeSetValidatorTest {

    @Test
    public void test_composite() throws Exception {
        Collection<IChangeSetValidator> validators = new LinkedList<>();
        IChangeSetValidator validator1 = mock(IChangeSetValidator.class);
        ValidationError violation1 = mock(ValidationError.class);
        when(validator1.validate(any(ChangeSet.class))).thenReturn(Arrays.asList(violation1));
        validators.add(validator1);

        IChangeSetValidator validator2 = mock(IChangeSetValidator.class);
        ValidationError violation2 = mock(ValidationError.class);
        when(validator2.validate(any(ChangeSet.class))).thenReturn(Arrays.asList(violation2));
        validators.add(validator2);

        ChangeSet changeSet = mock(ChangeSet.class);

        CompositeChangeSetValidator validator = new CompositeChangeSetValidator(validators, new HashSet<String>());
        Collection<ValidationError> violations = validator.validate(changeSet);

        Assert.assertThat(violations, CoreMatchers.hasItems(violation1, violation2));
    }

    @Test
    public void test_ignore_rules() throws Exception {
        Collection<IChangeSetValidator> validators = new LinkedList<>();
        IChangeSetValidator validator1 = mock(DuplicatedIdValidator.class);
        validators.add(validator1);

        Set<String> ignoreRules = new HashSet<String>();
        ignoreRules.add("duplicatedId");

        CompositeChangeSetValidator composite = new CompositeChangeSetValidator(validators, ignoreRules);
        for (IChangeSetValidator validator : validators) {
            Assert.assertTrue(composite.skip(validator.getClass()));
        }
    }

}
