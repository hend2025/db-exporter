package com.medicare.exporter.model;

/**
 * 表字段信息实体类
 */
public class TableColumn {
    private Integer tableColumnId;
    private String tableSchema;
    private String tableName;
    private Integer ordinalPosition;
    private String columnName;
    private String columnComment;
    private String columnType;
    private String columnKey;
    private String isNullable;
    private String columnDefault;
    private String dataType;
    private Integer charLen;
    private Integer numericPrecision;
    private Integer numericScale;
    private String characterSetName;
    private String collationName;
    private String extra;

    public Integer getTableColumnId() {
        return tableColumnId;
    }

    public void setTableColumnId(Integer tableColumnId) {
        this.tableColumnId = tableColumnId;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getCharLen() {
        return charLen;
    }

    public void setCharLen(Integer charLen) {
        this.charLen = charLen;
    }

    public Integer getNumericPrecision() {
        return numericPrecision;
    }

    public void setNumericPrecision(Integer numericPrecision) {
        this.numericPrecision = numericPrecision;
    }

    public Integer getNumericScale() {
        return numericScale;
    }

    public void setNumericScale(Integer numericScale) {
        this.numericScale = numericScale;
    }

    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    public String getCollationName() {
        return collationName;
    }

    public void setCollationName(String collationName) {
        this.collationName = collationName;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "tableColumnId=" + tableColumnId +
                ", tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", ordinalPosition=" + ordinalPosition +
                ", columnName='" + columnName + '\'' +
                ", columnComment='" + columnComment + '\'' +
                ", columnType='" + columnType + '\'' +
                ", columnKey='" + columnKey + '\'' +
                ", isNullable='" + isNullable + '\'' +
                ", columnDefault='" + columnDefault + '\'' +
                ", dataType='" + dataType + '\'' +
                ", charLen=" + charLen +
                ", numericPrecision=" + numericPrecision +
                ", numericScale=" + numericScale +
                ", characterSetName='" + characterSetName + '\'' +
                ", collationName='" + collationName + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}

