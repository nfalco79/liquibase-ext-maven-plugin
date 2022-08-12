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
package com.github.nfalco79.maven.liquibase.plugin.validator.sizextractor;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;

public class ColumnRowSizeDB2Test {

    private ColumnRowSizeDB2 extractor = new ColumnRowSizeDB2();
    private int EXPECTED_SMALLINT_SIZE = 4;
    private int EXPECTED_INTEGER_SIZE = 6;
    private int EXPECTED_BIGINT_SIZE = 10;
    private int EXPECTED_DOUBLE_SIZE = 10;
    private int EXPECTED_DATE_SIZE = 6;
    private int EXPECTED_TIME_SIZE = 5;

    private ColumnInfo newColumnInfo(String name, String type, String length) {
        ColumnInfo info = new ColumnInfo("table_name", name);
        info.setType(type);
        if (!length.equals("0")) {
            info.setLength(length);
        }
        return info;
    }

    @Test
    public void verify_smallint_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "BOOLEAN", "0");
        ColumnInfo c2 = newColumnInfo("col2", "TINYINT", "0");
        ColumnInfo c3 = newColumnInfo("col3", "SMALLINT", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_SMALLINT_SIZE);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(EXPECTED_SMALLINT_SIZE);
        Assertions.assertThat(extractor.getRowSize(c3)).isEqualTo(EXPECTED_SMALLINT_SIZE);
    }

    @Test
    public void verify_integer_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "INTEGER", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_INTEGER_SIZE);
    }

    @Test
    public void verify_bigint_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "BIGINT", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_BIGINT_SIZE);
    }

    @Test
    public void verify_double_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "FLOAT", "0");
        ColumnInfo c2 = newColumnInfo("col2", "DOUBLE", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_DOUBLE_SIZE);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(EXPECTED_DOUBLE_SIZE);
    }

    @Test
    public void verify_decimal_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "NUMBER", "10");
        ColumnInfo c2 = newColumnInfo("col2", "DECIMAL", "10");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(8);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(8);
    }

    @Test
    public void verify_char_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "NCHAR", "3");
        ColumnInfo c2 = newColumnInfo("col2", "CHAR", "3");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(5);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(5);
    }

    @Test
    public void verify_varchar_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "NVARCHAR", "5");
        ColumnInfo c2 = newColumnInfo("col2", "VARCHAR", "5");
        ColumnInfo c3 = newColumnInfo("col2", "VARCHAR", "31");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(10);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(10);
        Assertions.assertThat(extractor.getRowSize(c3)).isEqualTo(29);
    }

    @Test
    public void verify_date_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "DATE", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_DATE_SIZE);
    }

    @Test
    public void verify_time_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "TIME", "0");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(EXPECTED_TIME_SIZE);
    }

    @Test
    public void verify_timestamp_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "DATETIME", "10");
        ColumnInfo c2 = newColumnInfo("col2", "TIMESTAMP", "10");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(14);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(14);
    }

    @Test
    public void verify_lob_size() throws Exception {
        ColumnInfo c1 = newColumnInfo("tenant_id", "BLOB", "1024");
        ColumnInfo c2 = newColumnInfo("col2", "CLOB", "523000");
        ColumnInfo c3 = newColumnInfo("col3", "NCLOB", "536000001");

        Assertions.assertThat(extractor.getRowSize(c1)).isEqualTo(73);
        Assertions.assertThat(extractor.getRowSize(c2)).isEqualTo(145);
        Assertions.assertThat(extractor.getRowSize(c3)).isEqualTo(257);
    }
}