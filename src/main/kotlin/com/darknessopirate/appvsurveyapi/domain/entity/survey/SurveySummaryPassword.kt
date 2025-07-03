package com.darknessopirate.appvsurveyapi.domain.entity.survey

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "survey_summary_passwords")
data class SurveySummaryPassword(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", unique = true)
    val survey: Survey,

    @Column(nullable = false)
    var password: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
){
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}