package com.noovoweb.validator.unit.network

import com.noovoweb.validator.IPv6
import com.noovoweb.validator.Validated

@Validated
data class IPv6Address(
    @IPv6
    val address: String?,
)
