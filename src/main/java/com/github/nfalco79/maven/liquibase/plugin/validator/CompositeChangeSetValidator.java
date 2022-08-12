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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import liquibase.changelog.ChangeSet;

/**
 * A validator which collects a list of changeSet validator.
 */
public class CompositeChangeSetValidator implements IChangeSetValidator {

    private final Collection<IChangeSetValidator> validators;
    private final Set<String> ignoreRules;

    /**
     * Constructs an instance of this class.
     *
     * @param validators
     *            collection of changeSet validators
     * @param ignoreRules
     *            the rules to ignore
     */
    public CompositeChangeSetValidator(Collection<IChangeSetValidator> validators, Set<String> ignoreRules) {
        this.validators = Collections.unmodifiableCollection(validators);
        this.ignoreRules = (ignoreRules == null ? Collections.emptySet() : ignoreRules);
    }

    @Override
    public Collection<ValidationError> validate(ChangeSet changeSet) {
        Collection<ValidationError> issues = new ArrayList<>();

        for (IChangeSetValidator validator : validators) {
            if (!skip(validator.getClass())) {
                issues.addAll(validator.validate(changeSet));
            }
        }
        return Collections.unmodifiableCollection(issues);
    }

    /**
     * Skip the validator if its annotation name is in the list of exclusions.
     *
     * @param clazz
     *            the validator class
     * @return true if the validator is skipped
     */
    protected boolean skip(Class<?> clazz) {
        Validator annotation = clazz.getDeclaredAnnotation(Validator.class);
        String ruleName = annotation != null ? annotation.name() : null;
        return ruleName != null && ignoreRules.contains(ruleName);
    }

    /**
     * The list of changeSet validators.
     *
     * @return The list of changeSet validators
     */
    public Collection<IChangeSetValidator> getValidators() {
        return validators;
    }

    @Override
    public void configure(Map<String, String> parameters) {
        validators.forEach(v -> v.configure(parameters));
    }

}