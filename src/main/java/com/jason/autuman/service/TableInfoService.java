package com.jason.autuman.service;

import com.jason.autuman.domain.po.ColumnInfo;
import com.jason.autuman.exception.AppException;

import java.util.List;

/**
 * Created by yuchangcun on 2016/8/29.
 */
public interface TableInfoService {

    /**
     * 通过表名称查询所有列信息
     * @param tableName
     * @return
     */
    public List<ColumnInfo> selectCloumnListByTableName(String tableName) throws AppException;

    public void createJavaFile(String tableName);

    public void createMapperFile(String tableName);
}
