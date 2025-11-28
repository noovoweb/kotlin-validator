package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Odd
import com.noovoweb.validator.Validated

@Validated
data class OddValue(
    @Odd
    val value: Int?,
)
