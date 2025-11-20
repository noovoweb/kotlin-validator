package com.noovoweb.validator

/**
 * Enables custom validation logic for a field using an inline validator function.
 *
 * This annotation allows you to define custom validation logic without creating a separate validator class,
 * making it perfect for simple custom logic or business-specific validators.
 *
 * The validator lambda receives the field value and ValidationContext, returning true if valid,
 * false if invalid. You can throw ValidationException for custom error messages.
 *
 * **Key Features:**
 * - Inline validation logic - no separate validator classes needed
 * - Access to ValidationContext for locale, clock, and configuration
 * - Full type safety - validates at compile time
 * - Composable - combine with other validators
 * - Non-blocking - supports async/await patterns
 *
 * **Usage:**
 *
 * Basic custom validation:
 * ```kotlin
 * @Validated
 * data class User(
 *     @CustomValidator(validator = "com.example.validators.UserValidators::validateUsername")
 *     val username: String?
 * )
 * ```
 *
 * With error message:
 * ```kotlin
 * @Validated
 * data class Product(
 *     @CustomValidator(
 *         validator = "com.example.validators.ProductValidators::validatePrice",
 *         message = "Price must follow business validation requirements"
 *     )
 *     val price: Double?
 * )
 * ```
 *
 * **Creating a validator function:**
 *
 * ```kotlin
 * object UserValidators {
 *     suspend fun validateUsername(value: String?, context: ValidationContext): Boolean {
 *         if (value == null) return true  // null handling is separate
 *         return value.length >= 3 && value.all { it.isLetterOrDigit() }
 *     }
 * }
 * ```
 *
 * **For complex errors with multiple issues:**
 *
 * ```kotlin
 * object ProductValidators {
 *     suspend fun validatePrice(value: Double?, context: ValidationContext): Boolean {
 *         if (value == null) return true
 *
 *         val errors = mutableListOf<String>()
 *         if (value < 0) errors.add("Price cannot be negative")
 *         if (value > 999999) errors.add("Price exceeds maximum allowed value")
 *
 *         return if (errors.isEmpty()) {
 *             true
 *         } else {
 *             throw ValidationException(mapOf(
 *                 "price" to errors
 *             ))
 *         }
 *     }
 * }
 * ```
 *
 * **Advanced: Using ValidationContext for locale-aware validation:**
 *
 * ```kotlin
 * object LocalizedValidators {
 *     suspend fun validatePhoneNumber(value: String?, context: ValidationContext): Boolean {
 *         if (value == null) return true
 *
 *         return when (context.locale.language) {
 *             "fr" -> value.matches(Regex("""\+33\d{9}"""))
 *             "us" -> value.matches(Regex("""\+1\d{10}"""))
 *             else -> value.matches(Regex("""\+\d{1,3}\d{6,}"""))
 *         }
 *     }
 * }
 * ```
 *
 * **Combining with built-in validators:**
 *
 * ```kotlin
 * @Validated
 * data class Account(
 *     @Required
 *     @Email
 *     @CustomValidator(validator = "validators::emailNotBlacklisted")
 *     val email: String?
 * )
 * ```
 *
 * **Creating reusable custom annotations (meta-annotations):**
 *
 * Instead of repeating @CustomValidator everywhere, create your own annotations:
 *
 * ```kotlin
 * // Define once in your project
 * @Target(AnnotationTarget.PROPERTY)
 * @Retention(AnnotationRetention.SOURCE)
 * @CustomValidator(
 *     validator = "com.example.validators.PasswordValidator::validateStrong",
 *     message = "password.strong_password"
 * )
 * annotation class StrongPassword
 *
 * // Use everywhere - clean and reusable!
 * @Validated
 * data class RegisterRequest(
 *     @StrongPassword  // Much cleaner than @CustomValidator(...)
 *     val password: String?
 * )
 * ```
 *
 * @param validator Fully qualified name of validator function in format:
 *                  "package.path.ClassName::functionName"
 *                  Function must be suspend and accept (T?, ValidationContext) -> Boolean
 * @param message Optional custom error message. If not provided, a generic message is used.
 *
 * @throws IllegalArgumentException if validator function is not found or has wrong signature
 *
 * Generated code will:
 * - Call the validator function for each field
 * - Catch ValidationException for detailed error handling
 * - Support parallel validation when enabled
 * - Integrate with message provider for i18n
 *
 * @see ValidationContext
 * @see ValidationException
 * @see Validated
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomValidator(
    /**
     * Fully qualified name of validator function.
     * Format: "com.example.package.ClassName::functionName"
     *
     * Function signature must be:
     * `suspend fun functionName(value: T?, context: ValidationContext): Boolean`
     *
     * Where T is the field type or a supertype.
     */
    val validator: String,

    /**
     * Optional custom error message.
     * If empty string (default), a generic message is used.
     *
     * Example: "Username must be alphanumeric and 3-20 characters"
     */
    val message: String = ""
)
