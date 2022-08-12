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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ValidationContextTest {

    private class Subject {
        private String name;

        public Subject(String name) {
            this.name = name;
        }
    }

    @Test
    public void verify_returns_attribute_value_using_reflection() throws Exception {
        String nameValue = "foo";
        ValidationContext validationContext = new ValidationContext(new Subject(nameValue), "name");
        Assert.assertThat(validationContext.getAttributeValue(), CoreMatchers.equalTo(nameValue));
    }

    @Test
    public void verify_returns_attribute_value_passed_in_the_context() throws Exception {
        String attributeValue = "value";
        ValidationContext validationContext = new ValidationContext(new Subject("some value"), "name", attributeValue);
        Assert.assertThat(validationContext.getAttributeValue(), CoreMatchers.equalTo(attributeValue));
    }

}
