package org.ac.cst8277.baimakov.dmitriy.repository;

import org.ac.cst8277.baimakov.dmitriy.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleName(String roleName);
    List<RoleEntity> findByRoleIdIn(Collection<Long> roleIds);
    List<RoleEntity> findAllByOrderByRoleIdAsc();
}
