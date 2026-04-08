INSERT INTO roles (role_id, role_name, description) VALUES
    (1, 'PRODUCER', 'Message content producer'),
    (2, 'SUBSCRIBER', 'Message content consumer'),
    (3, 'ADMIN', 'Administrative role for the system')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    description = VALUES(description);

INSERT INTO users (user_id, username, display_name, email, password_hash, auth_provider, provider_user_id) VALUES
    (1, 'alice', 'Alice Producer', 'alice@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'LOCAL', NULL),
    (2, 'bob', 'Bob Subscriber', 'bob@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'LOCAL', NULL),
    (3, 'carol', 'Carol Producer', 'carol@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'LOCAL', NULL),
    (4, 'admin', 'Admin User', 'admin@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'LOCAL', NULL)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    display_name = VALUES(display_name),
    email = VALUES(email),
    password_hash = VALUES(password_hash),
    auth_provider = VALUES(auth_provider),
    provider_user_id = VALUES(provider_user_id);

INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),
    (1, 2),
    (2, 2),
    (3, 1),
    (4, 3)
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id);

ALTER TABLE users AUTO_INCREMENT = 5;
