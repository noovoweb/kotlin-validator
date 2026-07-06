package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Between
import com.noovoweb.validator.Validated

@Validated
data class BetweenValue(
    @Between(min = 10.0, max = 100.0)
    val value: Int?
)
