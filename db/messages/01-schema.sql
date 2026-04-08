CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producer_user_id BIGINT NOT NULL,
    producer_username VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_producer_created
    ON messages(producer_user_id, created_at);

CREATE FULLTEXT INDEX ft_messages_content
    ON messages(content);
