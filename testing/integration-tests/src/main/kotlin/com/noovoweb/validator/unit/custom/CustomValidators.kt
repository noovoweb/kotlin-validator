package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.ValidationContext

object CustomValidators {
    suspend fun validateUsername(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        return value.length >= 3 && value.all { it.isLetterOrDigit() || it == '_' }
    }

    suspend fun validateEvenNumber(value: Int?, context: ValidationContext): Boolean {
        if (value == null) return true
        return value % 2 == 0
    }

    suspend fun validateStrongPassword(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        return value.length >= 8 && 
               value.any { it.isUpperCase() } &&
               value.any { it.isLowerCase() } &&
               value.any { it.isDigit() } &&
               value.any { !it.isLetterOrDigit() }
    }
}
