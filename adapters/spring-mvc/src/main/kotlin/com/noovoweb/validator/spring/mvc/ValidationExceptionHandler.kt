package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.ValidationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for ValidationException in Spring MVC.
 *
 * Automatically converts ValidationException to a structured JSON response:
 * ```json
 * {
 *   "status": 422,
 *   "message": "Validation Failed",
 *   "errors": {
 *     "email": ["Must be a valid email address"],
 *     "age": ["Must be at least 18"]
 *   }
 * }
 * ```
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {
    /**
     * Handle ValidationException and return 422 Unprocessable Entity with error details.
     */
    @ExceptionHandler(ValidationException::class)
    public fun handleValidationException(ex: ValidationException): ResponseEntity<ValidationErrorResponse> {
        val response =
            ValidationErrorResponse(
                status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
                message = "Validation Failed",
                errors = ex.errors,
            )

        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(response)
    }
}

/**
 * Structured validation error response for REST APIs.
 *
 * @property status HTTP status code (422)
 * @property message Error message ("Validation Failed")
 * @property errors Map of field names to error messages
 */
public data class ValidationErrorResponse(
    val status: Int,
    val message: String,
    val errors: Map<String, List<String>>,
)
