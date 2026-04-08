package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.CreateUserRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.LastSessionResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.UserResponse;
import org.ac.cst8277.baimakov.dmitriy.entity.RoleEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserRoleEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRoleRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserTokenRepository;
import org.ac.cst8277.baimakov.dmitriy.util.PasswordHasher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserTokenRepository userTokenRepository;
    private final RoleService roleService;
    private final PasswordHasher passwordHasher;

    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            UserTokenRepository userTokenRepository,
            RoleService roleService,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userTokenRepository = userTokenRepository;
        this.roleService = roleService;
        this.passwordHasher = passwordHasher;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserEntity::getUserId))
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(Long userId) {
        return toResponse(findEntityById(userId));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity saved = userRepository.save(
                UserEntity.builder()
                        .username(request.username())
                        .displayName(request.displayName())
                        .email(request.email())
                        .passwordHash(passwordHasher.hash(request.password()))
                        .build()
        );

        List<RoleEntity> roles = roleService.findByRoleNames(request.roles());

        for (RoleEntity role : roles) {
            userRoleRepository.save(
                    UserRoleEntity.builder()
                            .userId(saved.getUserId())
                            .roleId(role.getRoleId())
                            .build()
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        userRepository.deleteById(userId);
    }

    public AuthContextResponse getInternalUser(Long userId) {
        UserEntity user = findEntityById(userId);
        return new AuthContextResponse(
                user.getUserId(),
                user.getUsername(),
                roleService.findRoleNamesByUserId(userId),
                true
        );
    }

    private UserEntity findEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(UserEntity user) {
        LastSessionResponse lastSession = userTokenRepository.findTopByUserIdOrderByIssuedAtDesc(user.getUserId())
                .map(token -> new LastSessionResponse(
                        token.getIssuedAt(),
                        token.getLogoutAt(),
                        token.isActive()
                ))
                .orElse(null);

        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                roleService.findRolesByUserId(user.getUserId()),
                lastSession
        );
    }
}
