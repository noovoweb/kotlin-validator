package com.noovoweb.validator.integration

import com.noovoweb.validator.Accepted
import com.noovoweb.validator.Alpha
import com.noovoweb.validator.Email
import com.noovoweb.validator.Max
import com.noovoweb.validator.MaxLength
import com.noovoweb.validator.Min
import com.noovoweb.validator.MinLength
import com.noovoweb.validator.Pattern
import com.noovoweb.validator.Required
import com.noovoweb.validator.Same
import com.noovoweb.validator.Validated

@Validated
data class RegisterRequest(
    @Email
    @MaxLength(20)
    val email: String?,
    @Required
    val password: String?,
    @Required
    @Same("password")
    val passwordConfirmation: String?,
    @Required
    @MinLength(2)
    @MaxLength(50)
    @Alpha
    val firstName: String?,
    @MinLength(2)
    @MaxLength(50)
    @Alpha
    val lastName: String?,
    @Required
    @Min(18.0)
    @Max(120.0)
    val age: Int?,
    @Required
    @Pattern(value = """^\s*\+?[1-9]\d{1,14}\s*$""")
    val phoneNumber: String?,
    @Accepted
    val acceptTerms: Boolean?,
)
