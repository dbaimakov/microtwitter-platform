CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscriber_user_id BIGINT NOT NULL,
    producer_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_subscriptions_subscriber_producer
        UNIQUE (subscriber_user_id, producer_user_id)
);

CREATE INDEX idx_subscriptions_subscriber
    ON subscriptions(subscriber_user_id);

CREATE INDEX idx_subscriptions_producer
    ON subscriptions(producer_user_id);

DELIMITER $$
CREATE TRIGGER trg_subscriptions_no_self_ins
BEFORE INSERT ON subscriptions
FOR EACH ROW
BEGIN
    IF NEW.subscriber_user_id = NEW.producer_user_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Subscriber cannot subscribe to themselves';
    END IF;
END$$
DELIMITER ;
