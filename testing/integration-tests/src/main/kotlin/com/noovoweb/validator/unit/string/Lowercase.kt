package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Lowercase
import com.noovoweb.validator.Validated

@Validated
data class Lowercase(
    @Lowercase
    val name: String?
)
