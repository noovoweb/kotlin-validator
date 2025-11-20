package com.noovoweb.validator.spring

import com.noovoweb.validator.ValidationContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import java.util.*

/**
 * Provider for ValidationContext in reactive WebFlux applications.
 * 
 * Unlike Spring MVC, WebFlux doesn't require request-scoped proxies since
 * it's reactive by nature. This provider offers a clean way to create
 * localized contexts from ServerRequest objects.
 * 
 * The locale is extracted from the ServerRequest's Accept-Language header
 * via the LocaleContext, providing automatic i18n support.
 */
@Component
class ValidationContextProvider(
    private val baseContext: ValidationContext
) {
    
    /**
     * Get a ValidationContext with locale from the ServerRequest.
     * 
     * Extracts locale from the Accept-Language header and creates
     * a new context with all other settings preserved.
     */
    fun get(request: ServerRequest): ValidationContext {
        val locale = request.exchange().localeContext.locale ?: Locale.ENGLISH
        return baseContext.withLocale(locale)
    }
    
    /**
     * Get the base ValidationContext without locale resolution.
     * 
     * Useful when you want to use a specific locale or when
     * locale doesn't matter for the validation.
     */
    fun getBase(): ValidationContext = baseContext
}
