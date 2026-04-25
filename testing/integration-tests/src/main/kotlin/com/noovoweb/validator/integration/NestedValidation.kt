package com.noovoweb.validator.integration

import com.noovoweb.validator.Email
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated

@Validated
data class NestedValidation(
    val name: String?,
    @Valid
    val contact: Contact?,
)

@Validated
data class Contact(
    @Email
    val email: String?,
)
