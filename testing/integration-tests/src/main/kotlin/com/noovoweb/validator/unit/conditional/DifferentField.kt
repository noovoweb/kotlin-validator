package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.Different
import com.noovoweb.validator.Validated

@Validated
data class DifferentField(
    val currentPassword: String?,
    @Different("currentPassword")
    val newPassword: String?
)
