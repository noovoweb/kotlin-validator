package com.noovoweb.validator

/**
 * Sealed class representing the result of validation.
 *
 * Provides a functional API for handling validation results without exceptions.
 *
 * @param T The type of the validated value
 */
sealed class ValidationResult<out T> {

    /**
     * Represents successful validation.
     *
     * @property value The validated value
     */
    data class Success<T>(val value: T) : ValidationResult<T>()

    /**
     * Represents failed validation.
     *
     * @property errors Map of field paths to validation errors
     */
    data class Failure(val errors: Map<String, List<ValidationError>>) : ValidationResult<Nothing>()

    /**
     * Returns the value if successful, throws ValidationException if failed.
     *
     * @return The validated value
     * @throws ValidationException if validation failed
     */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw ValidationException(
            errors.mapValues { (_, errors) -> errors.map { it.message } }
        )
    }

    /**
     * Returns the value if successful, null if failed.
     *
     * @return The validated value or null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * Returns the value if successful, or the default value if failed.
     *
     * @param defaultValue Value to return on failure
     * @return The validated value or default
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue
    }

    /**
     * Returns the value if successful, or computes a default value if failed.
     *
     * @param defaultValue Lambda to compute default value
     * @return The validated value or computed default
     */
    inline fun getOrElse(defaultValue: (Map<String, List<ValidationError>>) -> @UnsafeVariance T): @UnsafeVariance T = when (this) {
        is Success -> value
        is Failure -> defaultValue(errors)
    }

    /**
     * Execute block if validation succeeded.
     *
     * @param block Lambda to execute with the validated value
     * @return This result for chaining
     */
    inline fun onSuccess(block: (T) -> Unit): ValidationResult<T> {
        if (this is Success) block(value)
        return this
    }

    /**
     * Execute block if validation failed.
     *
     * @param block Lambda to execute with the error map
     * @return This result for chaining
     */
    inline fun onFailure(block: (Map<String, List<ValidationError>>) -> Unit): ValidationResult<T> {
        if (this is Failure) block(errors)
        return this
    }

    /**
     * Transform successful result.
     *
     * @param transform Transformation function
     * @return Transformed result
     */
    inline fun <R> map(transform: (T) -> R): ValidationResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Transform successful result with another ValidationResult.
     *
     * @param transform Transformation function returning ValidationResult
     * @return Transformed result
     */
    inline fun <R> flatMap(transform: (T) -> ValidationResult<R>): ValidationResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    /**
     * Transform failed result.
     *
     * @param transform Transformation function for errors
     * @return Result with transformed errors
     */
    inline fun mapErrors(
        transform: (Map<String, List<ValidationError>>) -> Map<String, List<ValidationError>>
    ): ValidationResult<T> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(errors))
    }

    /**
     * Fold the result into a single value.
     *
     * @param onSuccess Handler for success case
     * @param onFailure Handler for failure case
     * @return The folded value
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Map<String, List<ValidationError>>) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(errors)
    }

    /**
     * Check if validation was successful.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check if validation failed.
     */
    fun isFailure(): Boolean = this is Failure
}
