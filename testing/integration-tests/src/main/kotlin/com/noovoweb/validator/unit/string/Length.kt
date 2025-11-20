package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Validated
import com.noovoweb.validator.Length

@Validated
data class Length(
    @Length(min = 5, max = 10)
    val name: String?
)