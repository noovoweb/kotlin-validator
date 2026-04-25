package com.noovoweb.validator.unit.network

import com.noovoweb.validator.Port
import com.noovoweb.validator.Validated

@Validated
data class PortNumber(
    @Port
    val port: Int?,
)
