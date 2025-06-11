package com.darknessopirate.appvsurveyapi.domain.entity.user

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(unique = true, length = 100, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val role: String = "ADMIN", // For simplicity, just using string

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val refreshTokens: List<RefreshToken> = emptyList()
)