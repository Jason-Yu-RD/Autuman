package com.jason.autuman.service.impl;

import com.jason.autuman.autos.AutoJavaFile;
import com.jason.autuman.autos.AutoMapperFile;
import com.jason.autuman.dao.TableInfoDao;
import com.jason.autuman.domain.po.ColumnInfo;
import com.jason.autuman.exception.AppException;
import com.jason.autuman.service.TableInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by yuchangcun on 2016/8/29.
 */
public class TableInfoServiceImpl implements TableInfoService {

    private static final Logger logger = LoggerFactory.getLogger(TableInfoServiceImpl.class);

    private TableInfoDao tableInfoDao;

    /*数据库类型到java类型的映射*/
    private Map<String, String> javaTypeMap;

    /*数据库类型到import的映射*/
    private Map<String, String> importStrMap;

    /**
     * 通过表名称查询表信息
     * @param tableName
     * @return
     */
    @Override
    public List<ColumnInfo> selectCloumnListByTableName(String tableName) throws AppException {
        try{
            return tableInfoDao.selectCloumnListByTableName(tableName);
        }catch(Exception e){
            logger.error("selectTableInfoByName error , tableName:{}",tableName,e);
            throw AppException.dbAppException();
        }
    }

    @Override
    public void createJavaFile(String tableName) {
        try{
            List<ColumnInfo> dataList = this.selectCloumnListByTableName(tableName);
            AutoJavaFile autoJavaFile = new AutoJavaFile();
            autoJavaFile.setColumnInfoList(dataList);
            autoJavaFile.setImportStrMap(importStrMap);
            autoJavaFile.setJavaTypeMap(javaTypeMap);
            autoJavaFile.createJavaFile("E:\\Data\\java",tableName,"com.jason.campusinn");
        }catch(Exception e){
            logger.error("",e);
        }
    }

    @Override
    public void createMapperFile(String tableName) {
        try {
            List<ColumnInfo> dataList = this.selectCloumnListByTableName(tableName);
            AutoMapperFile autoMapperFile = new AutoMapperFile();
            autoMapperFile.setDataList(dataList);
            autoMapperFile.setJavaTypeMap(javaTypeMap);
            autoMapperFile.createMapperFile("E:\\Data\\java",tableName);
        }catch (Exception e){
            logger.error("",e);
        }
    }


    public TableInfoDao getTableInfoDao() {
        return tableInfoDao;
    }

    public void setTableInfoDao(TableInfoDao tableInfoDao) {
        this.tableInfoDao = tableInfoDao;
    }

    public void setJavaTypeMap(Map<String, String> javaTypeMap) {
        this.javaTypeMap = javaTypeMap;
    }

    public void setImportStrMap(Map<String, String> importStrMap) {
        this.importStrMap = importStrMap;
    }
}
