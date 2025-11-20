package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.Email
import com.noovoweb.validator.Nullable
import com.noovoweb.validator.Validated

@Validated
data class NullableData(
    @Nullable
    @Email
    val email: String?
)
