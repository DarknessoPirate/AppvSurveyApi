package com.darknessopirate.appvsurveyapi.domain.entity.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

// TODO: ADD ROLE BASED AUTHENTICATION
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private val id: Int? = null,

    @Column(nullable = false)
    private val name: String? = null

)