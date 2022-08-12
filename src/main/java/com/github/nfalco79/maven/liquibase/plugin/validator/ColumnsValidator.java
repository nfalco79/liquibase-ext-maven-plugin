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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.github.nfalco79.maven.liquibase.plugin.rule.DataTypeRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.LOBDimensionRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.LowerCaseRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.MaxLenghtRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.MinLenghtRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.NotPermittedRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.NumericPrecisionRule;
import com.github.nfalco79.maven.liquibase.plugin.rule.RuleEngine;
import com.github.nfalco79.maven.liquibase.plugin.util.LiquibaseUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.ReflectionUtil;
import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;

@Validator(name = "columns")
public class ColumnsValidator implements IChangeValidator {

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String COLUMNS = "columns";
    private static final String FOREIGN_KEY_NAME = "foreignKeyName";
    private static final String PRIMARY_KEY_NAME = "primaryKeyName";
    private static final String UNIQUE_CONSTRAINT_NAME = "uniqueConstraintName";
    private static final String PRIVATE_KEY_TABLESBACE_NAME = "privateKeyTablespace";
    private static final String REFERENCED_TABLE_CATALOG_NAME = "referencedTableCatalogName";
    private static final String REFERENCED_TABLE_SCHEMA_NAME = "referencedTableSchemaName";
    private static final String DEFAULT_VALUE = "defaultValue";

    private static final String[] BOOLEAN_DATATYPE = { "BOOLEAN" };
    private static final String[] NUMERIC_DATATYPE = { "INTEGER", "SMALLINT", "BIGINT", "REAL", "DOUBLE PRECISION", "DECIMAL", "NUMERIC",
                                                       "FLOAT" };
    private static final String[] DATE_DATATYPE = { "TIMESTAMP", "DATE" };
    private static final Map<String, String> DATATYPE_MAP = createMap();

    private static Map<String, String> createMap() {
        Map<String, String> result = new HashMap<>();
        for (String type : BOOLEAN_DATATYPE) {
            result.put(type, "defaultBooleanValue");
        }
        for (String type : NUMERIC_DATATYPE) {
            result.put(type, "defaultNumericValue");
        }
        for (String type : DATE_DATATYPE) {
            result.put(type, "defaultDateValue");
        }
        return result;
    }

    private final int min;
    private final int max;
    private final int maxPrecision;

    /**
     * Constructs an instance of this class.
     *
     * @param min the min length allowed
     * @param max the max length allowed
     * @param maxPrecision the max precision allowed for NUMERIC type
     */
    public ColumnsValidator(final int min, final int max, final int maxPrecision) {
        this.min = min;
        this.max = max;
        this.maxPrecision = maxPrecision;
    }

    @Override
    public Collection<ValidationError> validate(Change change) {
        Collection<ValidationError> issues = new LinkedList<>();

        NotPermittedRule notPermittedRule = new NotPermittedRule();
        MaxLenghtRule maxLengthRule = new MaxLenghtRule(max);
        MinLenghtRule minLengthRule = new MinLenghtRule(min);
        LowerCaseRule lowerCaseRule = new LowerCaseRule();
        DataTypeRule dataTypeRule = new DataTypeRule();
        NumericPrecisionRule numericPrecisionRule = new NumericPrecisionRule(maxPrecision);
        LOBDimensionRule lobDimensionRule = new LOBDimensionRule();

        RuleEngine ruleEngine = new RuleEngine();

        ValidationContext parentCtx = new ValidationContext(change, COLUMNS);

        @SuppressWarnings("unchecked")
        Collection<ColumnConfig> columns = (Collection<ColumnConfig>) ReflectionUtil.getFieldValue(COLUMNS, change);
        if (columns != null) {
            for (ColumnConfig column : columns) {
                ValidationContext columnNameCtx = buildContext(parentCtx, column, NAME, column.getName());
                ValidationContext columnTypeCtx = buildContext(parentCtx, column, TYPE, column.getType());

                ruleEngine.add(lowerCaseRule, columnNameCtx);
                ruleEngine.add(maxLengthRule, columnNameCtx);
                ruleEngine.add(minLengthRule, columnNameCtx);
                ruleEngine.add(dataTypeRule, columnTypeCtx);
                ruleEngine.add(notPermittedRule, getValueContexts(parentCtx, column));
                ruleEngine.add(numericPrecisionRule, columnTypeCtx);
                ruleEngine.add(lobDimensionRule, columnTypeCtx);

                // validate on column constraint
                ConstraintsConfig constraints = column.getConstraints();
                if (constraints != null) {
                    ValidationContext uniqueConstrCtx = buildContext(parentCtx, constraints, UNIQUE_CONSTRAINT_NAME, constraints.getUniqueConstraintName());
                    ValidationContext foreignKeyCtx = buildContext(parentCtx, constraints, FOREIGN_KEY_NAME, constraints.getForeignKeyName());
                    ValidationContext primaryKeyCtx = buildContext(parentCtx, constraints, PRIMARY_KEY_NAME, constraints.getPrimaryKeyName());

                    ruleEngine.add(lowerCaseRule, uniqueConstrCtx, foreignKeyCtx, primaryKeyCtx);
                    ruleEngine.add(minLengthRule, uniqueConstrCtx, foreignKeyCtx, primaryKeyCtx);
                    ruleEngine.add(maxLengthRule, uniqueConstrCtx, foreignKeyCtx, primaryKeyCtx);

                    ValidationContext primaryKeyTlbCtx = buildContext(parentCtx, constraints, PRIVATE_KEY_TABLESBACE_NAME, constraints.getPrimaryKeyTablespace());
                    ValidationContext refTlbCatalogCtx = buildContext(parentCtx, constraints, REFERENCED_TABLE_CATALOG_NAME, constraints.getReferencedTableCatalogName());
                    ValidationContext refTlbSchemaCtx = buildContext(parentCtx, constraints, REFERENCED_TABLE_SCHEMA_NAME, constraints.getReferencedTableSchemaName());

                    ruleEngine.add(notPermittedRule, primaryKeyTlbCtx, refTlbCatalogCtx, refTlbSchemaCtx);
                }

                // check of correctness for the defaultValue attribute based on
                // column type
                String columnType = column.getType();
                if (columnType != null) {
                    columnType = StringUtil.removeParam(columnType.trim().toUpperCase());

                    Object defaultValue = ReflectionUtil.getFieldValue(DEFAULT_VALUE, column);
                    if (defaultValue != null) {
                        String defaultTypedValue = DATATYPE_MAP.get(columnType);
                        if (defaultTypedValue != null) {
                            String message = DEFAULT_VALUE + " is not allowed because is not portable. Replace it with "
                                    + defaultTypedValue;
                            issues.add(LiquibaseUtil.createIssue(change, DEFAULT_VALUE, message));
                        }
                    }
                }
            }
        }

        issues.addAll(ruleEngine.execute());
        return issues;
    }

    private ValidationContext buildContext(ValidationContext parent, Object subject, String attributeName, Object attributeValue) {
        ValidationContext context = new ValidationContext(subject, attributeName, attributeValue);
        context.setParent(parent);
        return context;
    }

    private ValidationContext[] getValueContexts(ValidationContext parent, ColumnConfig column) {
        return new ValidationContext[] {
                                         buildContext(parent, column, "computed", column.getComputed()),
                                         buildContext(parent, column, "value", column.getValue()),
                                         buildContext(parent, column, "valueNumeric", column.getValueNumeric()),
                                         buildContext(parent, column, "valueDate", column.getValueDate()),
                                         buildContext(parent, column, "valueBoolean", column.getValueBoolean()),
                                         buildContext(parent, column, "valueBlobFile", column.getValueBlobFile()),
                                         buildContext(parent, column, "valueClobFile", column.getValueClobFile()),
                                         buildContext(parent, column, "valueComputed", column.getValueComputed()),
                                         buildContext(parent, column, "defaultValueComputed", column.getDefaultValueComputed()) };
    }

}