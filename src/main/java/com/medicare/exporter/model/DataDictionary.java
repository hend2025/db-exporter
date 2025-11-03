package com.medicare.exporter.model;

/**
 * 数据字典实体类
 */
public class DataDictionary {
    private String dicId;          // 数据字典ID
    private String dicCode;        // 字典值代码
    private String dicName;        // 字典值名称
    private String dicTypeCode;    // 字典类型代码
    private String dicTypeName;    // 字典类型名称

    public DataDictionary() {
    }

    public DataDictionary(String dicId, String dicCode, String dicName, String dicTypeCode, String dicTypeName) {
        this.dicId = dicId;
        this.dicCode = dicCode;
        this.dicName = dicName;
        this.dicTypeCode = dicTypeCode;
        this.dicTypeName = dicTypeName;
    }

    public String getDicId() {
        return dicId;
    }

    public void setDicId(String dicId) {
        this.dicId = dicId;
    }

    public String getDicCode() {
        return dicCode;
    }

    public void setDicCode(String dicCode) {
        this.dicCode = dicCode;
    }

    public String getDicName() {
        return dicName;
    }

    public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    public String getDicTypeCode() {
        return dicTypeCode;
    }

    public void setDicTypeCode(String dicTypeCode) {
        this.dicTypeCode = dicTypeCode;
    }

    public String getDicTypeName() {
        return dicTypeName;
    }

    public void setDicTypeName(String dicTypeName) {
        this.dicTypeName = dicTypeName;
    }

    @Override
    public String toString() {
        return "DataDictionary{" +
                "dicId='" + dicId + '\'' +
                ", dicCode='" + dicCode + '\'' +
                ", dicName='" + dicName + '\'' +
                ", dicTypeCode='" + dicTypeCode + '\'' +
                ", dicTypeName='" + dicTypeName + '\'' +
                '}';
    }
}

