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
class ValidationException(
    val errors: Map<String, List<String>>,
    message: String = "Validation failed with ${errors.size} field error(s)",
) : RuntimeException(message) {
    /**
     * Get errors for a specific field.
     *
     * @param field Field name/path
     * @return List of error messages for the field (empty if no errors)
     */
    fun getFieldErrors(field: String): List<String> = errors[field] ?: emptyList()

    /**
     * Check if a specific field has errors.
     *
     * @param field Field name/path
     * @return true if the field has errors
     */
    fun hasFieldError(field: String): Boolean = errors.containsKey(field)

    /**
     * Get all error messages as a flat list.
     *
     * @return All error messages across all fields
     */
    fun getAllMessages(): List<String> = errors.values.flatten()

    /**
     * Format errors as JSON string.
     *
     * @return JSON representation of errors
     */
    fun toJson(): String {
        return buildString {
            append("{")
            errors.entries.joinTo(this, ",") { (field, messages) ->
                "\"$field\":[${messages.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }}]"
            }
            append("}")
        }
    }

    /**
     * Get the total number of error messages across all fields.
     */
    val errorCount: Int
        get() = errors.values.sumOf { it.size }

    /**
     * Get the number of fields with errors.
     */
    val fieldCount: Int
        get() = errors.size

    override fun toString(): String {
        return "ValidationException(fieldCount=$fieldCount, errorCount=$errorCount, errors=$errors)"
    }
}
