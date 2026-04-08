package org.ac.cst8277.baimakov.dmitriy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTokenEntity {

    @Id
    @Column(name = "token", nullable = false, length = 36)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issued_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "active", nullable = false)
    private boolean active;
}
