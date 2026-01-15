package com.khchan.petstore.test;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Test-only configuration that wraps the DataSource with a proxy.
 * This intercepts all SQL queries and transaction events without any changes to app code.
 *
 * Works seamlessly with @Transactional, TransactionTemplate, and any other transaction management.
 */
@TestConfiguration
public class DataSourceProxyConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource originalDataSource = properties.initializeDataSourceBuilder().build();

        return ProxyDataSourceBuilder.create(originalDataSource)
            .name("QueryTrackingDataSource")
            .listener(new QueryExecutionListener() {
                @Override
                public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                    // No-op before query
                }

                @Override
                public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                    for (QueryInfo queryInfo : queryInfoList) {
                        String query = queryInfo.getQuery();
                        QueryCounter.recordQuery(query);
                    }
                }
            })
            .afterMethod(executionContext -> {
                Method method = executionContext.getMethod();
                String methodName = method.getName();

                // Track transaction boundaries via Connection methods
                if ("setAutoCommit".equals(methodName)) {
                    Object[] args = executionContext.getMethodArgs();
                    if (args != null && args.length > 0 && args[0] instanceof Boolean) {
                        Boolean autoCommit = (Boolean) args[0];
                        if (!autoCommit) {
                            // setAutoCommit(false) indicates transaction begin
                            TransactionTracker.recordBegin();
                        }
                    }
                } else if ("commit".equals(methodName)) {
                    TransactionTracker.recordCommit();
                } else if ("rollback".equals(methodName)) {
                    TransactionTracker.recordRollback();
                }
            })
            .build();
    }
}
