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
 *
 * Can be customized via application.properties:
 * ```
 * kotlin.validator.locale=en_US
 * ```
 */
@AutoConfiguration
@EnableConfigurationProperties(ValidatorProperties::class)
public class ValidatorAutoConfiguration {
    /**
     * Provide a Spring-aware MessageProvider that uses MessageSource.
     */
    @Bean
    @ConditionalOnMissingBean
    public fun messageProvider(messageSource: MessageSource): MessageProvider = SpringMessageProvider(messageSource)

    /**
     * Provide a default Clock bean if none exists.
     */
    @Bean
    @ConditionalOnMissingBean
    public fun clock(): Clock = Clock.systemDefaultZone()

    /**
     * Provide a default ValidationContext bean configured from application properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public fun validationContext(
        properties: ValidatorProperties,
        messageProvider: MessageProvider,
        clock: Clock,
    ): ValidationContext = ValidationContext(
        locale = properties.locale ?: Locale.getDefault(),
        messageProvider = messageProvider,
        dispatcher = Dispatchers.Default,
        clock = clock,
    )

    /**
     * Provide ValidationContextProvider for locale-aware validation.
     */
    @Bean
    @ConditionalOnMissingBean
    public fun validationContextProvider(validationContext: ValidationContext): ValidationContextProvider = ValidationContextProvider(validationContext)
}
