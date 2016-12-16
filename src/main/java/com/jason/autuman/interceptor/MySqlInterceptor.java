package com.jason.autuman.interceptor;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yuchangcun on 2016/8/30.
 */


/*注解是给wrap方法使用的*/
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class MySqlInterceptor implements Interceptor {

    protected static Logger logger = LoggerFactory
            .getLogger(MySqlInterceptor.class);
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY2 = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY2 = new DefaultObjectWrapperFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        /**
         *  MetaObject.forObject 用于快捷访问、设置对象属性，即使是私有方法、属性
         *  通过getValue("a.b.c"),级联获取后代属性
         *  通过setValue("a.b.c"),级联设置后代属性
         *  SystemMetaObject.forObject();简化工厂和Wrapper
         */
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);
        /**
         * 一个MappedStatement就相当于一个Mapper中的Select Update标签
         */
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
        /**
         * 获取可操作的反射对象 使用getValue() 和 setValue()方法
         * 这里可以简化为 SystemMetaObject.forObject(mappedStatement);
         */
        MetaObject metaMappedStatement = MetaObject.forObject(mappedStatement, DEFAULT_OBJECT_FACTORY2,DEFAULT_OBJECT_WRAPPER_FACTORY2);

        /**
         * mappedStatement.getId() 获取namespace.id Mapper中配置的ID
         * 只拦截这个方法，其他的正常走拦截链
         */
        if(!mappedStatement.getId().endsWith("com.jason.autuman.dao.TableInfoDao.selectCloumnListByTableName")){
            return invocation.proceed();
        }

        /**
         * 获取数据库名称
         *
         */
        String jdbcUrl = (String)metaStatementHandler.getValue("delegate.executor.transaction.dataSource.jdbcUrl");
        String dbName = getDBName(jdbcUrl);
        logger.info("dbName:{}",dbName);

        /**
         * 获取BuondSql对象 里面包含sql语句和参数
         */
        BoundSql boundSql = statementHandler.getBoundSql();
        logger.info("boundSql:{}", JSON.toJSONString(boundSql));
        /**
         * 获取sqlNode对象，一个SQL解析器、可以解析if choosen foreach等节点信息
         */
        SqlNode sqlNode = (SqlNode) metaMappedStatement
                .getValue("sqlSource.rootSqlNode");

        logger.info("sqlNode:{}", JSON.toJSONString(sqlNode));

        /**
         *DynamicContext 将入参的PO对象转成Map ，存储po对象的map转换结果
         */
        DynamicContext context = new DynamicContext(mappedStatement.getConfiguration(),boundSql.getParameterObject());

        /**
         * 递归调用解析动态sql
         * apply方法有多重实现 if实现、choosen实现、foreach实现。。。。
         */
        sqlNode.apply(context);
        /**
         * 获取解析完成的sql
         */
        String originalSql = context.getSql();
        logger.info("originalSql : {}",originalSql);

        /**
         *得到PO的class类型
         */
        Class<?> paramType = boundSql.getParameterObject() == null ? Object.class :  boundSql.getParameterObject().getClass();

        /**
         * sqlsource是一个接口类，只有一个getBoundSql方法
         * 一个BoundSql对象，代表了一次sql语句的实际执行，
         * 而SqlSource对象的责任，就是根据传入的参数对象，动态计算出这个BoundSql，
         * 也就是说Mapper文件中的<if />节点的计算，是由SqlSource对象完成的。
         * SqlSource最常用的实现类是DynamicSqlSource
         */
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(mappedStatement.getConfiguration());
        String newSql = originalSql + " AND TABLE_SCHEMA = '"+dbName+"';";
        SqlSource sqlSource = sqlSourceBuilder.parse(newSql,paramType,context.getBindings());

        boundSql = sqlSource.getBoundSql(boundSql.getParameterObject());
        /**
         * 重新再把参数塞回去
         */
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        /**
         * 把sql语句再放到属性里
         * 也可以直接把boundSql放进去
         * metaStatementHandler.setValue("delegate.boundSql", boundSql);
         * metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
         */
        metaStatementHandler.setValue("delegate.boundSql", boundSql);
        /**
         *形成拦截器的调用链
         */
        return invocation.proceed();

    }

    /**
     * plugin方法用于某些处理器(Handler)的构建过程
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        /**
         *  当目标类是StatementHandler类型时，才包装目标类，不做无意义的代理
         *  Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
         *  ParameterHandler (getParameterObject, setParameters)
         *  ResultSetHandler (handleResultSets, handleOutputParameters)
         *  StatementHandler (prepare, parameterize, batch, update, query)
         *
         *  wrap方法里使用到了上面的注解
         *  可以理解为给对象添加拦截器 target为handler对象，this为拦截器
         *  只有wrap包装后，才会去执行那个拦截器的方法
         */
        return (target instanceof StatementHandler) ? Plugin.wrap(target, this) : target;
    }

    /**
     * setProperties方法用于拦截器属性的设置
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 获取数据库名称
     * @param jdbcUrl
     * @return
     */
    private String getDBName(String jdbcUrl){
        if(StringUtils.isBlank(jdbcUrl)){
            return "";
        }
        return jdbcUrl.substring(jdbcUrl.lastIndexOf('/')+1,jdbcUrl.indexOf('?'));
    }
}
