package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.Same
import com.noovoweb.validator.Validated

@Validated
data class SameField(
    val password: String?,
    @Same("password")
    val passwordConfirmation: String?,
)
