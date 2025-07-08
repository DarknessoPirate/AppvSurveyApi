package com.darknessopirate.appvsurveyapi.domain.entity.survey

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "access_codes")
data class AccessCode (
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var code: String,

    @Column(nullable = false)
    var title: String,

    var description: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "usage_count")
    var usageCount: Int = 0,

    @Column(name = "max_uses")
    var maxUses: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    var survey: Survey? = null
    )