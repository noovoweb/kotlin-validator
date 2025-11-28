package com.noovoweb.validator.unit.string

import com.noovoweb.validator.OneOf
import com.noovoweb.validator.Validated

@Validated
data class OneOf(
    @OneOf(["option1", "option2", "option3"])
    val name: String?,
)
