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

import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ChangeStorage;

import liquibase.change.Change;

public class CompositeValidator implements IChangeValidator {

    private final Collection<IChangeValidator> validators;
    private final Set<String> ignoreRules;
    private ChangeStorage storage;

    public Collection<IChangeValidator> getValidators() {
        return validators;
    }

    /**
     * Constructs an instance of this class.
     *
     * @param validators
     *            collection of validators
     * @param ignoreRules
     *            the rules to ignore
     * @param storage
     *            the stored information about changes
     */
    public CompositeValidator(Collection<IChangeValidator> validators, Set<String> ignoreRules, ChangeStorage storage) {
        this.validators = Collections.unmodifiableCollection(validators);
        this.ignoreRules = (ignoreRules == null ? Collections.emptySet() : ignoreRules);
        this.storage = storage;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new ArrayList<>();

        for (IChangeValidator validator : validators) {
            if (!skip(validator.getClass())) {
                issues.addAll(validator.validate(change, storage));
            }
        }
        return Collections.unmodifiableCollection(issues);
    }

    protected boolean skip(Class<?> clazz) {
        Validator annotation = clazz.getDeclaredAnnotation(Validator.class);
        String ruleName = annotation != null ? annotation.name() : null;
        return ruleName != null && ignoreRules.contains(ruleName);
    }

    @Override
    public void configure(Map<String, String> parameters) {
        validators.forEach(v -> v.configure(parameters));
    }

}