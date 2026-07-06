package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Even
import com.noovoweb.validator.Validated

@Validated
data class EvenValue(
    @Even
    val value: Int?
)
