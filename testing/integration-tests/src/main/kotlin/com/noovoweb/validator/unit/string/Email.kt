package com.noovoweb.validator.unit.string

import com.noovoweb.validator.*
import com.noovoweb.validator.Email

@Validated
data class Email(
    @Email
    val email: String?
)