package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Validated

@Validated
data class IntegerValue(
    @com.noovoweb.validator.Integer
    val value: Double?
)
