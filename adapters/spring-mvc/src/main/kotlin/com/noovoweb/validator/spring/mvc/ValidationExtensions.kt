package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.LocaleResolver

/**
 * Extension function to create a localized ValidationContext from the request's locale.
 *
 * Requires a LocaleResolver to extract the locale from the request.
 */
fun ValidationContext.withLocale(
    request: HttpServletRequest,
    localeResolver: LocaleResolver,
): ValidationContext {
    val locale = localeResolver.resolveLocale(request)
    return this.withLocale(locale)
}

/**
 * Extension function to validate with locale extracted from the HttpServletRequest.
 *
 * This is a convenience method that automatically extracts the locale
 * from the HttpServletRequest and creates a localized validation context.
 *
 * Usage:
 * ```
 * @PostMapping("/validate")
 * suspend fun validate(@RequestBody payload: MyRequest, request: HttpServletRequest) {
 *     MyRequestValidator().validate(payload, request, validationContext, localeResolver)
 *     // ... handle successful validation
 * }
 * ```
 */
suspend fun <T> GeneratedValidator<T>.validate(
    payload: T,
    request: HttpServletRequest,
    baseContext: ValidationContext,
    localeResolver: LocaleResolver,
) {
    val localizedContext = baseContext.withLocale(request, localeResolver)
    this.validate(payload, localizedContext)
}
