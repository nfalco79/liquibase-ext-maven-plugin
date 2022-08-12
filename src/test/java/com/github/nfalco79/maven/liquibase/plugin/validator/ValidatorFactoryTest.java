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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnKey;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropIndexChange;
import liquibase.changelog.ChangeSet;

public class ValidatorFactoryTest {

    @Test
    public void test_extra_validators() {
        IChangeValidator extraValidator = mock(IChangeValidator.class);
        when(extraValidator.validate(any(Change.class))).thenReturn(Collections.emptyList());

        ValidatorFactory factory = new ValidatorFactory();
        factory.addValidator(extraValidator);
        CreateSequenceChange change = new CreateSequenceChange();
        change.setIncrementBy(new BigInteger("50"));
        change.setStartValue(new BigInteger("50"));
        change.setSequenceName("test");
        IChangeValidator validators = factory.newValidator(change);
        validators.validate(change);

        verify(extraValidator).validate(any(Change.class), any(ChangeStorage.class));
    }

    @Test
    public void test_change_validator_singleton() {
        ValidatorFactory factory = new ValidatorFactory();
        IChangeValidator validator1 = ((CompositeValidator) factory.newValidator(new DropIndexChange())) //
                .getValidators().stream().filter(DuplicatedIndexValidator.class::isInstance).findFirst().get();
        IChangeValidator validator2 = ((CompositeValidator) factory.newValidator(new DropIndexChange())) //
                .getValidators().stream().filter(DuplicatedIndexValidator.class::isInstance).findFirst().get();
        Assertions.assertThat(validator1).isSameAs(validator2);
    }

    @Test
    public void test_changeset_validator_singleton() {
        ValidatorFactory factory = new ValidatorFactory();
        IChangeSetValidator validator1 = ((CompositeChangeSetValidator) factory.newChangeSetValidator(mock(ChangeSet.class))) //
                .getValidators().stream().filter(DuplicatedIdValidator.class::isInstance).findFirst().get();
        IChangeSetValidator validator2 = ((CompositeChangeSetValidator) factory.newChangeSetValidator(mock(ChangeSet.class))) //
                .getValidators().stream().filter(DuplicatedIdValidator.class::isInstance).findFirst().get();
        Assertions.assertThat(validator1).isSameAs(validator2);
    }

    @Test(expected = IllegalStateException.class)
    public void test_no_validator_annotation() {

        class SubclassValidatorFactory extends ValidatorFactory {
            @Override
            public IChangeValidator newValidator(Change change) {
                scopedValidator(new IChangeValidator() {
                    @Override
                    public Collection<ValidationError> validate(Change c) {
                        return new LinkedList<>();
                    }
                });
                return null;
            }
        }

        SubclassValidatorFactory factory = new SubclassValidatorFactory();
        factory.newValidator(new AddColumnChange());
    }

    @Test
    public void test_change_storage() {
        ValidatorFactory factory = new ValidatorFactory();

        CreateTableChange change = new CreateTableChange();
        ColumnConfig col = new ColumnConfig();
        col.setName("col1");
        change.setTableName("t1");
        change.addColumn(col);
        factory.newValidator(change);

        AddColumnChange addColChange = new AddColumnChange();
        addColChange.setTableName("t1");
        AddColumnConfig colConf = new AddColumnConfig();
        colConf.setName("col2");
        addColChange.addColumn(colConf);
        factory.newValidator(addColChange);

        Assert.assertEquals(2, factory.getStorage().size());
        Assert.assertTrue(factory.getStorage().containsKey(new ColumnKey("t1", "col1")));
        Assert.assertTrue(factory.getStorage().containsKey(new ColumnKey("t1", "col2")));
    }
}
