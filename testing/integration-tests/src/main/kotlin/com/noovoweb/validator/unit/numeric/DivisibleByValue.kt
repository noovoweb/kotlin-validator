package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.DivisibleBy
import com.noovoweb.validator.Validated

@Validated
data class DivisibleByValue(
    @DivisibleBy(5)
    val value: Int?
)
