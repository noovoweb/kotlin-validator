package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.IsoDateTime
import com.noovoweb.validator.Validated

@Validated
data class IsoDateTime(
    @IsoDateTime
    val datetime: String?
)
