package com.noovoweb.validator.unit.string

import com.noovoweb.validator.MinLength
import com.noovoweb.validator.Validated

@Validated
data class MinLength(
    @MinLength(5)
    val name: String?,
)
