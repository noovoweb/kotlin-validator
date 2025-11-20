package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Validated
import com.noovoweb.validator.MinLength

@Validated
data class MinLength(
    @MinLength(5)
    val name: String?
)