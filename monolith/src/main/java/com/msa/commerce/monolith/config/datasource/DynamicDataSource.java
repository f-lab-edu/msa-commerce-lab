package com.msa.commerce.monolith.config.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
        if (dataSourceType == null) {
            dataSourceType = DataSourceType.WRITER; // 기본값은 Writer
        }

        log.trace("현재 선택된 데이터소스: {}", dataSourceType);
        return dataSourceType;
    }

    public void createTargetDataSources(DataSource writerDataSource, DataSource readerDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.WRITER, writerDataSource);
        targetDataSources.put(DataSourceType.READER, readerDataSource);

        setTargetDataSources(targetDataSources);
        setDefaultTargetDataSource(writerDataSource);

        afterPropertiesSet();

        log.info("동적 데이터소스 초기화 완료 - Writer/Reader 분리");
    }
}