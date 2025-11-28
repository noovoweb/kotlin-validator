package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.Email
import com.noovoweb.validator.FailFast
import com.noovoweb.validator.MaxLength
import com.noovoweb.validator.Required
import com.noovoweb.validator.Validated

@Validated
data class FailFastAtBeginning(
    @FailFast
    @Required
    @Email
    @MaxLength(50)
    val email: String?,
)

@Validated
data class FailFastAtMiddle(
    @Required
    @Email
    @FailFast
    @MaxLength(50)
    val email: String?,
)

@Validated
data class FailFastAtEnd(
    @Required
    @Email
    @MaxLength(50)
    @FailFast
    val email: String?,
)

@Validated
data class MultipleFailFastCheckpoints(
    @Required
    @FailFast // Checkpoint 1: After Required
    @Email
    @FailFast // Checkpoint 2: After Email
    @MaxLength(50)
    val email: String?,
)
