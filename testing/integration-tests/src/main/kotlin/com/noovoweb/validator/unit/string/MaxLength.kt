package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Validated
import com.noovoweb.validator.MaxLength

@Validated
data class MaxLength(
    @MaxLength(20)
    val name: String?
)