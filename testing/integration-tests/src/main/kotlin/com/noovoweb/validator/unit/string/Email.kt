package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Email
import com.noovoweb.validator.Validated

@Validated
data class Email(
    @Email
    val email: String?,
)
