package com.noovoweb.validator

/**
 * Base interface for all generated validator classes.
 *
 * This interface is implemented by validators generated via KSP for classes
 * annotated with @Validated.
 *
 * **NON-BLOCKING**: All methods are suspend functions to support non-blocking
 * validation with I/O operations, message resolution, and parallel execution.
 *
 * @param T The type of object being validated
 *
 * Example:
 * ```kotlin
 * // User code
 * @Validated
 * data class User(
 *     @Required @Email val email: String?,
 *     @Min(18.0) val age: Int?
 * )
 *
 * // Generated code
 * class UserValidator : GeneratedValidator<User> {
 *     override suspend fun validate(payload: User, context: ValidationContext) {
 *         // ... generated validation logic ...
 *     }
 *
 *     override suspend fun validateResult(payload: User, context: ValidationContext): ValidationResult<User> {
 *         // ... generated result logic ...
 *     }
 * }
 *
 * // Usage
 * val validator = UserValidator()
 * validator.validate(user, ValidationContext())
 * ```
 */
public interface GeneratedValidator<T> {
    /**
     * Validate the payload and throw ValidationException on failure.
     *
     * This is the exception-based API. Use this when you want to handle
     * validation errors with try-catch.
     *
     * **NON-BLOCKING**: This is a suspend function that:
     * - Validates fields in parallel using async/await
     * - Resolves messages using suspend MessageProvider
     * - Performs file I/O with Dispatchers.IO
     * - Never blocks threads
     *
     * @param payload The object to validate
     * @param context Validation configuration (locale, dispatcher, clock, etc.)
     * @throws ValidationException if validation fails with error details
     *
     * Example:
     * ```kotlin
     * suspend fun registerUser(data: UserRegistration) {
     *     val validator = UserRegistrationValidator()
     *
     *     try {
     *         validator.validate(data, ValidationContext(
     *             locale = Locale.ENGLISH,
     *             parallelValidation = true,
     *             dispatcher = Dispatchers.IO
     *         ))
     *         // Validation passed
     *         saveToDatabase(data)
     *     } catch (e: ValidationException) {
     *         // Handle errors
     *         e.errors.forEach { (field, messages) ->
     *             println("$field: ${messages.joinToString()}")
     *         }
     *     }
     * }
     * ```
     */
    public suspend fun validate(
        payload: T,
        context: ValidationContext = ValidationContext(),
    )

    /**
     * Validate the payload and return a Result.
     *
     * This is the result-based API. Use this when you prefer functional-style
     * error handling without exceptions.
     *
     * **NON-BLOCKING**: This is a suspend function with the same non-blocking
     * guarantees as validate().
     *
     * @param payload The object to validate
     * @param context Validation configuration (locale, dispatcher, clock, etc.)
     * @return ValidationResult.Success with the payload, or ValidationResult.Failure with errors
     *
     * Example:
     * ```kotlin
     * suspend fun registerUser(data: UserRegistration) {
     *     val validator = UserRegistrationValidator()
     *     val context = ValidationContext(parallelValidation = true)
     *
     *     when (val result = validator.validateResult(data, context)) {
     *         is ValidationResult.Success -> {
     *             println("Valid user: ${result.value}")
     *             saveToDatabase(result.value)
     *         }
     *         is ValidationResult.Failure -> {
     *             result.errors.forEach { (field, errors) ->
     *                 errors.forEach { error ->
     *                     println("$field: ${error.message}")
     *                 }
     *             }
     *         }
     *     }
     * }
     * ```
     *
     * Functional API example:
     * ```kotlin
     * validator.validateResult(data, context)
     *     .onSuccess { user -> saveToDatabase(user) }
     *     .onFailure { errors -> logErrors(errors) }
     * ```
     */
    public suspend fun validateResult(
        payload: T,
        context: ValidationContext = ValidationContext(),
    ): ValidationResult<T>
}
