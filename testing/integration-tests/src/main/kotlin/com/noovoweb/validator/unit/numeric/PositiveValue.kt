package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Positive
import com.noovoweb.validator.Validated

@Validated
data class PositiveValue(
    @Positive
    val value: Int?,
)
