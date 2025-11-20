package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.Future
import com.noovoweb.validator.Validated
import java.time.LocalDate

@Validated
data class FutureDate(
    @Future
    val date: LocalDate?
)
