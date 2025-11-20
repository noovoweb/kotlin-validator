package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Required
import com.noovoweb.validator.Validated

@Validated
data class Required(
    @Required
    val name: String?
)