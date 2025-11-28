package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.NotContains
import com.noovoweb.validator.Validated

@Validated
data class NotContainsList(
    @NotContains("forbidden")
    val items: List<String>?,
)
