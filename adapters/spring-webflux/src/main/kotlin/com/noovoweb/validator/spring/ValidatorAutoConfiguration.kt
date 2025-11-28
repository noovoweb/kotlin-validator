package com.noovoweb.validator.spring

import com.noovoweb.validator.MessageProvider
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.Dispatchers
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.util.Locale

/**
 * Auto-configuration for kotlin-validator in Spring Boot applications.
 *
 * Automatically configures:
 * - MessageProvider bean
 * - ValidationContext bean with Spring-aware defaults
 * - ValidationContextProvider for locale-aware validation
 * - Exception handlers for REST APIs
 *
 * Can be customized via application.properties:
 * ```
 * kotlin.validator.locale=en_US
 * ```
 */
@AutoConfiguration
@EnableConfigurationProperties(ValidatorProperties::class)
class ValidatorAutoConfiguration {
    /**
     * Provide a Spring-aware MessageProvider that uses MessageSource.
     */
    @Bean
    @ConditionalOnMissingBean
    fun messageProvider(messageSource: MessageSource): MessageProvider {
        return SpringMessageProvider(messageSource)
    }

    /**
     * Provide a default Clock bean if none exists.
     */
    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }

    /**
     * Provide a default ValidationContext bean configured from application properties.
     */
    @Bean
    @ConditionalOnMissingBean
    fun validationContext(
        properties: ValidatorProperties,
        messageProvider: MessageProvider,
        clock: Clock,
    ): ValidationContext {
        return ValidationContext(
            locale = properties.locale ?: Locale.getDefault(),
            messageProvider = messageProvider,
            dispatcher = Dispatchers.Default,
            clock = clock,
        )
    }

    /**
     * Provide ValidationContextProvider for locale-aware validation.
     */
    @Bean
    @ConditionalOnMissingBean
    fun validationContextProvider(validationContext: ValidationContext): ValidationContextProvider {
        return ValidationContextProvider(validationContext)
    }

    /**
     * Register global exception handler for ValidationException.
     */
    @Bean
    @ConditionalOnMissingBean
    fun validationExceptionHandler(): ValidationExceptionHandler {
        return ValidationExceptionHandler()
    }
}
