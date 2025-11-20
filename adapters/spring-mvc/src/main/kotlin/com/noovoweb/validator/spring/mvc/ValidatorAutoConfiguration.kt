package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.MessageProvider
import com.noovoweb.validator.ValidationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*

/**
 * Auto-configuration for kotlin-validator in Spring MVC.
 *
 * This configuration is automatically loaded when Spring Boot detects:
 * - Spring MVC on the classpath (spring-boot-starter-web)
 * - No custom ValidationContext bean defined
 *
 * It provides:
 * - Default ValidationContext with application properties
 * - SpringMessageProvider for localized messages
 * - ValidationExceptionHandler for REST API error responses
 * - Optional request-scoped ValidationContext with Accept-Language header support
 *
 * To customize, define your own beans:
 * ```kotlin
 * @Configuration
 * class CustomValidatorConfig {
 *     @Bean
 *     fun validationContext(): ValidationContext {
 *         return ValidationContext(
 *             locale = Locale.FRENCH
 *         )
 *     }
 * }
 * ```
 *
 * For request-scoped locale support (enabled by default):
 * ```kotlin
 * @RestController
 * class MyController(
 *     private val validationContextProvider: ValidationContextProvider
 * ) {
 *     @PostMapping("/validate")
 *     suspend fun validate(@RequestBody request: MyRequest): Response {
 *         val context = validationContextProvider.get()
 *         // context now has locale from Accept-Language header
 *     }
 * }
 * ```
 */
@Configuration
@ComponentScan(basePackageClasses = [ValidatorAutoConfiguration::class])
@ConditionalOnClass(DispatcherServlet::class)
@EnableConfigurationProperties(ValidatorProperties::class)
class ValidatorAutoConfiguration {

    /**
     * Create default MessageProvider that integrates with Spring's MessageSource.
     */
    @Bean
    @ConditionalOnMissingBean(MessageProvider::class)
    fun messageProvider(messageSource: MessageSource): MessageProvider {
        return SpringMessageProvider(messageSource)
    }

    /**
     * Create default LocaleResolver that uses Accept-Language header.
     * Only created if no custom LocaleResolver exists.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(LocaleResolver::class)
    fun localeResolver(properties: ValidatorProperties): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver()
        resolver.setDefaultLocale(properties.locale ?: Locale.getDefault())
        return resolver
    }

    /**
     * Create default ValidationContext from application properties.
     */
    @Bean
    @ConditionalOnMissingBean(ValidationContext::class)
    fun validationContext(
        properties: ValidatorProperties,
        messageProvider: MessageProvider
    ): ValidationContext {
        return ValidationContext(
            locale = properties.locale ?: Locale.getDefault(),
            messageProvider = messageProvider
        )
    }

    /**
     * Register global ValidationExceptionHandler.
     */
    @Bean
    @ConditionalOnMissingBean(ValidationExceptionHandler::class)
    fun validationExceptionHandler(): ValidationExceptionHandler {
        return ValidationExceptionHandler()
    }
}
