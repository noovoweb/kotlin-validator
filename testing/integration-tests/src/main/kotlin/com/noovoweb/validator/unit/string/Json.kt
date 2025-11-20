package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Json
import com.noovoweb.validator.Validated

@Validated
data class Json(
    @Json
    val name: String?
)
