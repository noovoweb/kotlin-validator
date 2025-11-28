package com.noovoweb.validator.unit.string

import com.noovoweb.validator.MaxLength
import com.noovoweb.validator.Validated

@Validated
data class MaxLength(
    @MaxLength(20)
    val name: String?,
)
