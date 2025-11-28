package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.Required
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated

@Validated
data class NestedObject(
    @Required
    val name: String?,
    @Valid
    val address: Address?,
)

@Validated
data class Address(
    @Required
    val street: String?,
    @Required
    val city: String?,
)
