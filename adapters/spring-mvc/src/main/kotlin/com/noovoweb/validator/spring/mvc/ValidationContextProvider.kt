package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.ValidationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.servlet.LocaleResolver

/**
 * Provider for ValidationContext in Spring MVC applications.
 *
 * This singleton bean provides a clean, performant way to create
 * localized validation contexts from HTTP requests. The locale is
 * extracted from the Accept-Language header via Spring's LocaleResolver.
 *
 * **Performance**: Unlike request-scoped beans, this singleton approach:
 * - Avoids CGLIB proxy overhead
 * - Eliminates per-request object creation
 * - Is thread-safe without synchronization (immutable state)
 *
 * **Usage in Controllers**:
 * ```kotlin
 * @RestController
 * class MyController(
 *     private val contextProvider: ValidationContextProvider
 * ) {
 *     @PostMapping("/validate")
 *     fun validate(@RequestBody request: MyRequest, httpRequest: HttpServletRequest) {
 *         MyRequestValidator().validate(request, contextProvider.get(httpRequest))
 *     }
 * }
 * ```
 */
@Component
public class ValidationContextProvider(
    private val baseContext: ValidationContext,
    private val localeResolver: LocaleResolver,
) {
    /**
     * Get a ValidationContext with locale from the HttpServletRequest.
     *
     * Extracts locale from the Accept-Language header via the LocaleResolver
     * and creates a new context with all other settings preserved.
     *
     * @param request The current HTTP request
     * @return ValidationContext with the request's locale
     */
    public fun get(request: HttpServletRequest): ValidationContext {
        val locale = localeResolver.resolveLocale(request)
        return baseContext.withLocale(locale)
    }

    /**
     * Get the base ValidationContext without locale resolution.
     *
     * Useful when you want to use a specific locale or when
     * locale doesn't matter for the validation.
     *
     * @return The base ValidationContext
     */
    public fun getBase(): ValidationContext = baseContext
}
