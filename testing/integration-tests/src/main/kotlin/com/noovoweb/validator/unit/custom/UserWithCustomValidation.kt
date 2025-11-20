package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Required
import com.noovoweb.validator.Validated

@Validated
data class UserWithCustomValidation(
    @Required
    @CustomValidator(validator = "com.noovoweb.validator.unit.custom.CustomValidators::validateUsername")
    val username: String?
)
