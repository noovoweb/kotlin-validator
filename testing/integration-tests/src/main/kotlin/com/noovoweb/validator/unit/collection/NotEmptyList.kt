package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.NotEmpty
import com.noovoweb.validator.Validated

@Validated
data class NotEmptyList(
    @NotEmpty
    val items: List<String>?,
)
