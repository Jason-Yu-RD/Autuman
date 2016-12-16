package com.jason.autuman.action;

import com.jason.autuman.service.TableInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Created by yuchangcun on 2016/8/29.
 */
public class MainAction {

    private static final Logger logger = LoggerFactory.getLogger(MainAction.class);

    public static void main(String[] args) throws Exception{
        try{
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config.xml");
            TableInfoService tableInfoService = (TableInfoService)context.getBean("tableInfoService");
            tableInfoService.createMapperFile("info_topic_mapping");
            tableInfoService.createJavaFile("info_topic_mapping");
        }catch(Exception e){
            logger.error("",e);
        }
    }

}
