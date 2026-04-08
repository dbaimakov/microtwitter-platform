INSERT INTO messages (message_id, producer_user_id, producer_username, content) VALUES
    (1, 1, 'alice', 'Welcome to MicroTwitter from Your Trollina! #hello'),
    (2, 3, 'carol', 'Carol is here muahahah. #microservices')
ON DUPLICATE KEY UPDATE
    producer_username = VALUES(producer_username),
    content = VALUES(content);

ALTER TABLE messages AUTO_INCREMENT = 3;
