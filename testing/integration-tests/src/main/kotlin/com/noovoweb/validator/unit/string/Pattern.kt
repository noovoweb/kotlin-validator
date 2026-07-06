package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Pattern
import com.noovoweb.validator.Validated

@Validated
data class Pattern(
    @Pattern("^[A-Z][a-z]+$")
    val name: String?
)
