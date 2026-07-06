package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.RequiredIf
import com.noovoweb.validator.Validated

@Validated
data class RequiredIfField(
    val shipToAddress: String?,
    @RequiredIf("shipToAddress", "other")
    val customAddress: String?
)
