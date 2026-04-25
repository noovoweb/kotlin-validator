package com.noovoweb.validator.unit.string

import com.noovoweb.validator.StartsWith
import com.noovoweb.validator.Validated

@Validated
data class StartsWith(
    @StartsWith("prefix")
    val name: String?,
)
