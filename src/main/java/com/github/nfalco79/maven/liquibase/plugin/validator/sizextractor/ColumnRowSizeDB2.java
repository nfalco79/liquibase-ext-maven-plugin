/*
 * Copyright 2022 Laura Cameran
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

import org.apache.maven.shared.utils.StringUtils;

import com.github.nfalco79.maven.liquibase.plugin.util.StringUtil;
import com.github.nfalco79.maven.liquibase.plugin.validator.IColumnRowSize;
import com.github.nfalco79.maven.liquibase.plugin.validator.listener.ColumnInfo;

/**
 * Class extractor of columns size for DB2.
 * 
 * @author Laura Cameran
 */
public class ColumnRowSizeDB2 implements IColumnRowSize {

    /**
     * Liquibase types.
     */
    public enum Type {
        TINYINT, //
        BOOLEAN, //
        SMALLINT, // TINYINT|BOOLEAN|SMALLINT Liquibase -> SMALLINT DB2
        INTEGER, //
        BIGINT, //
        FLOAT, //
        DOUBLE, //
        DECIMAL, //
        NUMBER, // DECIMAL|NUMBER Liquibase -> DECIMAL|DEC|NUMERIC|NUM DB2
        CHAR, //
        NCHAR, //
        VARCHAR, //
        NVARCHAR, //
        DATE, //
        TIME, //
        DATETIME, //
        TIMESTAMP, // DATETIME|TIMESTAMP Liquibase -> TIMESTAMP DB2
        BLOB, //
        CLOB, //
        NCLOB;
        // currency Liquibase -> DECIMAL(19, 4) DB2
        // uuid Liquibase -> CHAR(36) DB2
    }

    @Override
    public int getRowSize(ColumnInfo column) { // NOSONAR
        // The following values and calculation are valid for DB2 11.5 and are based on: "Table 4. Byte Counts of Columns by Data Type"
        // https://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0000927.html
        String colType = column.getType();
        if (StringUtils.isEmpty(colType)) {
            return 0;
        }
        try {
            Type t = Type.valueOf(colType.toUpperCase());
            String length = column.getLength();
            switch (t) {
                case TINYINT: //
                case BOOLEAN: //
                case SMALLINT:
                    return 4;
                case INTEGER:
                    return 6;
                case BIGINT:
                    return 10;
                case FLOAT: //
                case DOUBLE:
                    return 10;
                case NUMBER: //
                case DECIMAL:
                    if (!StringUtils.isEmpty(length)) {
                        return (StringUtil.getPrecision(length) / 2) + 3;
                    } else { // Default 5
                        return 5;
                    }
                case NCHAR: //
                case CHAR:
                    if (!StringUtils.isEmpty(length)) {
                        return Integer.parseInt(length) + 2;
                    } else { // Default 1
                        return 3;
                    }
                case NVARCHAR: //
                case VARCHAR:
                    if (!StringUtils.isEmpty(length)) {
                        if (Integer.parseInt(length) > 24) {
                            // A subset of the varying length string columns is stored as large object (LOB) data outside of the data row.
                            // The table column in the base row is replaced by a descriptor that is 24 bytes in size.
                            return 29;
                        } else {
                            return Integer.parseInt(length) + 5;
                        }
                    } else { // Default 1
                        return 6;
                    }
                case DATE:
                    return 6;
                case TIME:
                    return 5;
                case DATETIME: //
                case TIMESTAMP:
                    if (!StringUtils.isEmpty(length)) {
                        return ((Integer.parseInt(length) + 1) / 2) + 9;
                    } else { // Default 6
                        return 12;
                    }
                case BLOB: //
                case CLOB: //
                case NCLOB: // NO INLINE LENGTH SPECIFIED
                    int lengthValue = 1024;
                    if (!StringUtils.isEmpty(length)) {
                        lengthValue = Integer.parseInt(length);
                    }
                    if (lengthValue <= 1024) {
                        return 73;
                    } else if (lengthValue <= 8192) {
                        return 97;
                    } else if (lengthValue <= 65536) {
                        return 121;
                    } else if (lengthValue <= 524000) {
                        return 145;
                    } else if (lengthValue <= 4190000) {
                        return 169;
                    } else if (lengthValue <= 134000000) {
                        return 201;
                    } else if (lengthValue <= 536000000) {
                        return 225;
                    } else if (lengthValue <= 1070000000) {
                        return 257;
                    } else if (lengthValue <= 1470000000) {
                        return 281;
                    } else if (lengthValue <= 2147483647) {
                        return 317;
                    }
            }
        } catch (IllegalArgumentException e) {
            // No enum Type find with the given column type name
        }
        return 0;
    }

    @Override
    public int getSize(ColumnInfo column) {
        String colType = column.getType();
        if (StringUtils.isEmpty(colType)) {
            return 0;
        }
        if ("VARCHAR".equalsIgnoreCase(colType) || "NVARCHAR".equalsIgnoreCase(colType)) {
            String length = column.getLength();
            if (!StringUtils.isEmpty(length)) {
                // DB2 stores an additional 2-byte length field for each varying-length column
                return Integer.parseInt(length) + 3;
            } else { // Default 1
                return 4;
            }
        } // Any other case follows row size calculation
        return getRowSize(column);
    }
}
