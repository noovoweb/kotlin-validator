package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Ascii
import com.noovoweb.validator.Validated

@Validated
data class Ascii(
    @Ascii
    val name: String?
)
