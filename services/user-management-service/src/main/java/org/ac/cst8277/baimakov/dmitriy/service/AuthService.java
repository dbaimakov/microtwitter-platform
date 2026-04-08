package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginResponse;
import org.ac.cst8277.baimakov.dmitriy.entity.UserEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserTokenEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserTokenRepository;
import org.ac.cst8277.baimakov.dmitriy.util.PasswordHasher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final RoleService roleService;
    private final PasswordHasher passwordHasher;

    public AuthService(
            UserRepository userRepository,
            UserTokenRepository userTokenRepository,
            RoleService roleService,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.roleService = roleService;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String hashed = passwordHasher.hash(request.password());

        if (!hashed.equals(user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = UUID.randomUUID().toString();

        userTokenRepository.save(
                UserTokenEntity.builder()
                        .token(token)
                        .userId(user.getUserId())
                        .active(true)
                        .build()
        );

        return new LoginResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                roleService.findRoleNamesByUserId(user.getUserId())
        );
    }

    @Transactional
    public void logout(String token) {
        UserTokenEntity current = findActiveToken(token);
        current.setActive(false);
        current.setLogoutAt(LocalDateTime.now());
        userTokenRepository.save(current);
    }

    public AuthContextResponse validate(String token) {
        UserTokenEntity current = findActiveToken(token);

        UserEntity user = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        return new AuthContextResponse(
                user.getUserId(),
                user.getUsername(),
                roleService.findRoleNamesByUserId(user.getUserId()),
                current.isActive()
        );
    }

    public AuthContextResponse currentUser(String token) {
        return validate(token);
    }

    private UserTokenEntity findActiveToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return userTokenRepository.findByTokenAndActiveTrue(token.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
    }
}
