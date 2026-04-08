INSERT INTO subscriptions (subscription_id, subscriber_user_id, producer_user_id) VALUES
    (1, 2, 1),
    (2, 2, 3)
ON DUPLICATE KEY UPDATE
    producer_user_id = VALUES(producer_user_id);

ALTER TABLE subscriptions AUTO_INCREMENT = 3;
