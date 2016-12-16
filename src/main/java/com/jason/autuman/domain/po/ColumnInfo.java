package com.jason.autuman.domain.po;

/**
 * Created by yuchangcun on 2016/8/29.
 */
public class ColumnInfo {

    /*表名*/
    private String tableName;

    /*列名*/
    private String columnName;

    /*列的排序值*/
    private Integer ordinalPosition;

    /*数据类型*/
    private String dataType;

    /*备注*/
    private String columnComment;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
}
