package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.Distinct
import com.noovoweb.validator.Validated

@Validated
data class DistinctList(
    @Distinct
    val items: List<String>?,
)
