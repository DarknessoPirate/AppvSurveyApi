package com.darknessopirate.appvsurveyapi.domain.model

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import jakarta.persistence.CascadeType
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

// TODO: TRY TO SPLITUP QUESTIONS INTO OPEN AND CLOSED
data class OpenQuestion: Question ()
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    var survey: Survey? = null,

    var text: String,

    @Enumerated(EnumType.STRING)
    val questionType: QuestionType,

    var required: Boolean = false,

    var displayOrder: Int = 0,

)