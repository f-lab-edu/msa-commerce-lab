package com.msa.commerce.monolith.config.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Order(0) // 트랜잭션보다 먼저 실행되어야 함
@Slf4j
public class DataSourceAspect {

    @Around("@annotation(transactional)")
    public Object determineDataSource(ProceedingJoinPoint joinPoint, Transactional transactional)
            throws Throwable {

        DataSourceType dataSourceType = transactional.readOnly()
            ? DataSourceType.READER
            : DataSourceType.WRITER;

        DataSourceContextHolder.setDataSourceType(dataSourceType);

        try {
            log.trace("데이터소스 설정: {} for method: {}",
                dataSourceType, joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    @Around("execution(* com.msa.commerce.monolith.product.application.service.*Service.get*(..)) || " +
            "execution(* com.msa.commerce.monolith.product.application.service.*Service.find*(..)) || " +
            "execution(* com.msa.commerce.monolith.product.application.service.*Service.search*(..))")
    public Object setReaderDataSourceForQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        // @Transactional(readOnly=true)가 명시되지 않은 조회 메소드에 대해 Reader 데이터소스 설정
        if (DataSourceContextHolder.getDataSourceType() == null) {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READER);
            log.trace("자동 Reader 데이터소스 설정 for method: {}", joinPoint.getSignature().getName());

            try {
                return joinPoint.proceed();
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }
        return joinPoint.proceed();
    }
}