package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.MinSize
import com.noovoweb.validator.Validated

@Validated
data class MinSizeList(
    @MinSize(3)
    val items: List<String>?
)
