package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.RequiredWith
import com.noovoweb.validator.Validated

@Validated
data class RequiredWithField(
    val email: String?,
    val phone: String?,
    @RequiredWith(["email", "phone"])
    val name: String?,
)
