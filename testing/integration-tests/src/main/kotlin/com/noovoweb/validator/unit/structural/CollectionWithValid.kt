package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.Required
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated

@Validated
data class CollectionWithValid(
    @Required
    val name: String?,
    @Valid(each = true)
    val items: List<Item>?
)

@Validated
data class Item(
    @Required
    val value: String?
)
