package com.noovoweb.validator.unit.string

import com.noovoweb.validator.Alpha
import com.noovoweb.validator.Validated

@Validated
data class Alpha(
    @Alpha
    val name: String?,
)
