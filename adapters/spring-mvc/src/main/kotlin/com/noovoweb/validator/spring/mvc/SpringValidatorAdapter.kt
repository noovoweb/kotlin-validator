package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.runBlocking
import org.springframework.validation.Errors
import org.springframework.validation.Validator

/**
 * Adapter that allows kotlin-validator to integrate with Spring MVC's validation framework.
 *
 * Wraps a GeneratedValidator and makes it compatible with Spring's Validator interface,
 * allowing it to be used with @Valid and @Validated annotations in Spring MVC controllers.
 *
 * **Suspend Function Support**: This adapter supports suspend functions by using
 * runBlocking to bridge the gap between Spring MVC's blocking API and kotlin-validator's
 * suspending functions. This allows validation with I/O operations (database checks,
 * external API calls) while maintaining Spring MVC compatibility.
 *
 * Example usage:
 * ```kotlin
 * @Configuration
 * class ValidatorConfig {
 *     @Bean
 *     fun userValidator(context: ValidationContext) = SpringValidatorAdapter(
 *         validator = UserValidator(),
 *         context = context,
 *         targetClass = User::class.java
 *     )
 * }
 *
 * @RestController
 * class UserController {
 *     @PostMapping("/users")
 *     fun createUser(@Valid @RequestBody user: User): User {
 *         // Validation happens automatically before this method
 *         // Suspend functions in validators are executed via runBlocking
 *         return userService.create(user)
 *     }
 * }
 * ```
 *
 * @param T The type being validated
 * @property validator The generated validator instance
 * @property context Validation context with configuration
 * @property targetClass The class type this validator handles
 */
class SpringValidatorAdapter<T : Any>(
    private val validator: GeneratedValidator<T>,
    private val context: ValidationContext = ValidationContext(),
    private val targetClass: Class<T>,
) : Validator {
    /**
     * Check if this validator supports the given class.
     */
    override fun supports(clazz: Class<*>): Boolean {
        return targetClass.isAssignableFrom(clazz)
    }

    /**
     * Validate the target object and populate Spring's Errors object.
     *
     * **Suspend Function Support**: Uses runBlocking to execute suspend validation functions.
     * This is acceptable in Spring MVC which is blocking by default. The validator can
     * perform async I/O operations internally while presenting a blocking API to Spring.
     */
    override fun validate(
        target: Any,
        errors: Errors,
    ) {
        @Suppress("UNCHECKED_CAST")
        val typedTarget = target as T

        try {
            // Execute suspend validation using runBlocking
            // This allows validators to use suspend functions for I/O operations
            runBlocking {
                validator.validate(typedTarget, context)
            }
        } catch (ex: ValidationException) {
            // Convert ValidationException errors to Spring Errors
            ex.errors.forEach { (field, messages) ->
                messages.forEach { message ->
                    errors.rejectValue(field, "validation.error", message)
                }
            }
        }
    }
}
