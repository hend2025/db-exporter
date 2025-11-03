package com.medicare.exporter;

import com.medicare.exporter.model.DataDictionary;
import com.medicare.exporter.model.Table;
import com.medicare.exporter.model.TableColumn;
import com.medicare.exporter.model.TableIndex;
import com.medicare.exporter.service.DatabaseService;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WordDocumentGenerator {

    private static final String DB_HOST = "127.0.0.1";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "medicare_test";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    private static final String TARGET_SCHEMA = "medicare_test";

    private static final String HEADER_COLOR = "D9D9D9";

    public static void generateReportFromTables(String templateName, String outputFileName, String searchText, List<Table> tables) {
        InputStream is = null;
        XWPFDocument document = null;
        FileOutputStream out = null;

        try {
            is = WordDocumentGenerator.class.getClassLoader().getResourceAsStream(templateName);
            if (is == null) {
                System.err.println("错误：在 resources 目录中未找到模板文件: " + templateName);
                return;
            }
            document = new XWPFDocument(is);

            XWPFParagraph targetParagraph = findParagraphByText(document, searchText);
            if (targetParagraph == null) {
                System.err.println("错误：未在文档中找到目标章节：" + searchText);
                return;
            }

            BigInteger parentNumId = targetParagraph.getNumID();
            if (parentNumId == null) {
                System.out.println("警告：父段落 '" + searchText + "' 没有关联的编号方案(NumID)。子章节将不会自动编号。");
            }

            Map<String, List<Table>> moduleMap = groupTablesByModule(tables);
            XmlCursor cursor = getInsertionCursor(targetParagraph);

            for (Map.Entry<String, List<Table>> entry : moduleMap.entrySet()) {
                String moduleName = entry.getKey();
                List<Table> moduleTables = entry.getValue();

                System.out.println("正在插入模块: " + moduleName);
                XWPFParagraph modulePara = document.insertNewParagraph(cursor);
                formatAsHeading(modulePara, moduleName, 3, parentNumId);
                cursor.toNextToken();

                for (Table tableData : moduleTables) {
                    String tableTitle = formatTableTitle(tableData);
                    System.out.println("  > 正在插入表: " + tableTitle);

                    XWPFParagraph tableHeadingPara = document.insertNewParagraph(cursor);
                    formatAsHeading(tableHeadingPara, tableTitle, 4, parentNumId);
                    cursor.toNextToken();

                    XWPFTable xwpfTable = document.insertNewTbl(cursor);
                    populateTable(xwpfTable, tableData);
                    cursor.toNextToken();

                    if (tableData.getIndexes() != null && !tableData.getIndexes().isEmpty()) {
                        // 【修改】仅在存在索引表时，才在表结构和索引表之间添加空行
                        document.insertNewParagraph(cursor);
                        cursor.toNextToken();

                        System.out.println("    > 正在插入索引...");
                        XWPFTable indexTable = document.insertNewTbl(cursor);
                        populateIndexTable(indexTable, tableData);
                        cursor.toNextToken();

                    }
                }
            }
            cursor.dispose();

            out = new FileOutputStream(outputFileName);
            document.write(out);
            System.out.println("\n✅ [第1步] 表结构插入成功! 保存为：" + outputFileName);

        } catch (Exception e) {
            System.err.println("处理 Word 文档时发生错误 (表结构): " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(out, document, is);
        }
    }

    public static void generateDataDictionaries(String inputFileName, String outputFileName, String searchText, List<DataDictionary> dictionaries) {
        InputStream is = null;
        XWPFDocument document = null;
        FileOutputStream out = null;

        try {
            is = new FileInputStream(inputFileName);
            document = new XWPFDocument(is);

            XWPFParagraph targetParagraph = findParagraphByText(document, searchText);
            if (targetParagraph == null) {
                System.err.println("错误：未在文档中找到目标章节：" + searchText);
                return;
            }

            BigInteger parentNumId = targetParagraph.getNumID();
            if (parentNumId == null) {
                System.out.println("警告：父段落 '" + searchText + "' 没有关联的编号方案(NumID)。子章节将不会自动编号。");
            }

            Map<String, List<DataDictionary>> dicTypeMap = groupDictionariesByType(dictionaries);
            XmlCursor cursor = getInsertionCursor(targetParagraph);

            for (Map.Entry<String, List<DataDictionary>> entry : dicTypeMap.entrySet()) {
                String dicTypeName = entry.getKey();
                List<DataDictionary> typeDictionaries = entry.getValue();

                String dicTypeCode = "";
                if (!typeDictionaries.isEmpty()) {
                    dicTypeCode = typeDictionaries.get(0).getDicTypeCode();
                }

                String fullTitle = formatDictionaryTitle(dicTypeName, dicTypeCode);

                System.out.println("正在插入数据字典: " + fullTitle);
                XWPFParagraph typePara = document.insertNewParagraph(cursor);
                formatAsHeading(typePara, fullTitle, 3, parentNumId);
                cursor.toNextToken();

                XWPFTable dicTable = document.insertNewTbl(cursor);
                populateDictionaryTable(dicTable, typeDictionaries);
                cursor.toNextToken();

            }

            cursor.dispose();

            out = new FileOutputStream(outputFileName);
            document.write(out);
            System.out.println("\n✅ [第2步] 数据字典插入成功! 最终文件保存为：" + outputFileName);

        } catch (Exception e) {
            System.err.println("处理 Word 文档时发生错误 (数据字典): " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(out, document, is);
        }
    }


    public static void main(String[] args) {
        String templateName = "数据库设计说明书-示例.docx";
        String outputName = "数据库设计-"+System.currentTimeMillis()+".docx";

        DatabaseService dbService = new DatabaseService(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD);

        System.out.println("--- 正在从数据库导出表结构 ---");
        List<Table> tables = dbService.exportTables(TARGET_SCHEMA);
        if (tables == null || tables.isEmpty()) {
            System.err.println("未能从数据库获取任何表数据，程序终止。");
            return;
        }
        System.out.println("--- 成功导出 " + tables.size() + " 张表，正在生成文档 ---");
        generateReportFromTables(templateName, outputName, "物理结构设计", tables);

        System.out.println("--- 正在从数据库导出数据字典 ---");
        List<DataDictionary> dictionaries = dbService.exportDataDictionaries();
        if (dictionaries == null || dictionaries.isEmpty()) {
            System.err.println("未能从数据库获取任何数据字典数据，跳过数据字典步骤。");
        } else {
            System.out.println("--- 成功导出 " + dictionaries.size() + " 条字典数据，正在更新文档 ---");
            generateDataDictionaries(outputName, outputName, "数据字典设计", dictionaries);
        }

        System.out.println("\n--- 所有操作执行完毕 ---");
    }

    private static void populateTable(XWPFTable xwpfTable, Table tableData) {
        xwpfTable.setWidth("100%");
        String[] headers = {"字段名", "数据类型", "长度", "主键", "可空", "注释"};
        XWPFTableRow headerRow = xwpfTable.getRow(0);

        setHeaderCellFormat(headerRow.getCell(0), headers[0]);
        for (int i = 1; i < headers.length; i++) {
            setHeaderCellFormat(headerRow.addNewTableCell(), headers[i]);
        }

        for (TableColumn column : tableData.getColumns()) {
            XWPFTableRow dataRow = xwpfTable.createRow();
            setDataCellFormat(dataRow.getCell(0), column.getColumnName() != null ? column.getColumnName() : "");
            setDataCellFormat(dataRow.getCell(1), column.getDataType() != null ? column.getDataType() : "", ParagraphAlignment.CENTER);
            setDataCellFormat(dataRow.getCell(2), getColumnLength(column), ParagraphAlignment.CENTER);
            setDataCellFormat(dataRow.getCell(3), "PRI".equalsIgnoreCase(column.getColumnKey()) ? "是" : "否", ParagraphAlignment.CENTER);
            setDataCellFormat(dataRow.getCell(4), "NO".equalsIgnoreCase(column.getIsNullable()) ? "否" : "是", ParagraphAlignment.CENTER);
            setDataCellFormat(dataRow.getCell(5), column.getColumnComment() != null ? column.getColumnComment() : "");
        }
    }

    private static void populateIndexTable(XWPFTable xwpfTable, Table tableData) {
        xwpfTable.setWidth("100%");
        String[] headers = {"索引类型", "索引名称", "字段名"};
        XWPFTableRow headerRow = xwpfTable.getRow(0);

        setHeaderCellFormat(headerRow.getCell(0), headers[0]);
        for (int i = 1; i < headers.length; i++) {
            setHeaderCellFormat(headerRow.addNewTableCell(), headers[i]);
        }

        List<TableIndex> sortedIndexes = new ArrayList<>(tableData.getIndexes());
        sortedIndexes.sort((i1, i2) -> {
            boolean isPri1 = "PRIMARY KEY".equals(i1.getIndexType());
            boolean isPri2 = "PRIMARY KEY".equals(i2.getIndexType());
            if (isPri1 && !isPri2) return -1;
            if (!isPri1 && isPri2) return 1;
            return i1.getIndexName().compareTo(i2.getIndexName());
        });

        for (TableIndex index : sortedIndexes) {
            XWPFTableRow dataRow = xwpfTable.createRow();
            setDataCellFormat(dataRow.getCell(0), index.getIndexType() != null ? index.getIndexType() : "");
            setDataCellFormat(dataRow.getCell(1), index.getIndexName() != null ? index.getIndexName() : "");
            setDataCellFormat(dataRow.getCell(2), index.getColumnName() != null ? index.getColumnName() : "");
        }
    }

    private static void populateDictionaryTable(XWPFTable table, List<DataDictionary> dictionaries) {
        table.setWidth("100%");
        String[] headers = {"字典值代码", "字典值名称"};
        XWPFTableRow headerRow = table.getRow(0);

        setHeaderCellFormat(headerRow.getCell(0), headers[0]);
        for (int i = 1; i < headers.length; i++) {
            setHeaderCellFormat(headerRow.addNewTableCell(), headers[i]);
        }

        for (DataDictionary dic : dictionaries) {
            XWPFTableRow row = table.createRow();
            setDataCellFormat(row.getCell(0), dic.getDicCode() != null ? dic.getDicCode() : "");
            setDataCellFormat(row.getCell(1), dic.getDicName() != null ? dic.getDicName() : "");
        }
    }

    private static String formatDictionaryTitle(String dicTypeName, String dicTypeCode) {
        if (dicTypeCode != null && !dicTypeCode.trim().isEmpty()) {
            return dicTypeName + "【" + dicTypeCode + "】";
        }
        return dicTypeName;
    }

    private static Map<String, List<DataDictionary>> groupDictionariesByType(List<DataDictionary> dictionaries) {
        Map<String, List<DataDictionary>> dicTypeMap = new LinkedHashMap<>();
        for (DataDictionary dic : dictionaries) {
            String dicTypeName = dic.getDicTypeName();
            if (dicTypeName == null || dicTypeName.trim().isEmpty()) {
                dicTypeName = "未分类字典";
            }
            dicTypeMap.computeIfAbsent(dicTypeName, k -> new ArrayList<>()).add(dic);
        }
        return dicTypeMap;
    }

    private static void setHeaderCellFormat(XWPFTableCell cell, String text) {
        cell.setColor(HEADER_COLOR);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        XWPFParagraph para = cell.getParagraphs().get(0);
        if (para == null) {
            para = cell.addParagraph();
        }
        para.setAlignment(ParagraphAlignment.CENTER);

        while(para.getRuns().size() > 0) {
            para.removeRun(0);
        }
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
    }

    private static void setDataCellFormat(XWPFTableCell cell, String text) {
        setDataCellFormat(cell, text, ParagraphAlignment.LEFT);
    }

    private static void setDataCellFormat(XWPFTableCell cell, String text, ParagraphAlignment alignment) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        XWPFParagraph para = cell.getParagraphs().get(0);
        if (para == null) {
            para = cell.addParagraph();
        }
        para.setAlignment(alignment);

        while(para.getRuns().size() > 0) {
            para.removeRun(0);
        }
        XWPFRun run = para.createRun();
        run.setText(text);
    }


    private static Map<String, List<Table>> groupTablesByModule(List<Table> tables) {
        Map<String, List<Table>> moduleMap = new LinkedHashMap<>();
        for (Table table : tables) {
            String moduleName = table.getModuleName();
            if (moduleName == null || moduleName.trim().isEmpty()) {
                moduleName = "未分类模块";
            }
            moduleMap.computeIfAbsent(moduleName, k -> new ArrayList<>()).add(table);
        }
        return moduleMap;
    }

    private static XWPFParagraph findParagraphByText(XWPFDocument document, String searchText) {
        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph p = (XWPFParagraph) element;
                if (p.getText() != null && p.getText().trim().equals(searchText)) {
                    return p;
                }
            }
        }
        return null;
    }

    private static String formatTableTitle(Table table) {
        String comment = table.getTableComment();
        return table.getTableName() + (comment != null && !comment.trim().isEmpty() ? " (" + comment + ")" : "");
    }

    private static String getColumnLength(TableColumn column) {
        if (column.getCharLen() != null && column.getCharLen() > 0) {
            return String.valueOf(column.getCharLen());
        }
        if (column.getNumericPrecision() != null && column.getNumericPrecision() > 0) {
            if (column.getNumericScale() != null && column.getNumericScale() > 0) {
                return column.getNumericPrecision() + "," + column.getNumericScale();
            }
            return String.valueOf(column.getNumericPrecision());
        }
        return "-";
    }

    private static org.apache.xmlbeans.XmlCursor getInsertionCursor(XWPFParagraph paragraph) {
        org.apache.xmlbeans.XmlCursor cursor = paragraph.getCTP().newCursor();
        cursor.toEndToken();
        cursor.toNextToken();
        return cursor;
    }

    private static void formatAsHeading(XWPFParagraph paragraph, String text, int level, BigInteger numId) {
        String styleId;
        if (level == 1) {
            styleId = "标题 1";
        } else {
            styleId = String.valueOf(level + 1);
        }

        paragraph.setStyle(styleId);

        if (paragraph.getCTP() == null) {
            System.err.println("错误：新创建的段落 " + text + " 内部 XML 对象 (CTP) 为 null。跳过此标题。");
            return;
        }

        CTPPr pPr = paragraph.getCTP().getPPr();
        if (pPr == null) {
            pPr = paragraph.getCTP().addNewPPr();
        }

        BigInteger outlineLvlValue = BigInteger.valueOf(level - 1);
        pPr.addNewOutlineLvl().setVal(outlineLvlValue);

        if (numId != null) {
            paragraph.setNumID(numId);
            paragraph.setNumILvl(BigInteger.valueOf(level - 1));
        }

        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }

    private static void closeResources(FileOutputStream out, XWPFDocument doc, InputStream is) {
        try { if (out != null) out.close(); } catch (IOException e) { e.printStackTrace(); }
        try { if (doc != null) doc.close(); } catch (IOException e) { e.printStackTrace(); }
        try { if (is != null) is.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}