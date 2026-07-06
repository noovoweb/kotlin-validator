package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.MaxSize
import com.noovoweb.validator.Validated

@Validated
data class MaxSizeList(
    @MaxSize(5)
    val items: List<String>?
)
