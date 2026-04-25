package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Validated

@Validated
data class EvenNumberData(
    @CustomValidator(validator = "com.noovoweb.validator.unit.custom.CustomValidators::validateEvenNumber")
    val number: Int?,
)
