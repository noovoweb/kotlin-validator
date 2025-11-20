package com.noovoweb.validator.integration

import com.noovoweb.validator.Email
import com.noovoweb.validator.Validated
import com.noovoweb.validator.Valid

@Validated
data class NestedValidation(
    val name: String?,
    @Valid
    val contact: Contact?
)

@Validated
data class Contact(
    @Email
    val email: String?
)
