package com.jason.autuman.autos;

import com.jason.autuman.domain.po.ColumnInfo;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * Created by yuchangcun on 2016/8/29.
 */
public class AutoMapperFile implements AutoFile {

    /*不需要查询判断的列*/
    private List<String> excludeMap;

    /*数据库类型到java类型的映射*/
    private Map<String, String> javaTypeMap;

    private List<ColumnInfo> dataList;

    private Writer fileWriter;

    public void createMapperFile(String filePath,String tableName) throws IOException {
        File file = new File(filePath+ File.separator+formatUperName(formatPropertyName(tableName))+"DaoMapper.xml");
        if(file.exists()){
            file.delete();
        }
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        this.fileWriter = new FileWriter(file);
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        document.addDocType("mapper","-//mybatis.org//DTD Mapper 3.0//EN", "http://mybatis.org/dtd/mybatis-3-mapper.dtd");
        Element rootElement = document.addElement("mapper");
        createMapperNode(rootElement);
        Element resultMap = rootElement.addElement("resultMap");
        createResultMapNode(resultMap);
        createResultNodes(resultMap);
        Element baseColumnSql = rootElement.addElement("sql");
        createBaseColumnList(baseColumnSql);
        Element baseQuerySql = rootElement.addElement("sql");
        createBaseQueryList(baseQuerySql);
        Element baseUpdateSql = rootElement.addElement("sql");
        createBaseUpdateList(baseUpdateSql);
        write(document);
    }

    /**
     * 创建mapper节点
     * @param element
     */
    public void createMapperNode(Element element){
        element.addAttribute("namespace","com.jason.autuman.dao.**");
    }

    /**
     * 创建resultMap节点
     * @param element
     */
    public void createResultMapNode(Element element){
        element.addAttribute("id","BaseResultMap");
        element.addAttribute("type","com.jason.autuman.domian.po.**");
    }

    /**
     * 生成所有result 列
     * @param resultMap
     */
    public void createResultNodes(Element resultMap){
        for(ColumnInfo item : dataList){
            if(null == item || StringUtils.isBlank(item.getColumnName()) || StringUtils.isBlank(item.getDataType())){
                continue;
            }
            Element result = resultMap.addElement("result");
            createSingleResult(result,item);
        }
    }

    /**
     * 生成单个result列
     * @param result
     * @param item
     */
    public void createSingleResult(Element result,ColumnInfo item){
        result.addAttribute("column",item.getColumnName());
        result.addAttribute("property",formatPropertyName(item.getColumnName()));
        result.addAttribute("jdbcType",item.getDataType().toUpperCase());
    }

    /**
     * 生成选择所有列的Sql
     * @param element
     */
    public void createBaseColumnList(Element element){
        element.addAttribute("id", "BaseColmnSql");
        StringBuffer baseSql = new StringBuffer();
        for (int index = 0; index < dataList.size(); index++) {
            baseSql.append(dataList.get(index).getColumnName());
            if(index  != dataList.size()-1){
                baseSql.append(",");
            }
        }
        element.addCDATA(baseSql.toString());
    }

    /**
     * 生成query语句
     * @param element
     */
    public void createBaseQueryList(Element element){
        element.addAttribute("id","BaseQuerySql");
        for(ColumnInfo item : dataList){
            if(null == item || StringUtils.isBlank(item.getColumnName())){
                return;
            }
            Element queryEle = element.addElement("if");
            createSingleIfQuery(queryEle,item);
        }
    }

    /**
     * 生成单个的查询query
     * @param element
     */
    public void createSingleIfQuery(Element element,ColumnInfo item){
        String proName = formatPropertyName(item.getColumnName());
        StringBuffer buffer = new StringBuffer();
        buffer.append(proName).append("!=null");
        if("String".equals(javaTypeMap.get(item.getDataType()))){
            buffer.append(" and ").append(proName).append("!=''");
        }
        element.addAttribute("test",buffer.toString());
        buffer = new StringBuffer();
        buffer.append("AND ").append(item.getColumnName()).append(" = #{")
                .append(proName).append("}");
        element.addCDATA(buffer.toString());
    }

    /**
     * 生成的Base更新Sql
     * @param element
     */
    public void createBaseUpdateList(Element element){
        element.addAttribute("id","BaseUpdateSql");
        for(ColumnInfo item : dataList){
            if(null == item || StringUtils.isBlank(item.getColumnName())){
                continue;
            }
            Element queryEle = element.addElement("if");
            createSingleIfUpdate(queryEle, item);
        }
    }

    /**
     * 生成单个的更新update
     * @param element
     */
    public void createSingleIfUpdate(Element element,ColumnInfo item){
        String proName = formatPropertyName(item.getColumnName());
        StringBuffer buffer = new StringBuffer();
        buffer.append(proName).append("!=null");
        if("String".equals(javaTypeMap.get(item.getDataType()))){
            buffer.append(" and ").append(proName).append("!=''");
        }
        element.addAttribute("test",buffer.toString());
        buffer = new StringBuffer();
        buffer.append(item.getColumnName()).append(" = #{")
                .append(proName).append("},");
        element.addCDATA(buffer.toString());
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

    public void write(Document document) throws IOException{
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter xmlWriter = new XMLWriter(this.fileWriter,format);
        xmlWriter.write(document);
        xmlWriter.flush();
        xmlWriter.close();
    }

    public void setExcludeMap(List<String> excludeMap) {
        this.excludeMap = excludeMap;
    }

    public void setDataList(List<ColumnInfo> dataList) {
        this.dataList = dataList;
    }

    public void setJavaTypeMap(Map<String, String> javaTypeMap) {
        this.javaTypeMap = javaTypeMap;
    }
}
