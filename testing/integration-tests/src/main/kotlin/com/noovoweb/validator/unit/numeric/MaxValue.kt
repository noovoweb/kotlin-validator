package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Max
import com.noovoweb.validator.Validated

@Validated
data class MaxValue(
    @Max(100.0)
    val value: Int?
)
