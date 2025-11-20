package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Contains
import com.noovoweb.validator.Validated

@Validated
data class Contains(
    @Contains("substring")
    val name: String?
)
