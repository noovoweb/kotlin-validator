package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Min
import com.noovoweb.validator.Validated

@Validated
data class MinValue(
    @Min(10.0)
    val value: Int?
)
