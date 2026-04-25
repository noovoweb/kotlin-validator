package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Length
import com.noovoweb.validator.Validated

@Validated
data class Length(
    @Length(min = 5, max = 10)
    val name: String?,
)
