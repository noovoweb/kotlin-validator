package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Url
import com.noovoweb.validator.Validated

@Validated
data class Url(
    @Url
    val name: String?
)