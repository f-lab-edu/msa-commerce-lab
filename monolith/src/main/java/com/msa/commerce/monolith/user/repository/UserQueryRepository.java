package com.msa.commerce.monolith.user.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.monolith.user.UserStatus;
import com.msa.commerce.monolith.user.entity.User;

public interface UserQueryRepository {

    Page<User> search(
        String keyword,              // username/email/first_name/last_name 포함 검색
        UserStatus status,           // 상태 필터
        LocalDateTime createdFrom,   // 생성일 From
        LocalDateTime createdTo,     // 생성일 To
        Pageable pageable
    );

}
