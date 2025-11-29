package com.noovoweb.validator

/**
 * Represents a single validation error.
 *
 * @property message Human-readable error message
 * @property code Optional error code for programmatic handling
 * @property metadata Optional metadata for additional context
 */
public data class ValidationError(
    public val message: String,
    public val code: String? = null,
    public val metadata: Map<String, Any> = emptyMap(),
) {
    public companion object {
        public fun required(field: String): ValidationError =
            ValidationError(
                message = "Field '$field' is required",
                code = "required",
            )

        public fun invalid(
            field: String,
            reason: String,
        ): ValidationError =
            ValidationError(
                message = "Field '$field' is invalid: $reason",
                code = "invalid",
            )
    }
}
