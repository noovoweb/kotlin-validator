package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Decimal
import com.noovoweb.validator.Validated

@Validated
data class DecimalValue(
    @Decimal
    val value: Double?,
)
