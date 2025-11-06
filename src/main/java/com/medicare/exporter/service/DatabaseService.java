package com.medicare.exporter.service;

import com.medicare.exporter.model.DataDictionary;
import com.medicare.exporter.model.Table;
import com.medicare.exporter.model.TableColumn;
import com.medicare.exporter.model.TableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库服务类，负责从数据库导出表结构信息
 */
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private String url;
    private String username;
    private String password;

    public DatabaseService(String host, String port, String database, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                   "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        this.username = username;
        this.password = password;
    }

    /**
     * 导出指定schema的所有表结构
     */
    public List<Table> exportTables(String tableSchema) {
        List<Table> tables = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            logger.info("数据库连接成功: {}", url);

            // 查询表信息
            String tableSql = "SELECT TABLE_ID, TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT, " +
                            "MODULE_NAME, ZONE_COLUMN FROM t_table WHERE TABLE_SCHEMA = ? " +
                            "ORDER BY MODULE_XH, TABLE_NAME";
            
            pstmt = conn.prepareStatement(tableSql);
            pstmt.setString(1, tableSchema);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Table table = new Table();
                table.setTableId(rs.getInt("TABLE_ID"));
                table.setTableSchema(rs.getString("TABLE_SCHEMA"));
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setTableComment(rs.getString("TABLE_COMMENT"));
                table.setModuleName(rs.getString("MODULE_NAME"));
                table.setZoneColumn(rs.getString("ZONE_COLUMN"));

                // 查询该表的字段信息
                List<TableColumn> columns = getTableColumns(conn, tableSchema, table.getTableName());
                table.setColumns(columns);

                // 查询该表的索引信息
                List<TableIndex> indexes = getTableIndexes(conn, tableSchema, table.getTableName());
                table.setIndexes(indexes);

                tables.add(table);
                logger.info("已加载表: {} (模块: {})", table.getTableName(), table.getModuleName());
            }

            logger.info("共导出 {} 张表", tables.size());

        } catch (SQLException e) {
            logger.error("数据库操作失败", e);
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tables;
    }

    /**
     * 获取表的字段信息
     */
    private List<TableColumn> getTableColumns(Connection conn, String tableSchema, String tableName) 
            throws SQLException {
        List<TableColumn> columns = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String columnSql = "SELECT TABLE_COLUMN_ID, TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION, " +
                             "COLUMN_NAME, COLUMN_COMMENT, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE, " +
                             "COLUMN_DEFAULT, DATA_TYPE, CHAR_LEN, NUMERIC_PRECISION, NUMERIC_SCALE, " +
                             "CHARACTER_SET_NAME, COLLATION_NAME, EXTRA " +
                             "FROM t_table_column " +
                             "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                             "ORDER BY ORDINAL_POSITION";

            pstmt = conn.prepareStatement(columnSql);
            pstmt.setString(1, tableSchema);
            pstmt.setString(2, tableName);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TableColumn column = new TableColumn();
                column.setTableColumnId(rs.getInt("TABLE_COLUMN_ID"));
                column.setTableSchema(rs.getString("TABLE_SCHEMA"));
                column.setTableName(rs.getString("TABLE_NAME"));
                column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
                column.setColumnName(rs.getString("COLUMN_NAME"));
                column.setColumnComment(rs.getString("COLUMN_COMMENT"));
                column.setColumnType(rs.getString("COLUMN_TYPE"));
                column.setColumnKey(rs.getString("COLUMN_KEY"));
                column.setIsNullable(rs.getString("IS_NULLABLE"));
                column.setColumnDefault(rs.getString("COLUMN_DEFAULT"));
                column.setDataType(rs.getString("DATA_TYPE"));
                column.setCharLen(rs.getObject("CHAR_LEN") != null ? rs.getInt("CHAR_LEN") : null);
                column.setNumericPrecision(rs.getObject("NUMERIC_PRECISION") != null ? rs.getInt("NUMERIC_PRECISION") : null);
                column.setNumericScale(rs.getObject("NUMERIC_SCALE") != null ? rs.getInt("NUMERIC_SCALE") : null);
                column.setCharacterSetName(rs.getString("CHARACTER_SET_NAME"));
                column.setCollationName(rs.getString("COLLATION_NAME"));
                column.setExtra(rs.getString("EXTRA"));

                columns.add(column);
            }

        } finally {
            closeResources(rs, pstmt, null);
        }

        return columns;
    }

    /**
     * 获取表的索引信息
     */
    private List<TableIndex> getTableIndexes(Connection conn, String tableSchema, String tableName) 
            throws SQLException {
        List<TableIndex> indexes = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 从t_table_index表获取索引信息
            String indexSql = "SELECT INDEX_NAME, NON_UNIQUE, COLUMN_NAME " +
                            "FROM t_table_index " +
                            "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                            "ORDER BY INDEX_NAME, SEQ_IN_INDEX";

            pstmt = conn.prepareStatement(indexSql);
            pstmt.setString(1, tableSchema);
            pstmt.setString(2, tableName);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TableIndex index = new TableIndex();
                String indexName = rs.getString("INDEX_NAME");
                String nonUnique = rs.getString("NON_UNIQUE");
                
                // 确定索引类型
                String indexType;
                if ("PRIMARY".equalsIgnoreCase(indexName)) {
                    indexType = "PRIMARY KEY";
                } else if ("0".equals(nonUnique)) {
                    indexType = "UNIQUE";
                } else {
                    indexType = "INDEX";
                }
                
                index.setIndexType(indexType);
                index.setIndexName(indexName);
                index.setColumnName(rs.getString("COLUMN_NAME"));
                
                indexes.add(index);
            }

        } finally {
            closeResources(rs, pstmt, null);
        }

        return indexes;
    }

    /**
     * 导出数据字典
     */
    public List<DataDictionary> exportDataDictionaries() {
        List<DataDictionary> dictionaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            logger.info("数据库连接成功，开始导出数据字典");

            // 查询数据字典信息，按字典类型和代码排序，使用DISTINCT去重
            String dicSql = "SELECT DISTINCT DIC_CODE, DIC_NAME, DIC_TYPE_CODE, DIC_TYPE_NAME " +
                           "FROM t_table_dic " +
                           "ORDER BY DIC_TYPE_CODE, DIC_CODE";
            
            pstmt = conn.prepareStatement(dicSql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DataDictionary dic = new DataDictionary();
                dic.setDicCode(rs.getString("DIC_CODE"));
                dic.setDicName(rs.getString("DIC_NAME"));
                dic.setDicTypeCode(rs.getString("DIC_TYPE_CODE"));
                dic.setDicTypeName(rs.getString("DIC_TYPE_NAME"));
                
                dictionaries.add(dic);
            }

            logger.info("共导出 {} 条数据字典记录", dictionaries.size());

        } catch (SQLException e) {
            logger.error("导出数据字典失败", e);
            throw new RuntimeException("导出数据字典失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return dictionaries;
    }

    /**
     * 关闭数据库资源
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.warn("关闭ResultSet失败", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.warn("关闭Statement失败", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("关闭Connection失败", e);
            }
        }
    }
}
