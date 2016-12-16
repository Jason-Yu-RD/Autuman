package com.jason.autuman.dao;

import com.jason.autuman.domain.po.ColumnInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yuchangcun on 2016/8/29.
 */
public interface TableInfoDao {

    /**
     * 通过表名称查询表信息
     * @param tableName
     * @return
     */
    public List<ColumnInfo> selectCloumnListByTableName(@Param("tableName")String tableName);

}
