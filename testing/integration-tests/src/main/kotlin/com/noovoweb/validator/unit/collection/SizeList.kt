package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.Size
import com.noovoweb.validator.Validated

@Validated
data class SizeList(
    @Size(min = 2, max = 5)
    val items: List<String>?
)
