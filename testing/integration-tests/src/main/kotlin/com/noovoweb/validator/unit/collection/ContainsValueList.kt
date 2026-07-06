package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ContainsValue
import com.noovoweb.validator.Validated

@Validated
data class ContainsValueList(
    @ContainsValue("required")
    val items: List<String>?
)
