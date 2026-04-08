package org.ac.cst8277.baimakov.dmitriy.repository;

import org.ac.cst8277.baimakov.dmitriy.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findAllByOrderByCreatedAtDesc();
    List<MessageEntity> findByProducerUserIdOrderByCreatedAtDesc(Long producerUserId);
    List<MessageEntity> findByProducerUserIdInOrderByCreatedAtDesc(Collection<Long> producerUserIds);
    @Query(value = "SELECT * FROM messages WHERE MATCH(content) AGAINST (?1 IN NATURAL LANGUAGE MODE) ORDER BY created_at DESC", nativeQuery = true)
    List<MessageEntity> searchByContent(String query);
}
