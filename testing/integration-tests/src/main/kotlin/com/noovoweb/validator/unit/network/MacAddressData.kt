package com.noovoweb.validator.unit.network

import com.noovoweb.validator.MacAddress
import com.noovoweb.validator.Validated

@Validated
data class MacAddressData(
    @MacAddress
    val address: String?,
)
