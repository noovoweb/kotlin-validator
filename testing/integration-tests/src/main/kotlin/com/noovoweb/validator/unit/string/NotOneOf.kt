package com.noovoweb.validator.unit.string

import com.noovoweb.validator.NotOneOf
import com.noovoweb.validator.Validated

@Validated
data class NotOneOf(
    @NotOneOf(["forbidden1", "forbidden2", "forbidden3"])
    val name: String?,
)
