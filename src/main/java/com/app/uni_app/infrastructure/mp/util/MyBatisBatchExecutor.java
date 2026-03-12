package com.app.uni_app.infrastructure.mp.util;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

/**
 * 通用MyBatis 批量执行工具
 */

@Component
public class MyBatisBatchExecutor {

    private final SqlSessionTemplate sqlSessionTemplate;

    public MyBatisBatchExecutor(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 批量执行SQL操作（封装BATCH模式）
     * @param action 要执行的批量操作（Function接口，替代匿名内部类）
     * @param <T> 返回值类型
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T executeBatch(Function<SqlSession, T> action) {
        // 1. 开启BATCH模式SqlSession（不自动提交）
        try (SqlSession batchSession = sqlSessionTemplate.getSqlSessionFactory()
                .openSession(ExecutorType.BATCH, false)) {
            // 2. 执行批量操作
            T result = action.apply(batchSession);
            // 3. 批量提交
            batchSession.commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("批量执行失败", e);
        }
    }
}