package com.noovoweb.validator.unit.boolean

import com.noovoweb.validator.Accepted
import com.noovoweb.validator.Validated

@Validated
data class AcceptedValue(
    @Accepted
    val value: Boolean?
)
