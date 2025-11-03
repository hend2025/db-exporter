package com.medicare.exporter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息实体类
 */
public class Table {
    private Integer tableId;
    private String tableSchema;
    private String tableName;
    private String tableComment;
    private String moduleName;
    private String zoneColumn;
    private List<TableColumn> columns = new ArrayList<>();
    private List<TableIndex> indexes = new ArrayList<>();

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
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

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getZoneColumn() {
        return zoneColumn;
    }

    public void setZoneColumn(String zoneColumn) {
        this.zoneColumn = zoneColumn;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public List<TableIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<TableIndex> indexes) {
        this.indexes = indexes;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableId=" + tableId +
                ", tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableComment='" + tableComment + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", zoneColumn='" + zoneColumn + '\'' +
                ", columns=" + columns +
                ", indexes=" + indexes +
                '}';
    }
}

