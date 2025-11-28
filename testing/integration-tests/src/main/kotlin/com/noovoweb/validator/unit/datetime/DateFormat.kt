package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.DateFormat
import com.noovoweb.validator.Validated

@Validated
data class DateFormat(
    @DateFormat("yyyy-MM-dd")
    val date: String?,
)
