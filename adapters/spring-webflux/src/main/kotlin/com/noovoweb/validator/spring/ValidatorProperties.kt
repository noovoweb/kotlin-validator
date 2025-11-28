package com.noovoweb.validator.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Locale

/**
 * Configuration properties for kotlin-validator.
 *
 * Configure in application.properties or application.yml:
 * ```properties
 * kotlin.validator.locale=en_US
 * ```
 *
 * Or in YAML:
 * ```yaml
 * kotlin:
 *   validator:
 *     locale: en_US
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
)
