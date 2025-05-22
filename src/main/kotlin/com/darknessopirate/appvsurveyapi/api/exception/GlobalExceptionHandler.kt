package com.darknessopirate.appvsurveyapi.api.exception

import com.darknessopirate.appvsurveyapi.domain.exception.InvalidOperationException
import com.darknessopirate.appvsurveyapi.domain.exception.ResourceNotFoundException
import com.darknessopirate.appvsurveyapi.domain.exception.ValidationException
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource with given id not found",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource with given id not found",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
/*
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Validation error",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
*/
    @ExceptionHandler(InvalidOperationException::class)
    fun handleInvalidOperationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ,
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ,
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
    // when json malformed
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleRequestBadFormat(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Bad Request",
            timestamp = System.currentTimeMillis()
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception occurred:", ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}