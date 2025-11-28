package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.IsoDate
import com.noovoweb.validator.Validated

@Validated
data class IsoDate(
    @IsoDate
    val date: String?,
)
