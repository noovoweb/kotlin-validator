package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.RequiredWithout
import com.noovoweb.validator.Validated

@Validated
data class RequiredWithoutField(
    val email: String?,
    val phone: String?,
    @RequiredWithout(["email", "phone"])
    val mailingAddress: String?
)
