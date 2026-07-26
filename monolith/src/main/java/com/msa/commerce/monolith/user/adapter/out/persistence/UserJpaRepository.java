package com.msa.commerce.monolith.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByUsername(String username);

    List<UserJpaEntity> findByIdIn(List<Long> ids);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
