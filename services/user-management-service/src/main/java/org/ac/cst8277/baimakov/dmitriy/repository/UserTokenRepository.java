package org.ac.cst8277.baimakov.dmitriy.repository;

import org.ac.cst8277.baimakov.dmitriy.entity.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserTokenEntity, String> {
    Optional<UserTokenEntity> findByTokenAndActiveTrue(String token);
    Optional<UserTokenEntity> findTopByUserIdOrderByIssuedAtDesc(Long userId);
}
