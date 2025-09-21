package com.msa.commerce.common.config.database;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(name = "spring.datasource.routing.enabled", havingValue = "true", matchIfMissing = false)
public class RoutingDataSourceAutoConfiguration {

    @Bean("writerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.writer")
    public DataSource writerDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "readerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.reader")
    @ConditionalOnProperty(prefix = "spring.datasource.reader", name = "jdbc-url")
    public DataSource readerDataSourceConfigured() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "readerDataSource")
    @ConditionalOnMissingBean(name = "readerDataSource")
    public DataSource readerDataSourceFallback(@Qualifier("writerDataSource") DataSource writerDataSource) {
        return writerDataSource;
    }

    @Bean
    public DataSource routingDataSource(
        @Qualifier("writerDataSource") DataSource writerDataSource,
        @Qualifier("readerDataSource") DataSource readerDataSource
    ) {
        DynamicRoutingDataSource routing = new DynamicRoutingDataSource();
        Map<Object, Object> targets = new HashMap<>();
        targets.put(DataSourceType.WRITER, writerDataSource);
        targets.put(DataSourceType.READER, readerDataSource);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(writerDataSource);
        routing.afterPropertiesSet();
        return routing;
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

}
