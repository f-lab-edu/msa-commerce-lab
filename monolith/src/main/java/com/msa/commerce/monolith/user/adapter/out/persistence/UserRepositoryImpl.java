package com.msa.commerce.monolith.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return userJpaRepository.save(UserJpaEntity.fromDomainEntityForCreation(user)).toDomainEntity();
        }

        // 같은 트랜잭션에서 이미 조회된 엔티티는 영속성 컨텍스트에서 반환되므로 추가 조회가 발생하지 않는다.
        UserJpaEntity jpaEntity = userJpaRepository.findById(user.getId())
            .orElseGet(() -> UserJpaEntity.fromDomainEntity(user));
        jpaEntity.applyDomainEntity(user);

        return userJpaRepository.save(jpaEntity).toDomainEntity();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public List<User> findAllByIds(List<Long> ids) {
        return userJpaRepository.findByIdIn(ids)
            .stream()
            .map(UserJpaEntity::toDomainEntity)
            .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

}
