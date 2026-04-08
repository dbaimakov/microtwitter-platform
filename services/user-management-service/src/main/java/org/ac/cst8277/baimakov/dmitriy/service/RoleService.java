package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.dto.RoleResponse;
import org.ac.cst8277.baimakov.dmitriy.entity.RoleEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserRoleEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.RoleRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAllByOrderByRoleIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Set<String> findRoleNamesByUserId(Long userId) {
        return findRoleEntitiesByUserId(userId)
                .stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<RoleResponse> findRolesByUserId(Long userId) {
        return findRoleEntitiesByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RoleEntity> findByRoleNames(Set<String> rawRoleNames) {
        Set<String> normalized = rawRoleNames.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, RoleEntity> byName = roleRepository.findAllByOrderByRoleIdAsc()
                .stream()
                .collect(Collectors.toMap(RoleEntity::getRoleName, Function.identity()));

        List<RoleEntity> matched = normalized.stream()
                .map(byName::get)
                .filter(Objects::nonNull)
                .toList();

        if (matched.size() != normalized.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more roles were not found");
        }

        return matched;
    }

    private List<RoleEntity> findRoleEntitiesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<RoleEntity> roles = roleRepository.findByRoleIdIn(roleIds);
        roles.sort(Comparator.comparing(RoleEntity::getRoleId));
        return roles;
    }

    private RoleResponse toResponse(RoleEntity role) {
        return new RoleResponse(role.getRoleId(), role.getRoleName(), role.getDescription());
    }
}
