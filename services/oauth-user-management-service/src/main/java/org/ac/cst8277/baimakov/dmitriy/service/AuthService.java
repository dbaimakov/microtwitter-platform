package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.config.AppSecurityProperties;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.JwtClaimsResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginResponse;
import org.ac.cst8277.baimakov.dmitriy.entity.RoleEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserRoleEntity;
import org.ac.cst8277.baimakov.dmitriy.entity.UserTokenEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserRoleRepository;
import org.ac.cst8277.baimakov.dmitriy.repository.UserTokenRepository;
import org.ac.cst8277.baimakov.dmitriy.util.PasswordHasher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final AppSecurityProperties appSecurityProperties;

    public AuthService(
            UserRepository userRepository,
            UserTokenRepository userTokenRepository,
            UserRoleRepository userRoleRepository,
            RoleService roleService,
            PasswordHasher passwordHasher,
            JwtService jwtService,
            AppSecurityProperties appSecurityProperties
    ) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.appSecurityProperties = appSecurityProperties;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String hashed = passwordHasher.hash(request.password());

        if (!hashed.equals(user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return issueTokens(user, "LOCAL", null);
    }

    @Transactional
    public LoginResponse loginWithGithub(Map<String, Object> attributes) {
        UserEntity user = synchronizeGithubUser(attributes);
        ensureDefaultRolesIfMissing(user.getUserId());
        String providerLogin = attribute(attributes, "login");
        return issueTokens(user, "GITHUB", providerLogin);
    }

    @Transactional
    public void logout(String token) {
        UserTokenEntity current = findValidToken(token);
        current.setActive(false);
        current.setLogoutAt(LocalDateTime.now());
        userTokenRepository.save(current);
    }

    @Transactional
    public AuthContextResponse validate(String token) {
        UserTokenEntity current = findValidToken(token);

        UserEntity user = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        return new AuthContextResponse(
                user.getUserId(),
                user.getUsername(),
                roleService.findRoleNamesByUserId(user.getUserId()),
                current.isActive()
        );
    }

    @Transactional
    public AuthContextResponse currentUser(String token) {
        return validate(token);
    }

    public JwtClaimsResponse inspectJwt(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token is required");
        }
        return jwtService.inspect(token.trim());
    }

    private UserEntity synchronizeGithubUser(Map<String, Object> attributes) {
        String providerUserId = attribute(attributes, "id");
        if (!StringUtils.hasText(providerUserId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GitHub account id was not provided");
        }

        String githubLogin = attribute(attributes, "login");
        if (!StringUtils.hasText(githubLogin)) {
            githubLogin = "github_" + providerUserId;
        }

        String displayName = attribute(attributes, "name");
        if (!StringUtils.hasText(displayName)) {
            displayName = githubLogin;
        }

        String email = attribute(attributes, "email");
        Optional<UserEntity> existingByProvider = userRepository.findByProviderUserId(providerUserId);
        if (existingByProvider.isPresent()) {
            UserEntity user = existingByProvider.get();
            user.setAuthProvider("GITHUB");
            user.setProviderUserId(providerUserId);
            user.setDisplayName(displayName);
            if (StringUtils.hasText(email)) {
                user.setEmail(resolveExistingEmail(user, email));
            }
            updateGithubUsernameIfAvailable(user, githubLogin);
            return userRepository.save(user);
        }

        if (StringUtils.hasText(email)) {
            Optional<UserEntity> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                UserEntity user = existingByEmail.get();
                user.setAuthProvider("GITHUB");
                user.setProviderUserId(providerUserId);
                user.setDisplayName(displayName);
                return userRepository.save(user);
            }
        }

        Optional<UserEntity> existingByUsername = userRepository.findByUsername(githubLogin);
        if (existingByUsername.isPresent()) {
            UserEntity user = existingByUsername.get();
            user.setAuthProvider("GITHUB");
            user.setProviderUserId(providerUserId);
            user.setDisplayName(displayName);
            if (StringUtils.hasText(email)) {
                user.setEmail(resolveExistingEmail(user, email));
            }
            return userRepository.save(user);
        }

        String username = buildUniqueUsername(githubLogin, providerUserId);
        String resolvedEmail = buildUniqueEmail(email, providerUserId);

        UserEntity created = userRepository.save(
                UserEntity.builder()
                        .username(username)
                        .displayName(displayName)
                        .email(resolvedEmail)
                        .passwordHash(passwordHasher.hash(UUID.randomUUID().toString()))
                        .authProvider("GITHUB")
                        .providerUserId(providerUserId)
                        .build()
        );

        return created;
    }

    private LoginResponse issueTokens(UserEntity user, String authSource, String providerLogin) {
        revokeActiveTokens(user.getUserId());

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusMinutes(appSecurityProperties.getSession().getTtlMinutes());
        String token = UUID.randomUUID().toString();

        userTokenRepository.save(
                UserTokenEntity.builder()
                        .token(token)
                        .userId(user.getUserId())
                        .expiresAt(expiresAt)
                        .active(true)
                        .build()
        );

        Set<String> roles = roleService.findRoleNamesByUserId(user.getUserId());
        String jwtToken = jwtService.issueToken(
                user.getUserId(),
                user.getUsername(),
                roles,
                issuedAt,
                expiresAt,
                authSource,
                providerLogin
        );

        return new LoginResponse(
                token,
                jwtToken,
                expiresAt,
                user.getUserId(),
                user.getUsername(),
                roles,
                authSource,
                providerLogin
        );
    }

    private void revokeActiveTokens(Long userId) {
        List<UserTokenEntity> activeTokens = userTokenRepository.findByUserIdAndActiveTrue(userId);
        if (activeTokens.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (UserTokenEntity token : activeTokens) {
            token.setActive(false);
            if (token.getLogoutAt() == null) {
                token.setLogoutAt(now);
            }
            if (token.getExpiresAt() == null && token.getIssuedAt() != null) {
                token.setExpiresAt(token.getIssuedAt().plusMinutes(appSecurityProperties.getSession().getTtlMinutes()));
            }
        }
        userTokenRepository.saveAll(activeTokens);
    }

    private UserTokenEntity findValidToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        UserTokenEntity current = userTokenRepository.findById(token.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        if (!current.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        LocalDateTime expiresAt = resolveExpiresAt(current);
        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            current.setActive(false);
            current.setLogoutAt(LocalDateTime.now());
            current.setExpiresAt(expiresAt);
            userTokenRepository.save(current);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        if (current.getExpiresAt() == null && expiresAt != null) {
            current.setExpiresAt(expiresAt);
            userTokenRepository.save(current);
        }

        return current;
    }

    private LocalDateTime resolveExpiresAt(UserTokenEntity tokenEntity) {
        if (tokenEntity.getExpiresAt() != null) {
            return tokenEntity.getExpiresAt();
        }
        if (tokenEntity.getIssuedAt() == null) {
            return null;
        }
        return tokenEntity.getIssuedAt().plusMinutes(appSecurityProperties.getSession().getTtlMinutes());
    }

    private void ensureDefaultRolesIfMissing(Long userId) {
        if (!userRoleRepository.findByUserId(userId).isEmpty()) {
            return;
        }

        List<RoleEntity> roles = roleService.findByRoleNames(appSecurityProperties.getOauth().getDefaultRoles());
        for (RoleEntity role : roles) {
            userRoleRepository.save(
                    UserRoleEntity.builder()
                            .userId(userId)
                            .roleId(role.getRoleId())
                            .build()
            );
        }
    }

    private String resolveExistingEmail(UserEntity user, String preferredEmail) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(preferredEmail);
        if (byEmail.isEmpty() || byEmail.get().getUserId().equals(user.getUserId())) {
            return preferredEmail;
        }
        return user.getEmail();
    }

    private void updateGithubUsernameIfAvailable(UserEntity user, String preferredUsername) {
        String normalized = normalizeUsername(preferredUsername, user.getProviderUserId());
        Optional<UserEntity> byUsername = userRepository.findByUsername(normalized);
        if (byUsername.isEmpty() || byUsername.get().getUserId().equals(user.getUserId())) {
            user.setUsername(normalized);
        }
    }

    private String buildUniqueUsername(String preferredUsername, String providerUserId) {
        String base = normalizeUsername(preferredUsername, providerUserId);
        if (!userRepository.existsByUsername(base)) {
            return base;
        }

        String fallback = trimToLength(base + "_" + providerUserId, 50);
        if (!userRepository.existsByUsername(fallback)) {
            return fallback;
        }

        String random = trimToLength("github_" + providerUserId + "_" + UUID.randomUUID().toString().replace("-", ""), 50);
        if (!userRepository.existsByUsername(random)) {
            return random;
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to allocate unique username for GitHub account");
    }

    private String buildUniqueEmail(String preferredEmail, String providerUserId) {
        if (StringUtils.hasText(preferredEmail) && !userRepository.existsByEmail(preferredEmail)) {
            return preferredEmail;
        }
        return "github+" + providerUserId + "@users.noreply.github.com";
    }

    private String normalizeUsername(String preferredUsername, String providerUserId) {
        String normalized = StringUtils.hasText(preferredUsername) ? preferredUsername.trim() : "github_" + providerUserId;
        return trimToLength(normalized, 50);
    }

    private String trimToLength(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String attribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
