package com.medicare.exporter.model;

/**
 * 表索引信息
 */
public class TableIndex {
    private String indexType;      // 索引类型（PRIMARY、UNIQUE、INDEX等）
    private String indexName;      // 索引名称
    private String columnName;     // 字段名

    public TableIndex() {
    }

    public TableIndex(String indexType, String indexName, String columnName) {
        this.indexType = indexType;
        this.indexName = indexName;
        this.columnName = columnName;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "TableIndex{" +
                "indexType='" + indexType + '\'' +
                ", indexName='" + indexName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}

