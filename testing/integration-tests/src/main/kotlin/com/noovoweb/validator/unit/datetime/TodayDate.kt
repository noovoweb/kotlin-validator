package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.Today
import com.noovoweb.validator.Validated
import java.time.LocalDate

@Validated
data class TodayDate(
    @Today
    val date: LocalDate?
)
