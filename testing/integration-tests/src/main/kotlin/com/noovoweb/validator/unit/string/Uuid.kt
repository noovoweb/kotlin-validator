package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Uuid
import com.noovoweb.validator.Validated

@Validated
data class Uuid(
    @Uuid
    val name: String?,
)
