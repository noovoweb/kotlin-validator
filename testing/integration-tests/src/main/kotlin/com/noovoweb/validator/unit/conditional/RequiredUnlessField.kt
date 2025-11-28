package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.RequiredUnless
import com.noovoweb.validator.Validated

@Validated
data class RequiredUnlessField(
    val paymentMethod: String?,
    @RequiredUnless("paymentMethod", "cash")
    val cardNumber: String?,
)
