package com.noovoweb.validator

/**
 * Represents a single validation error.
 *
 * @property message Human-readable error message
 * @property code Optional error code for programmatic handling
 * @property metadata Optional metadata for additional context
 */
data class ValidationError(
    val message: String,
    val code: String? = null,
    val metadata: Map<String, Any> = emptyMap(),
) {
    companion object {
        /**
         * Factory method for creating a "required" error.
         */
        fun required(field: String) =
            ValidationError(
                message = "Field '$field' is required",
                code = "required",
            )

        /**
         * Factory method for creating an "invalid" error.
         */
        fun invalid(
            field: String,
            reason: String,
        ) = ValidationError(
            message = "Field '$field' is invalid: $reason",
            code = "invalid",
        )
    }
}
