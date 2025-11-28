package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.Validated
import com.noovoweb.validator.Zero

@Validated
data class ZeroValue(
    @Zero
    val value: Int?,
)
