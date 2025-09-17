package com.msa.commerce.monolith.config.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DataSourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.writer")
    public DataSource writerDataSource() {
        log.info("Writer 데이터소스 초기화");
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.reader")
    @Profile("!test") // 테스트 환경에서는 제외
    public DataSource readerDataSource() {
        log.info("Reader 데이터소스 초기화");
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean("readerDataSource")
    @Profile("test") // 테스트 환경에서는 Writer 데이터소스를 Reader로도 사용
    public DataSource testReaderDataSource(@Qualifier("writerDataSource") DataSource writerDataSource) {
        log.info("테스트 환경: Writer 데이터소스를 Reader로 공유");
        return writerDataSource;
    }

    @Bean
    @Primary
    public DataSource dynamicDataSource(@Qualifier("writerDataSource") DataSource writerDataSource,
                                      @Qualifier("readerDataSource") DataSource readerDataSource) {

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.createTargetDataSources(writerDataSource, readerDataSource);

        log.info("동적 라우팅 데이터소스 구성 완료");
        return dynamicDataSource;
    }
}