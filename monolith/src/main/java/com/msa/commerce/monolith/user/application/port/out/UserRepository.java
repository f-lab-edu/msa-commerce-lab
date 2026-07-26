package com.msa.commerce.monolith.user.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msa.commerce.monolith.user.domain.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByIds(List<Long> ids);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
