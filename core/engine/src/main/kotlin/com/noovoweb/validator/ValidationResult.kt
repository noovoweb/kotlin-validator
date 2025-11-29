package com.noovoweb.validator

/**
 * Sealed class representing the result of validation.
 *
 * Provides a functional API for handling validation results without exceptions.
 *
 * @param T The type of the validated value
 */
public sealed class ValidationResult<out T> {
    public data class Success<T>(public val value: T) : ValidationResult<T>()

    public data class Failure(public val errors: Map<String, List<ValidationError>>) : ValidationResult<Nothing>()

    public fun getOrThrow(): T =
        when (this) {
            is Success -> value
            is Failure -> throw ValidationException(
                errors.mapValues { (_, errors) -> errors.map { it.message } },
            )
        }

    public fun getOrNull(): T? =
        when (this) {
            is Success -> value
            is Failure -> null
        }

    public fun getOrDefault(defaultValue: @UnsafeVariance T): T =
        when (this) {
            is Success -> value
            is Failure -> defaultValue
        }

    public inline fun getOrElse(defaultValue: (Map<String, List<ValidationError>>) -> @UnsafeVariance T): @UnsafeVariance T =
        when (this) {
            is Success -> value
            is Failure -> defaultValue(errors)
        }

    public inline fun onSuccess(block: (T) -> Unit): ValidationResult<T> {
        if (this is Success) block(value)
        return this
    }

    public inline fun onFailure(block: (Map<String, List<ValidationError>>) -> Unit): ValidationResult<T> {
        if (this is Failure) block(errors)
        return this
    }

    public inline fun <R> map(transform: (T) -> R): ValidationResult<R> =
        when (this) {
            is Success -> Success(transform(value))
            is Failure -> this
        }

    public inline fun <R> flatMap(transform: (T) -> ValidationResult<R>): ValidationResult<R> =
        when (this) {
            is Success -> transform(value)
            is Failure -> this
        }

    public inline fun mapErrors(
        transform: (Map<String, List<ValidationError>>) -> Map<String, List<ValidationError>>,
    ): ValidationResult<T> =
        when (this) {
            is Success -> this
            is Failure -> Failure(transform(errors))
        }

    public inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Map<String, List<ValidationError>>) -> R,
    ): R =
        when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(errors)
        }

    public fun isSuccess(): Boolean = this is Success

    public fun isFailure(): Boolean = this is Failure
}
