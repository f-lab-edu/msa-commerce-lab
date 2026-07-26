package com.msa.commerce.monolith.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 애플리케이션 클래스가 아니라 별도 설정으로 분리한다.
// 앱 클래스에 두면 JPA 를 로드하지 않는 @WebMvcTest 슬라이스까지 감사 인프라를 초기화하려다 실패한다.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}
