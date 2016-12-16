package com.jason.autuman.autos;


import com.jason.autuman.domain.po.ColumnInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuchangcun on 2016/8/29.
 */
public class AutoJavaFile implements AutoFile {

    private static final Logger logger = LoggerFactory.getLogger(AutoJavaFile.class);

    /*文件路径*/
    private String filePath;

    /*文件名*/
    private String fileName;

    /*生成文件*/
    private File file;

    /*输出流*/
    private OutputStream outputStream;

    /*列集合*/
    private List<ColumnInfo> columnInfoList;

    /*数据库类型到java类型的映射*/
    private Map<String, String> javaTypeMap;

    /*数据库类型到import的映射*/
    private Map<String, String> importStrMap;

    public void createJavaFile(String filePath,String tableName,String packageName) throws IOException{
        this.filePath = filePath;
        this.fileName = formatUperName(formatPropertyName(tableName));
        this.file = new File(filePath+File.separator+fileName+".java");
        if(file.exists()){
            file.delete();
        }
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        this.outputStream = FileUtils.openOutputStream(this.file);
        createPackageLine(packageName);
        createImportLines();
        createClassComments();
        createClassDomian(this.fileName);
    }

    /**
     * 生成package头
     *
     * @param packageName
     * @throws IOException
     */
    public void createPackageLine(String packageName) {
        write("package ");
        write(packageName);
        write(";\r\n\r\n");
    }

    /**
     * 生成导入信息
     */
    public void createImportLines() {

        Map<String, ColumnInfo> uniqueDataType = new HashMap<String, ColumnInfo>(columnInfoList.size());

        for (ColumnInfo item : columnInfoList) {
            if (null == item || StringUtils.isBlank(item.getColumnName()) || StringUtils.isBlank(item.getDataType())) {
                continue;
            }
            /*类型去重*/
            uniqueDataType.put(item.getDataType(), item);
        }
        for (ColumnInfo uniqueType : uniqueDataType.values()) {
            createSingleImportLine(uniqueType);
        }
        write("\r\n\r\n");
    }

    /**
     * 生成单行import
     *
     * @param column
     */
    public void createSingleImportLine(ColumnInfo column) {
        String importStr = importStrMap.get(column.getDataType());
        if (StringUtils.isBlank(importStr)) {
            return;
        }
        write("import ");
        write(importStr);
        write(";\r\n");
    }

    /**
     * 生成注释
     *
     * @param content
     */
    public void createComments(String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        content.replace("\r\n", "\r\n\t*");
        write("\t/**\r\n\t*");
        write(content);
        write("\r\n\t*/\r\n");
    }

    /**
     * 生成类注释
     */
    public void createClassComments(){
        write("\r\n/**\r\n");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
        write("* Created by JasonAuto on "+format.format(new Date())+".\r\n");
        write("*/\r\n");
    }

    /**
     * 生成class实体
     */
    public void createClassDomian(String className) {
        write("public class ");
        write(className);
        write(" {\r\n");
        createProperties();
        createMethods();
        write("}\r\n");
    }

    /**
     * 生成所有属性
     */
    public void createProperties() {
        for (ColumnInfo item : columnInfoList) {
            if (item == null) {
                continue;
            }
            createSingleProperty(item);
        }
    }

    /**
     * 生成单行属性
     */
    public void createSingleProperty(ColumnInfo item) {
        if (StringUtils.isBlank(item.getColumnName()) || StringUtils.isBlank(item.getDataType())) {
            return;
        }
        String javaType = javaTypeMap.get(item.getDataType());
        if (StringUtils.isBlank(javaType)) {
            return;
        }
        createComments(item.getColumnComment());
        write("\tprivate ");
        write(javaType+" ");
        write(formatPropertyName(item.getColumnName()));
        write(";\r\n\r\n");

    }

    /**
     * 生成方法
     */
    public void createMethods() {
        for(ColumnInfo item : columnInfoList){
            if(null == item || StringUtils.isBlank(item.getColumnName()) || StringUtils.isBlank(item.getDataType())){
                continue;
            }
            createGetMethod(item);
            createSetMethod(item);
        }
    }

    /**
     * 生成单个Get方法
     */
    public void createGetMethod(ColumnInfo item){
        String formatName = formatPropertyName(item.getColumnName());

        write("\r\n\tpublic "+javaTypeMap.get(item.getDataType())+" get"+formatUperName(formatName)+"() {\r\n");
        write("\t\treturn "+formatName+";\r\n");
        write("\t}\r\n");
    }

    /**
     * 生成单个Set方法
     * @param item
     */
    public void createSetMethod(ColumnInfo item){
        String formatName = formatPropertyName(item.getColumnName());
        write("\r\n\tpublic void set"+formatUperName(formatName)+"("+javaTypeMap.get(item.getDataType())+" "+formatName+") {\r\n");
        write("\t\tthis."+formatName+" = "+formatName+";\r\n");
        write("\t}\r\n");
    }

    /**
     * 格式化列名
     *
     * @param columnName
     * @return
     */
    private String formatPropertyName(String columnName) {
        if (StringUtils.isBlank(columnName)) {
            return "";
        }
        String propertyName = columnName;
        char[] oriChars = columnName.toCharArray();

        for (int i = 0; i < oriChars.length - 1; i++) {
            if (oriChars[i] == '_' && (oriChars[i + 1] >= 'a' && oriChars[i + 1] <= 'z')) {
                String tmpStr = "_" + oriChars[i + 1];
                propertyName = propertyName.replace(tmpStr, String.valueOf((char)(oriChars[i + 1]-32)));
            }
        }
        return propertyName;
    }

    /**
     * 生成首字母大写的函数后缀
     */
    public String formatUperName(String name){
        char[] chars = name.toCharArray();
        if(chars[0] >= 'a' && chars[0] <= 'z'){
            chars[0] = (char)(chars[0]-32);
        }
        return String.copyValueOf(chars);
    }

    /**
     * 写入磁盘
     *
     * @param msg
     */
    private void write(String msg) {
        if (outputStream == null || StringUtils.isBlank(msg)) {
            return;
        }
        try {
            IOUtils.write(msg, outputStream);
        } catch (IOException e) {
            logger.error("write error msg:{}", msg, e);
        }
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
        this.columnInfoList = columnInfoList;
    }

    public void setJavaTypeMap(Map<String, String> javaTypeMap) {
        this.javaTypeMap = javaTypeMap;
    }

    public void setImportStrMap(Map<String, String> importStrMap) {
        this.importStrMap = importStrMap;
    }
}
