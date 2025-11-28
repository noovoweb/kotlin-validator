package com.noovoweb.validator.unit.network

import com.noovoweb.validator.IPv4
import com.noovoweb.validator.Validated

@Validated
data class IPv4Address(
    @IPv4
    val address: String?,
)
