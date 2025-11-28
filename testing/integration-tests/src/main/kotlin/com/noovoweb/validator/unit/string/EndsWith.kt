package com.noovoweb.validator.unit.string

import com.noovoweb.validator.EndsWith
import com.noovoweb.validator.Validated

@Validated
data class EndsWith(
    @EndsWith("suffix")
    val name: String?,
)
