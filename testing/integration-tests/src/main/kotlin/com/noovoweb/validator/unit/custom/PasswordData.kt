package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.MinLength
import com.noovoweb.validator.Required
import com.noovoweb.validator.Validated

@Validated
data class PasswordData(
    @Required
    @MinLength(8)
    @CustomValidator(
        validator = "com.noovoweb.validator.unit.custom.CustomValidators::validateStrongPassword",
        message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    val password: String?
)
