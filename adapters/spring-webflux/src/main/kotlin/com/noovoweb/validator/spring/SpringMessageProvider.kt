package com.noovoweb.validator.spring

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.MessageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import java.util.Locale

/**
 * Spring-aware MessageProvider that delegates to Spring's MessageSource.
 *
 * This allows custom validation messages to be loaded from Spring's
 * messages.properties files, while falling back to the default
 * ValidationMessages.properties for built-in validators.
 *
 * **NON-BLOCKING**: MessageSource lookups are wrapped in withContext(Dispatchers.Default)
 * to prevent blocking the event loop, even though MessageSource is typically in-memory.
 */
public class SpringMessageProvider(
    private val messageSource: MessageSource,
) : MessageProvider {
    private val defaultProvider = DefaultMessageProvider()

    override suspend fun getMessage(
        key: String,
        args: Array<Any>?,
        locale: Locale,
    ): String {
        return withContext(Dispatchers.Default) {
            try {
                // First try Spring's MessageSource (for custom messages)
                messageSource.getMessage(key, args, locale)
            } catch (e: NoSuchMessageException) {
                // Fall back to default provider (for built-in validation messages)
                defaultProvider.getMessage(key, args, locale)
            } catch (e: Exception) {
                // Last resort: return the key itself
                key
            }
        }
    }
}
