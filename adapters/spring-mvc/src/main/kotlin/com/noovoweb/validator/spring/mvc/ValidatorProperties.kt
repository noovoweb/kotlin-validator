package com.noovoweb.validator.spring.mvc

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Locale

/**
 * Configuration properties for kotlin-validator in Spring MVC.
 *
 * Configure in application.properties or application.yml:
 * ```properties
 * kotlin.validator.locale=en_US
 * kotlin.validator.use-request-locale=true
 * ```
 *
 * Or in YAML:
 * ```yaml
 * kotlin:
 *   validator:
 *     locale: en_US
 *     use-request-locale: true
 * ```
 */
@ConfigurationProperties(prefix = "kotlin.validator")
data class ValidatorProperties(
    /**
     * Default locale for validation messages.
     *
     * If not set, uses Locale.getDefault().
     * Examples: "en_US", "fr_FR", "en"
     */
    var locale: Locale? = null,
    /**
     * Whether to use request-scoped ValidationContext with locale from Accept-Language header (default: true).
     *
     * When enabled:
     * - ValidationContext is created per-request with locale from Accept-Language header
     * - Requires LocaleResolver bean (auto-configured if not present)
     * - Inject ValidationContextProvider instead of ValidationContext directly
     *
     * When disabled:
     * - Uses singleton ValidationContext with fixed locale
     * - Better for non-web applications or when locale doesn't change per request
     */
    var useRequestLocale: Boolean = true,
)
