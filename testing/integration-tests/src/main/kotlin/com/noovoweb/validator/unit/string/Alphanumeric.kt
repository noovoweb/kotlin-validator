package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Alphanumeric
import com.noovoweb.validator.Validated

@Validated
data class Alphanumeric(
    @Alphanumeric
    val name: String?,
)
