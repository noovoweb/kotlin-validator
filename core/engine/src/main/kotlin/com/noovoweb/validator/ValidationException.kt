package com.noovoweb.validator

/**
 * Exception thrown when validation fails.
 *
 * Contains a map of field names to their error messages. Multiple errors per field are supported.
 *
 * @property errors Map of field paths to error message lists
 * @property message Summary message
 *
 * Example error structure:
 * ```kotlin
 * {
 *   "email": ["Please enter a valid email address", "This field must not exceed 255 characters"],
 *   "age": ["This field must be at least 18"],
 *   "address.zipCode": ["Invalid format"]
 * }
 * ```
 */
public class ValidationException(
    public val errors: Map<String, List<String>>,
    message: String = "Validation failed with ${errors.size} field error(s)"
) : RuntimeException(message) {
    /**
     * Get errors for a specific field.
     */
    public fun getFieldErrors(field: String): List<String> = errors[field] ?: emptyList()

    /**
     * Check if a specific field has errors.
     */
    public fun hasFieldError(field: String): Boolean = errors.containsKey(field)

    /**
     * Get all error messages as a flat list.
     */
    public fun getAllMessages(): List<String> = errors.values.flatten()

    /**
     * Format errors as JSON string.
     *
     * All field names and message values are properly escaped per RFC 8259.
     */
    public fun toJson(): String = buildString {
        append("{")
        errors.entries.joinTo(this, ",") { (field, messages) ->
            "\"${escapeJson(field)}\":[${messages.joinToString(",") { "\"${escapeJson(it)}\"" }}]"
        }
        append("}")
    }

    private companion object {
        /**
         * Escapes a string for safe inclusion in a JSON value per RFC 8259.
         */
        fun escapeJson(value: String): String = buildString(value.length) {
            for (ch in value) {
                when (ch) {
                    '"' -> append("\\\"")

                    '\\' -> append("\\\\")

                    '\n' -> append("\\n")

                    '\r' -> append("\\r")

                    '\t' -> append("\\t")

                    '\b' -> append("\\b")

                    '\u000C' -> append("\\f")

                    else -> if (ch.code < 0x20) {
                        append("\\u${ch.code.toString(16).padStart(4, '0')}")
                    } else {
                        append(ch)
                    }
                }
            }
        }
    }

    /**
     * Get the total number of error messages across all fields.
     */
    public val errorCount: Int
        get() = errors.values.sumOf { it.size }

    /**
     * Get the number of fields with errors.
     */
    public val fieldCount: Int
        get() = errors.size

    override fun toString(): String = "ValidationException(fieldCount=$fieldCount, errorCount=$errorCount, errors=$errors)"
}
