package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.Past
import com.noovoweb.validator.Validated
import java.time.LocalDate

@Validated
data class PastDate(
    @Past
    val date: LocalDate?,
)
