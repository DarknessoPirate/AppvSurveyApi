package com.darknessopirate.appvsurveyapi.api.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class LoginRequest(

    @field:Email(message = "Provide correct email format")
    @field:NotEmpty(message = "Email cannot be empty")
    @field:NotBlank(message = "Email cannot be blank")
    @field:Size(min = 4, max = 30, message = "Email length must be 4-30")
    val email:String,

    @field:NotEmpty(message = "Password cannot be empty")
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 4, max = 30, message = "Password length must be 4-30")
    val password:String,
)