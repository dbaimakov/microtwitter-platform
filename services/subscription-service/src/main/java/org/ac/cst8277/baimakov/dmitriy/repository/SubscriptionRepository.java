package org.ac.cst8277.baimakov.dmitriy.repository;

import org.ac.cst8277.baimakov.dmitriy.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    boolean existsBySubscriberUserIdAndProducerUserId(Long subscriberUserId, Long producerUserId);
    List<SubscriptionEntity> findBySubscriberUserIdOrderByCreatedAtDesc(Long subscriberUserId);
    Optional<SubscriptionEntity> findBySubscriberUserIdAndProducerUserId(Long subscriberUserId, Long producerUserId);
}
