package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
// TODO : ADD AUTH CONTROLLER LOGIC
class AuthController {
    @PostMapping
    fun createQuestion() {

    }
}