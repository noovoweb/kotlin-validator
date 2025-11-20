package com.noovoweb.validator.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName

/**
 * Generates validation code for individual validators.
 *
 * Each validator type generates a CodeBlock that validates a property value
 * and adds error messages when validation fails.
 *
 * **NON-BLOCKING**: Generates code that:
 * - Wraps file I/O in withContext(Dispatchers.IO)
 * - Uses injectable Clock from context
 * - Calls suspend getMessage() for error messages
 */
class FieldValidatorCodeGenerator {

    private val withContextIO = MemberName("kotlinx.coroutines", "withContext")
    private val dispatchersIO = MemberName("kotlinx.coroutines", "Dispatchers")

    /**
     * Generate validation code for a single validator.
     *
     * @param validator The validation validator
     * @param property Property being validated
     * @param fieldPath Dot-notation path for error messages (e.g., "email", "address.city")
     * @return CodeBlock with validation logic
     */
    fun generateValidatorCode(
        validator: ValidationValidatorInfo,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return when (validator) {
            // String validators
            is ValidationValidatorInfo.RequiredValidator -> generateRequiredValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.EmailValidator -> generateEmailValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.UrlValidator -> generateUrlValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.UuidValidator -> generateUuidValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.LengthValidator -> generateLengthValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MinLengthValidator -> generateMinLengthValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MaxLengthValidator -> generateMaxLengthValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.PatternValidator -> generatePatternValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.AlphaValidator -> generateAlphaValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.AlphanumericValidator -> generateAlphanumericValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.AsciiValidator -> generateAsciiValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.LowercaseValidator -> generateLowercaseValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.UppercaseValidator -> generateUppercaseValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.StartsWithValidator -> generateStartsWithValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.EndsWithValidator -> generateEndsWithValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.ContainsValidator -> generateContainsValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.OneOfValidator -> generateOneOfValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.NotOneOfValidator -> generateNotOneOfValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.JsonValidator -> generateJsonValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.LuhnValidator -> generateLuhnValidator(validator, property, fieldPath)

            // Numeric validators
            is ValidationValidatorInfo.MinValidator -> generateMinValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MaxValidator -> generateMaxValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.BetweenValidator -> generateBetweenValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.PositiveValidator -> generatePositiveValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.NegativeValidator -> generateNegativeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.ZeroValidator -> generateZeroValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.IntegerValidator -> generateIntegerValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.DecimalValidator -> generateDecimalValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.DivisibleByValidator -> generateDivisibleByValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.EvenValidator -> generateEvenValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.OddValidator -> generateOddValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.DecimalPlacesValidator -> generateDecimalPlacesValidator(validator, property, fieldPath)

            // Boolean validator
            is ValidationValidatorInfo.AcceptedValidator -> generateAcceptedValidator(validator, property, fieldPath)

            // Collection validators
            is ValidationValidatorInfo.SizeValidator -> generateSizeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MinSizeValidator -> generateMinSizeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MaxSizeValidator -> generateMaxSizeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.NotEmptyValidator -> generateNotEmptyValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.DistinctValidator -> generateDistinctValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.ContainsValueValidator -> generateContainsValueValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.NotContainsValidator -> generateNotContainsValidator(validator, property, fieldPath)

            // Date/Time validators
            is ValidationValidatorInfo.DateFormatValidator -> generateDateFormatValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.IsoDateValidator -> generateIsoDateValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.IsoDateTimeValidator -> generateIsoDateTimeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.FutureValidator -> generateFutureValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.PastValidator -> generatePastValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.TodayValidator -> generateTodayValidator(validator, property, fieldPath)

            // Network validators
            is ValidationValidatorInfo.IPv4Validator -> generateIPv4Validator(validator, property, fieldPath)
            is ValidationValidatorInfo.IPv6Validator -> generateIPv6Validator(validator, property, fieldPath)
            is ValidationValidatorInfo.IPValidator -> generateIPValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MacAddressValidator -> generateMacAddressValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.PortValidator -> generatePortValidator(validator, property, fieldPath)

            // File validators
            is ValidationValidatorInfo.MimeTypeValidator -> generateMimeTypeValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.FileExtensionValidator -> generateFileExtensionValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.MaxFileSizeValidator -> generateMaxFileSizeValidator(validator, property, fieldPath)

            // Conditional validators
            is ValidationValidatorInfo.SameValidator -> generateSameValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.DifferentValidator -> generateDifferentValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.RequiredIfValidator -> generateRequiredIfValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.RequiredUnlessValidator -> generateRequiredUnlessValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.RequiredWithValidator -> generateRequiredWithValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.RequiredWithoutValidator -> generateRequiredWithoutValidator(validator, property, fieldPath)

            // Custom validators
            is ValidationValidatorInfo.CustomValidatorInfo -> generateCustomValidator(validator, property, fieldPath)
        }
    }

    // === Helper Methods ===

    /**
     * Generate code to add an error message.
     */
    private fun addErrorMessage(validator: ValidationValidatorInfo, args: String? = null): CodeBlock {
        return if (validator.customMessage != null) {
            // Custom message provided
            CodeBlock.of("errors.add(%S)\n", validator.customMessage)
        } else {
            // Use message provider
            val argsCode = args ?: "null"
            CodeBlock.of(
                "errors.add(context.messageProvider.getMessage(%S, %L, context.locale))\n",
                validator.messageKey,
                argsCode
            )
        }
    }

    /**
     * Add fail-fast logic if @FailFast is present.
     *
     * NOTE: This method is now DEPRECATED and returns empty CodeBlock.
     * Fail-fast checkpoints are now handled by ValidatorClassGenerator at specific positions.
     */
    private fun addFailFastIfNeeded(property: PropertyInfo, fieldPath: String): CodeBlock {
        // Checkpoints are now injected by ValidatorClassGenerator after validators
        // based on failFastPositions, not after every validator
        return CodeBlock.of("")
    }

    // === String Validators ===

    private fun generateRequiredValidator(
        validator: ValidationValidatorInfo.RequiredValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Required")
            beginControlFlow("if (value == null || (value is String && value.isBlank()))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
        }.build()
    }

    private fun generateEmailValidator(
        validator: ValidationValidatorInfo.EmailValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Email")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!emailRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateUrlValidator(
        validator: ValidationValidatorInfo.UrlValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Url - Uses URL class validation (no ReDoS risk)")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Use ValidationPatterns.isValidURL for safe validation")
            addStatement("val isValid = %T.isValidURL(it)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateUuidValidator(
        validator: ValidationValidatorInfo.UuidValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Uuid")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!uuidRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateLengthValidator(
        validator: ValidationValidatorInfo.LengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Length(min=%L, max=%L)", validator.min, validator.max)
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (it.length !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMinLengthValidator(
        validator: ValidationValidatorInfo.MinLengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MinLength(%L)", validator.value)
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (it.length < %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMaxLengthValidator(
        validator: ValidationValidatorInfo.MaxLengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MaxLength(%L)", validator.value)
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (it.length > %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generatePatternValidator(
        validator: ValidationValidatorInfo.PatternValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        // Validate pattern safety at compile time
        RegexSafety.validatePatternCompiles(validator.pattern)
        val warning = RegexSafety.validatePattern(validator.pattern)
        
        return CodeBlock.builder().apply {
            addStatement("// @Pattern")
            if (warning != null) {
                // Add warning - keep it short for readability
                addStatement("// PERFORMANCE WARNING: Pattern may be slow on long inputs. Use @MaxLength before @Pattern.")
            }
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for pattern matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.pattern.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // Use cached regex from companion object
            addStatement("// Use pre-compiled regex for performance")
            beginControlFlow("else if (!${property.name}Regex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateAlphaValidator(
        validator: ValidationValidatorInfo.AlphaValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Alpha")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!alphaRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateAlphanumericValidator(
        validator: ValidationValidatorInfo.AlphanumericValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Alphanumeric")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!alphanumericRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateAsciiValidator(
        validator: ValidationValidatorInfo.AsciiValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Ascii")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!asciiRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateLowercaseValidator(
        validator: ValidationValidatorInfo.LowercaseValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Lowercase")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (it != it.lowercase())")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateUppercaseValidator(
        validator: ValidationValidatorInfo.UppercaseValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Uppercase")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (it != it.uppercase())")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateStartsWithValidator(
        validator: ValidationValidatorInfo.StartsWithValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @StartsWith")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (!it.startsWith(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateEndsWithValidator(
        validator: ValidationValidatorInfo.EndsWithValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @EndsWith")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (!it.endsWith(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateContainsValidator(
        validator: ValidationValidatorInfo.ContainsValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Contains")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("if (!it.contains(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateOneOfValidator(
        validator: ValidationValidatorInfo.OneOfValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @OneOf")
            beginControlFlow("value?.let")
            val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
            addStatement("val allowedValues = setOf($valuesString)")
            beginControlFlow("if (it.toString() !in allowedValues)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedValues.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateNotOneOfValidator(
        validator: ValidationValidatorInfo.NotOneOfValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotOneOf")
            beginControlFlow("value?.let")
            val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
            addStatement("val forbiddenValues = setOf($valuesString)")
            beginControlFlow("if (it.toString() in forbiddenValues)")
            add(addErrorMessage(validator, "arrayOf<Any>(forbiddenValues.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateJsonValidator(
        validator: ValidationValidatorInfo.JsonValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Json - Proper JSON structure validation")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Use ValidationPatterns.isValidJson for proper validation")
            addStatement("val isValid = %T.isValidJson(it)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateLuhnValidator(
        validator: ValidationValidatorInfo.LuhnValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Luhn - Luhn algorithm validation (credit cards, IMEI, etc.)")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Remove spaces and hyphens")
            addStatement("val digits = it.replace(\" \", \"\").replace(\"-\", \"\")")
            beginControlFlow("if (digits.all { c -> c.isDigit() })")
            addStatement("var sum = 0")
            addStatement("var alternate = false")
            beginControlFlow("for (i in digits.length - 1 downTo 0)")
            addStatement("var n = digits[i].digitToInt()")
            beginControlFlow("if (alternate)")
            addStatement("n *= 2")
            beginControlFlow("if (n > 9)")
            addStatement("n -= 9")
            endControlFlow()
            endControlFlow()
            addStatement("sum += n")
            addStatement("alternate = !alternate")
            endControlFlow()
            beginControlFlow("if (sum %% 10 != 0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            nextControlFlow("else")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Numeric Validators ===

    private fun generateMinValidator(
        validator: ValidationValidatorInfo.MinValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Min(%L)", validator.value)
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() < %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMaxValidator(
        validator: ValidationValidatorInfo.MaxValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Max(%L)", validator.value)
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() > %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateBetweenValidator(
        validator: ValidationValidatorInfo.BetweenValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Between(%L, %L)", validator.min, validator.max)
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            addStatement("val numValue = it.toDouble()")
            beginControlFlow("if (numValue !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generatePositiveValidator(
        validator: ValidationValidatorInfo.PositiveValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Positive")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() <= 0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateNegativeValidator(
        validator: ValidationValidatorInfo.NegativeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Negative")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() >= 0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateZeroValidator(
        validator: ValidationValidatorInfo.ZeroValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Zero")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() != 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateIntegerValidator(
        validator: ValidationValidatorInfo.IntegerValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Integer")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Int, is Long, is Short, is Byte -> { /* Always valid */ }")
            addStatement("is Float -> {")
            indent()
            beginControlFlow("if (it %% 1 != 0f)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Double -> {")
            indent()
            beginControlFlow("if (it %% 1 != 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateDecimalValidator(
        validator: ValidationValidatorInfo.DecimalValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Decimal")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Int, is Long, is Short, is Byte -> {")
            indent()
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            unindent()
            addStatement("}")
            addStatement("is Float -> {")
            indent()
            beginControlFlow("if (it %% 1 == 0f)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Double -> {")
            indent()
            beginControlFlow("if (it %% 1 == 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateDivisibleByValidator(
        validator: ValidationValidatorInfo.DivisibleByValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DivisibleBy(%L)", validator.value)
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() %% %L != 0.0)", validator.value.toDouble())
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateEvenValidator(
        validator: ValidationValidatorInfo.EvenValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Even")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() %% 2 != 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateOddValidator(
        validator: ValidationValidatorInfo.OddValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Odd")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if (it.toDouble() %% 2 == 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateDecimalPlacesValidator(
        validator: ValidationValidatorInfo.DecimalPlacesValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DecimalPlaces - validates string representation has exact decimal places")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is String -> {")
            indent()
            addStatement("val decimalIndex = it.indexOf('.')")
            beginControlFlow("if (decimalIndex == -1 || it.length - decimalIndex - 1 != %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Boolean Validator ===

    private fun generateAcceptedValidator(
        validator: ValidationValidatorInfo.AcceptedValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Accepted")
            beginControlFlow("value?.let { v ->")
            // Generate type-specific check based on property type
            when {
                property.type.qualifiedName == "kotlin.Boolean" -> {
                    addStatement("val isAccepted = v")
                }
                property.type.qualifiedName == "kotlin.String" -> {
                    addStatement("val isAccepted = v.lowercase() in setOf(\"1\", \"yes\", \"true\", \"on\")")
                }
                property.type.qualifiedName == "kotlin.Int" -> {
                    addStatement("val isAccepted = v == 1")
                }
                else -> {
                    // For Any type, use when expression
                    addStatement("val isAccepted = when (v) {")
                    indent()
                    addStatement("is Boolean -> v")
                    addStatement("is String -> v.lowercase() in setOf(\"1\", \"yes\", \"true\", \"on\")")
                    addStatement("is Int -> v == 1")
                    addStatement("else -> false")
                    unindent()
                    addStatement("}")
                }
            }
            beginControlFlow("if (!isAccepted)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Collection Validators ===

    private fun generateSizeValidator(
        validator: ValidationValidatorInfo.SizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Size(min=%L, max=%L)", validator.min, validator.max)
            beginControlFlow("value?.let")
            addStatement("val size = when (it) {")
            indent()
            addStatement("is Collection<*> -> it.size")
            addStatement("is Array<*> -> it.size")
            addStatement("is Map<*, *> -> it.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMinSizeValidator(
        validator: ValidationValidatorInfo.MinSizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MinSize(%L)", validator.value)
            beginControlFlow("value?.let")
            addStatement("val size = when (it) {")
            indent()
            addStatement("is Collection<*> -> it.size")
            addStatement("is Array<*> -> it.size")
            addStatement("is Map<*, *> -> it.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size < %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMaxSizeValidator(
        validator: ValidationValidatorInfo.MaxSizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MaxSize(%L)", validator.value)
            beginControlFlow("value?.let")
            addStatement("val size = when (it) {")
            indent()
            addStatement("is Collection<*> -> it.size")
            addStatement("is Array<*> -> it.size")
            addStatement("is Map<*, *> -> it.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size > %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateNotEmptyValidator(
        validator: ValidationValidatorInfo.NotEmptyValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotEmpty")
            beginControlFlow("value?.let")
            addStatement("val isEmpty = when (it) {")
            indent()
            addStatement("is Collection<*> -> it.isEmpty()")
            addStatement("is Array<*> -> it.isEmpty()")
            addStatement("is Map<*, *> -> it.isEmpty()")
            addStatement("is String -> it.isEmpty()")
            addStatement("else -> true")
            unindent()
            addStatement("}")
            beginControlFlow("if (isEmpty)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateDistinctValidator(
        validator: ValidationValidatorInfo.DistinctValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Distinct")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is List<*> -> {")
            indent()
            beginControlFlow("if (it.size != it.distinct().size)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Array<*> -> {")
            indent()
            addStatement("val arrayAsList = (it as Array<*>).toList()")
            addStatement("val distinctList = arrayAsList.distinct()")
            beginControlFlow("if (it.size != distinctList.size)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateContainsValueValidator(
        validator: ValidationValidatorInfo.ContainsValueValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @ContainsValue")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Collection<*> -> {")
            indent()
            addStatement("val stringValues = (it as Collection<*>).map { elem -> elem.toString() }")
            beginControlFlow("if (%S !in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Array<*> -> {")
            indent()
            addStatement("val arrayAsList = (it as Array<*>).toList()")
            addStatement("val stringValues = arrayAsList.map { elem -> elem.toString() }")
            beginControlFlow("if (%S !in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateNotContainsValidator(
        validator: ValidationValidatorInfo.NotContainsValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotContains")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Collection<*> -> {")
            indent()
            addStatement("val stringValues = (it as Collection<*>).map { elem -> elem.toString() }")
            beginControlFlow("if (%S in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Array<*> -> {")
            indent()
            addStatement("val arrayAsList = (it as Array<*>).toList()")
            addStatement("val stringValues = arrayAsList.map { elem -> elem.toString() }")
            beginControlFlow("if (%S in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Date/Time Validators ===

    private fun generateDateFormatValidator(
        validator: ValidationValidatorInfo.DateFormatValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DateFormat")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            beginControlFlow("try")
            addStatement("java.time.format.DateTimeFormatter.ofPattern(%S).parse(it)", validator.format)
            nextControlFlow("catch (e: Exception)")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.format}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateIsoDateValidator(
        validator: ValidationValidatorInfo.IsoDateValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IsoDate")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!isoDateRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateIsoDateTimeValidator(
        validator: ValidationValidatorInfo.IsoDateTimeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IsoDateTime")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!isoDateTimeRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateFutureValidator(
        validator: ValidationValidatorInfo.FutureValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Future - uses injectable Clock from context")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.time.LocalDate -> {")
            indent()
            addStatement("val now = java.time.LocalDate.now(context.clock)")
            beginControlFlow("if (!it.isAfter(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.LocalDateTime -> {")
            indent()
            addStatement("val now = java.time.LocalDateTime.now(context.clock)")
            beginControlFlow("if (!it.isAfter(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.Instant -> {")
            indent()
            addStatement("val now = java.time.Instant.now(context.clock)")
            beginControlFlow("if (!it.isAfter(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generatePastValidator(
        validator: ValidationValidatorInfo.PastValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Past - uses injectable Clock from context")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.time.LocalDate -> {")
            indent()
            addStatement("val now = java.time.LocalDate.now(context.clock)")
            beginControlFlow("if (!it.isBefore(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.LocalDateTime -> {")
            indent()
            addStatement("val now = java.time.LocalDateTime.now(context.clock)")
            beginControlFlow("if (!it.isBefore(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.Instant -> {")
            indent()
            addStatement("val now = java.time.Instant.now(context.clock)")
            beginControlFlow("if (!it.isBefore(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateTodayValidator(
        validator: ValidationValidatorInfo.TodayValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Today - uses injectable Clock from context")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.time.LocalDate -> {")
            indent()
            addStatement("val today = java.time.LocalDate.now(context.clock)")
            beginControlFlow("if (!it.isEqual(today))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.LocalDateTime -> {")
            indent()
            addStatement("val today = java.time.LocalDate.now(context.clock)")
            beginControlFlow("if (!it.toLocalDate().isEqual(today))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Network Validators ===

    private fun generateIPv4Validator(
        validator: ValidationValidatorInfo.IPv4Validator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IPv4 - Uses InetAddress validation (safe and reliable)")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Use ValidationPatterns.isValidIPv4 for safe validation")
            addStatement("val isValid = %T.isValidIPv4(it)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateIPv6Validator(
        validator: ValidationValidatorInfo.IPv6Validator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IPv6 - Uses InetAddress validation (no ReDoS risk)")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Use ValidationPatterns.isValidIPv6 for safe validation")
            addStatement("val isValid = %T.isValidIPv6(it)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateIPValidator(
        validator: ValidationValidatorInfo.IPValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IP (IPv4 or IPv6) - Uses InetAddress validation (safe)")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            addStatement("// Use ValidationPatterns.isValidIP for safe validation")
            addStatement("val isValid = %T.isValidIP(it)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMacAddressValidator(
        validator: ValidationValidatorInfo.MacAddressValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MacAddress")
            beginControlFlow("value?.let")
            beginControlFlow("if (it is String)")
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if (it.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!macRegex.matches(it))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generatePortValidator(
        validator: ValidationValidatorInfo.PortValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Port")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is Int -> {")
            indent()
            beginControlFlow("if (it !in 1..65535)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is String -> {")
            indent()
            addStatement("val port = it.toIntOrNull()")
            beginControlFlow("if (port == null || port !in 1..65535)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === File Validators ===

    private fun generateMimeTypeValidator(
        validator: ValidationValidatorInfo.MimeTypeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
        return CodeBlock.builder().apply {
            addStatement("// @MimeType - NON-BLOCKING with IO dispatcher")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.io.File -> {")
            indent()
            addStatement("val mimeType = %M(%M.IO) {", withContextIO, dispatchersIO)
            indent()
            addStatement("java.nio.file.Files.probeContentType(it.toPath()) ?: \"application/octet-stream\"")
            unindent()
            addStatement("}")
            addStatement("val allowedTypes = arrayOf($valuesString)")
            beginControlFlow("if (mimeType !in allowedTypes)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedTypes.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.nio.file.Path -> {")
            indent()
            addStatement("val mimeType = %M(%M.IO) {", withContextIO, dispatchersIO)
            indent()
            addStatement("java.nio.file.Files.probeContentType(it) ?: \"application/octet-stream\"")
            unindent()
            addStatement("}")
            addStatement("val allowedTypes = arrayOf($valuesString)")
            beginControlFlow("if (mimeType !in allowedTypes)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedTypes.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is String -> {")
            indent()
            addStatement("val allowedTypes = arrayOf($valuesString)")
            beginControlFlow("if (it !in allowedTypes)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedTypes.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateFileExtensionValidator(
        validator: ValidationValidatorInfo.FileExtensionValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
        return CodeBlock.builder().apply {
            addStatement("// @FileExtension - Pure string check, non-blocking")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.io.File -> {")
            indent()
            addStatement("val extension = it.extension")
            addStatement("val allowedExtensions = arrayOf($valuesString)")
            beginControlFlow("if (extension !in allowedExtensions)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedExtensions.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.nio.file.Path -> {")
            indent()
            addStatement("val extension = it.fileName.toString().substringAfterLast('.', \"\")")
            addStatement("val allowedExtensions = arrayOf($valuesString)")
            beginControlFlow("if (extension !in allowedExtensions)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedExtensions.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is String -> {")
            indent()
            addStatement("val extension = it.substringAfterLast('.', \"\")")
            addStatement("val allowedExtensions = arrayOf($valuesString)")
            beginControlFlow("if (extension !in allowedExtensions)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedExtensions.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateMaxFileSizeValidator(
        validator: ValidationValidatorInfo.MaxFileSizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MaxFileSize - NON-BLOCKING with IO dispatcher")
            beginControlFlow("value?.let")
            beginControlFlow("when (it)")
            addStatement("is java.io.File -> {")
            indent()
            addStatement("val fileSize = %M(%M.IO) { it.length() }", withContextIO, dispatchersIO)
            beginControlFlow("if (fileSize > %LL)", validator.bytes)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.bytes}L)"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.nio.file.Path -> {")
            indent()
            addStatement("val fileSize = %M(%M.IO) { java.nio.file.Files.size(it) }", withContextIO, dispatchersIO)
            beginControlFlow("if (fileSize > %LL)", validator.bytes)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.bytes}L)"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()
            endControlFlow()
        }.build()
    }

    // === Conditional Validators ===

    private fun generateSameValidator(
        validator: ValidationValidatorInfo.SameValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Same - Compare with another field")
            addStatement("val otherValue = payload.%L", validator.field)
            beginControlFlow("if (value != otherValue)")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.field}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
        }.build()
    }

    private fun generateDifferentValidator(
        validator: ValidationValidatorInfo.DifferentValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Different - Must differ from another field")
            addStatement("val otherValue = payload.%L", validator.field)
            beginControlFlow("if (value == otherValue)")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.field}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
        }.build()
    }

    private fun generateRequiredIfValidator(
        validator: ValidationValidatorInfo.RequiredIfValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @RequiredIf")
            addStatement("val otherValue = payload.%L", validator.field)
            beginControlFlow("if (otherValue?.toString() == %S)", validator.value)
            beginControlFlow("if (value == null || (value is String && value.isBlank()))")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.field}\", \"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateRequiredUnlessValidator(
        validator: ValidationValidatorInfo.RequiredUnlessValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @RequiredUnless")
            addStatement("val otherValue = payload.%L", validator.field)
            beginControlFlow("if (otherValue?.toString() != %S)", validator.value)
            beginControlFlow("if (value == null || (value is String && value.isBlank()))")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.field}\", \"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateRequiredWithValidator(
        validator: ValidationValidatorInfo.RequiredWithValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @RequiredWith")
            val fieldsCheck = validator.fields.joinToString(" || ") { "payload.$it != null" }
            val fieldsArray = validator.fields.joinToString(", ") { "\"$it\"" }
            beginControlFlow("if ($fieldsCheck)")
            beginControlFlow("if (value == null || (value is String && value.isBlank()))")
            addStatement("val requiredFields = arrayOf($fieldsArray)")
            add(addErrorMessage(validator, "arrayOf<Any>(requiredFields.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateRequiredWithoutValidator(
        validator: ValidationValidatorInfo.RequiredWithoutValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @RequiredWithout")
            val fieldsCheck = validator.fields.joinToString(" && ") { "payload.$it == null" }
            val fieldsArray = validator.fields.joinToString(", ") { "\"$it\"" }
            beginControlFlow("if ($fieldsCheck)")
            beginControlFlow("if (value == null || (value is String && value.isBlank()))")
            addStatement("val requiredFields = arrayOf($fieldsArray)")
            add(addErrorMessage(validator, "arrayOf<Any>(requiredFields.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            endControlFlow()
        }.build()
    }

    private fun generateCustomValidator(
        validator: ValidationValidatorInfo.CustomValidatorInfo,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @CustomValidator - Custom validation logic")

            // Parse the validator function reference (format: "package.Class::method")
            val parts = validator.validatorFunctionFqn.split("::")
            if (parts.size != 2) {
                addStatement("// ERROR: Invalid validator function reference: %S", validator.validatorFunctionFqn)
                return@apply
            }

            val (className, methodName) = parts

            // Call the custom validator function with try-catch
            beginControlFlow("try")
            addStatement("val isValid = %L.%L(value, context)", className, methodName)
            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()

            // Catch ValidationException to collect detailed errors
            nextControlFlow("catch (ex: %T)", com.noovoweb.validator.ValidationException::class)
            addStatement("// CustomValidator threw ValidationException with detailed errors")
            addStatement("// Merge errors into the field errors list")
            beginControlFlow("ex.errors.forEach { (field, messages) ->")
            addStatement("if (field == %S) errors.addAll(messages)", fieldPath)
            endControlFlow()
            // Note: @FailFast checkpoints are now handled at ValidatorClassGenerator level
            // No need to handle fail-fast here in CustomValidator

            endControlFlow() // end try-catch
        }.build()
    }
}
