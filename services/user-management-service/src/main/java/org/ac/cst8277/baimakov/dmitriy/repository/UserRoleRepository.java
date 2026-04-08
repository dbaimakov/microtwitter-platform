package org.ac.cst8277.baimakov.dmitriy.repository;

import org.ac.cst8277.baimakov.dmitriy.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId);
    List<UserRoleEntity> findByUserIdIn(Collection<Long> userIds);
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
