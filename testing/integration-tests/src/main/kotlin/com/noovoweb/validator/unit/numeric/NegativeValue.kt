package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Negative
import com.noovoweb.validator.Validated

@Validated
data class NegativeValue(
    @Negative
    val value: Int?
)
