package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Uppercase
import com.noovoweb.validator.Validated

@Validated
data class Uppercase(
    @Uppercase
    val name: String?,
)
