package com.app.uni_app.application.common;

import com.app.uni_app.application.config.AppConfig;
import com.app.uni_app.application.config.ConfigHolder;
import com.app.uni_app.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

/**
 * 数据库工具类
 * 可以独立获取数据库连接 无需启动项目
 */
public class DbUtil {

    public static SqlSessionFactory getSqlSessionFactory() {
        AppConfig.DbConfig dbConfig = ConfigHolder.getConfig().getDatasource();

        // 打印配置，确认读取正确
        System.out.println("===== 读取数据库配置 =====");
        System.out.println("驱动：" + dbConfig.getDriverClassName());
        System.out.println("URL：" + dbConfig.getUrl());
        System.out.println("账号：" + dbConfig.getUsername());
        System.out.println("密码：" + dbConfig.getPassword());

        DataSource dataSource = new PooledDataSource(
                dbConfig.getDriverClassName(),
                dbConfig.getUrl(),
                dbConfig.getUsername(),
                dbConfig.getPassword()
        );

        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(ProductMapper.class);
        configuration.setEnvironment(environment);

        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }
}