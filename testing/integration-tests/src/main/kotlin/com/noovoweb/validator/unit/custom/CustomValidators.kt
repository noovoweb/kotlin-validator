package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.ValidationContext

object CustomValidators {
    suspend fun validateUsername(
        value: String?,
        @Suppress("UNUSED_PARAMETER") context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        return value.length >= MIN_USERNAME_LENGTH && value.all { it.isLetterOrDigit() || it == '_' }
    }

    suspend fun validateEvenNumber(
        value: Int?,
        @Suppress("UNUSED_PARAMETER") context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        return value % 2 == 0
    }

    suspend fun validateStrongPassword(
        value: String?,
        @Suppress("UNUSED_PARAMETER") context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        return value.length >= MIN_PASSWORD_LENGTH &&
            value.any { it.isUpperCase() } &&
            value.any { it.isLowerCase() } &&
            value.any { it.isDigit() } &&
            value.any { !it.isLetterOrDigit() }
    }

    private const val MIN_USERNAME_LENGTH = 3
    private const val MIN_PASSWORD_LENGTH = 8
}
