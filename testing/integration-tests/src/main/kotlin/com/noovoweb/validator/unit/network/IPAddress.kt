package com.noovoweb.validator.unit.network

import com.noovoweb.validator.IP
import com.noovoweb.validator.Validated

@Validated
data class IPAddress(
    @IP
    val address: String?,
)
