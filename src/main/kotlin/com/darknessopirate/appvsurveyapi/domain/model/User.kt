package com.darknessopirate.appvsurveyapi.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.Date


// TODO: ADD USERs to db
// TODO: ADD ADMIN CREATION ON BUILD or something
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private val id: Int? = null

    @Column(nullable = false)
    private val username: String? = null

    @Column(unique = true, length = 30, nullable = false)
    private var email: String? = null

    @Column(nullable = false)
    private val password: String? = null

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private val createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    private val updatedAt: LocalDateTime? = null

    // TODO :ADD ROLES


}