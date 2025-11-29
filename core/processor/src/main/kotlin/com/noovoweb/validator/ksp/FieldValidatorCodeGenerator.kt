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
        fieldPath: String,
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
            is ValidationValidatorInfo.EnumValidator -> generateEnumValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.JsonValidator -> generateJsonValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.LuhnValidator -> generateLuhnValidator(validator, property, fieldPath)
            is ValidationValidatorInfo.CreditCardValidator -> generateCreditCardValidator(validator, property, fieldPath)

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
    private fun addErrorMessage(
        validator: ValidationValidatorInfo,
        args: String? = null,
    ): CodeBlock {
        val argsCode = args ?: "null"
        val messageKey = validator.customMessage ?: validator.messageKey
        return CodeBlock.of(
            "errors.add(context.messageProvider.getMessage(%S, %L, context.locale))\n",
            messageKey,
            argsCode,
        )
    }

    /**
     * Add fail-fast logic if @FailFast is present.
     * NOTE: Returns empty - checkpoints handled by ValidatorClassGenerator.
     */
    private fun addFailFastIfNeeded(
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = CodeBlock.of("")

    /**
     * Helper to wrap validation logic based on property nullability.
     */
    private fun wrapInNullabilityCheck(
        property: PropertyInfo,
        validationLogic: CodeBlock.Builder.(valueRef: String) -> Unit,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            if (property.isNullable) {
                beginControlFlow("value?.let")
                validationLogic("it")
                endControlFlow()
            } else {
                validationLogic("value")
            }
        }.build()
    }

    /**
     * Generate a string validator with nullability and type checking handled automatically.
     * This is the primary helper for most string validators.
     */
    private fun generateStringValidator(
        property: PropertyInfo,
        fieldPath: String,
        comment: String,
        validation: CodeBlock.Builder.(valueRef: String) -> Unit,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// $comment")
            add(wrapInNullabilityCheck(property) { valueRef ->
                if (property.type.isString()) {
                    validation(valueRef)
                } else {
                    beginControlFlow("if ($valueRef is String)")
                    validation(valueRef)
                    endControlFlow()
                }
            })
        }.build()
    }

    /**
     * Generate a string validator that includes ReDoS protection (length check before regex).
     */
    private fun generateRegexStringValidator(
        property: PropertyInfo,
        fieldPath: String,
        comment: String,
        regexName: String,
        validator: ValidationValidatorInfo,
    ): CodeBlock {
        return generateStringValidator(property, fieldPath, comment) { valueRef ->
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH,
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            beginControlFlow("if (!$regexName.matches($valueRef))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
        }
    }

    /**
     * Generate a validator with nullability check only (no type check).
     * Used for validators that work on Any type.
     */
    private fun generateAnyValidator(
        property: PropertyInfo,
        fieldPath: String,
        comment: String,
        validation: CodeBlock.Builder.(valueRef: String) -> Unit,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// $comment")
            add(wrapInNullabilityCheck(property) { valueRef ->
                validation(valueRef)
            })
        }.build()
    }

    /**
     * Generate a numeric validator with nullability and type checking handled automatically.
     */
    private fun generateNumericValidator(
        property: PropertyInfo,
        fieldPath: String,
        comment: String,
        validation: CodeBlock.Builder.(valueRef: String) -> Unit,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// $comment")
            add(wrapInNullabilityCheck(property) { valueRef ->
                if (property.type.isNumeric()) {
                    validation(valueRef)
                } else {
                    beginControlFlow("when ($valueRef)")
                    addStatement("is Number -> {")
                    indent()
                    validation(valueRef)
                    unindent()
                    addStatement("}")
                    endControlFlow()
                }
            })
        }.build()
    }

    /**
     * Generate a collection validator with nullability handled and size extraction.
     */
    private fun generateCollectionValidator(
        property: PropertyInfo,
        fieldPath: String,
        comment: String,
        validation: CodeBlock.Builder.(valueRef: String) -> Unit,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// $comment")
            add(wrapInNullabilityCheck(property) { valueRef ->
                addStatement("val size = when ($valueRef) {")
                indent()
                addStatement("is Collection<*> -> $valueRef.size")
                addStatement("is Array<*> -> $valueRef.size")
                addStatement("is Map<*, *> -> $valueRef.size")
                addStatement("else -> 0")
                unindent()
                addStatement("}")
                validation(valueRef)
            })
        }.build()
    }

    // === String Validators ===

    private fun generateRequiredValidator(
        validator: ValidationValidatorInfo.RequiredValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Required")

            if (property.isNullable) {
                // Nullable property - check both null and blank (for Strings)
                if (property.type.isString()) {
                    beginControlFlow("if (value == null || value.isBlank())")
                } else {
                    // Nullable non-String - only check null
                    beginControlFlow("if (value == null)")
                }
            } else {
                // Non-nullable property
                if (property.type.isString()) {
                    // Non-nullable String - only check blank
                    beginControlFlow("if (value.isBlank())")
                } else {
                    // Non-nullable non-String type - should never be null at runtime
                    // Skip validation as Kotlin's type system guarantees non-null
                    addStatement("// Non-nullable non-String type - Kotlin guarantees non-null")
                    return@apply
                }
            }

            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
        }.build()
    }

    private fun generateEmailValidator(
        validator: ValidationValidatorInfo.EmailValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@Email", "emailRegex", validator)

    private fun generateUrlValidator(
        validator: ValidationValidatorInfo.UrlValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Url - Uses URL class validation (no ReDoS risk)") { valueRef ->
        addStatement("val isValid = %T.isValidURL($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
        beginControlFlow("if (!isValid)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateUuidValidator(
        validator: ValidationValidatorInfo.UuidValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@Uuid", "uuidRegex", validator)

    private fun generateLengthValidator(
        validator: ValidationValidatorInfo.LengthValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Length(min=${validator.min}, max=${validator.max})") { valueRef ->
        beginControlFlow("if ($valueRef.length !in %L..%L)", validator.min, validator.max)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMinLengthValidator(
        validator: ValidationValidatorInfo.MinLengthValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@MinLength(${validator.value})") { valueRef ->
        beginControlFlow("if ($valueRef.length < %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMaxLengthValidator(
        validator: ValidationValidatorInfo.MaxLengthValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@MaxLength(${validator.value})") { valueRef ->
        beginControlFlow("if ($valueRef.length > %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generatePatternValidator(
        validator: ValidationValidatorInfo.PatternValidator,
        property: PropertyInfo,
        fieldPath: String,
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

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            if (!property.type.isString()) {
                beginControlFlow("if ($valueRef is String)")
            }

            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for pattern matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.pattern.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH,
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // Use cached regex from companion object
            addStatement("// Use pre-compiled regex for performance")
            beginControlFlow("else if (!${property.name}Regex.matches($valueRef))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()

            if (!property.type.isString()) {
                endControlFlow()
            }

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateAlphaValidator(
        validator: ValidationValidatorInfo.AlphaValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@Alpha", "alphaRegex", validator)

    private fun generateAlphanumericValidator(
        validator: ValidationValidatorInfo.AlphanumericValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@Alphanumeric", "alphanumericRegex", validator)

    private fun generateAsciiValidator(
        validator: ValidationValidatorInfo.AsciiValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@Ascii", "asciiRegex", validator)

    private fun generateLowercaseValidator(
        validator: ValidationValidatorInfo.LowercaseValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Lowercase") { valueRef ->
        beginControlFlow("if ($valueRef != $valueRef.lowercase())")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateUppercaseValidator(
        validator: ValidationValidatorInfo.UppercaseValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Uppercase") { valueRef ->
        beginControlFlow("if ($valueRef != $valueRef.uppercase())")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateStartsWithValidator(
        validator: ValidationValidatorInfo.StartsWithValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@StartsWith") { valueRef ->
        beginControlFlow("if (!$valueRef.startsWith(%S))", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateEndsWithValidator(
        validator: ValidationValidatorInfo.EndsWithValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@EndsWith") { valueRef ->
        beginControlFlow("if (!$valueRef.endsWith(%S))", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateContainsValidator(
        validator: ValidationValidatorInfo.ContainsValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Contains") { valueRef ->
        beginControlFlow("if (!$valueRef.contains(%S))", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateOneOfValidator(
        validator: ValidationValidatorInfo.OneOfValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@OneOf") { valueRef ->
        val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
        addStatement("val allowedValues = setOf($valuesString)")
        beginControlFlow("if ($valueRef.toString() !in allowedValues)")
        add(addErrorMessage(validator, "arrayOf<Any>(allowedValues.joinToString(\", \"))"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateNotOneOfValidator(
        validator: ValidationValidatorInfo.NotOneOfValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@NotOneOf") { valueRef ->
        val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
        addStatement("val forbiddenValues = setOf($valuesString)")
        beginControlFlow("if ($valueRef.toString() in forbiddenValues)")
        add(addErrorMessage(validator, "arrayOf<Any>(forbiddenValues.joinToString(\", \"))"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateEnumValidator(
        validator: ValidationValidatorInfo.EnumValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Enum") { valueRef ->
        addStatement("val enumEntries = %L.entries", validator.enumClass)
        addStatement("val allowedValues = enumEntries.map { e -> e.name }")
        beginControlFlow("if ($valueRef.toString() !in allowedValues)")
        add(addErrorMessage(validator, "arrayOf<Any>(allowedValues.joinToString(\", \"))"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateJsonValidator(
        validator: ValidationValidatorInfo.JsonValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@Json - Proper JSON structure validation") { valueRef ->
        addStatement("val isValid = %T.isValidJson($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
        beginControlFlow("if (!isValid)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateLuhnValidator(
        validator: ValidationValidatorInfo.LuhnValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Luhn - Luhn algorithm validation (credit cards, IMEI, etc.)")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            if (!property.type.isString()) {
                beginControlFlow("if ($valueRef is String)")
            }

            addStatement("// Remove spaces and hyphens")
            addStatement("val digits = $valueRef.replace(\" \", \"\").replace(\"-\", \"\")")
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

            if (!property.type.isString()) {
                endControlFlow()
            }

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateCreditCardValidator(
        validator: ValidationValidatorInfo.CreditCardValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @CreditCard - Credit card validation (format + Luhn)")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            if (!property.type.isString()) {
                beginControlFlow("if ($valueRef is String)")
            }

            addStatement("val digits = $valueRef.replace(\" \", \"\").replace(\"-\", \"\")")
            addStatement("var isValid = digits.all { c -> c.isDigit() } && digits.length >= 13 && digits.length <= 19")
            
            // Luhn check
            beginControlFlow("if (isValid)")
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
            addStatement("isValid = sum %% 10 == 0")
            endControlFlow()

            // Card type validation
            beginControlFlow("if (isValid)")
            addStatement("val len = digits.length")
            addStatement("""isValid = when {
                digits.startsWith("4") && (len == 13 || len == 16) -> true
                digits.take(2).toIntOrNull() in 51..55 && len == 16 -> true
                digits.take(4).toIntOrNull() in 2221..2720 && len == 16 -> true
                (digits.startsWith("34") || digits.startsWith("37")) && len == 15 -> true
                (digits.startsWith("6011") || digits.startsWith("65") || digits.take(3).toIntOrNull() in 644..649) && len == 16 -> true
                digits.take(6).toIntOrNull() in 622126..622925 && len == 16 -> true
                digits.take(3).toIntOrNull() in 300..305 && len == 14 -> true
                (digits.startsWith("36") || digits.startsWith("38")) && len == 14 -> true
                digits.take(4).toIntOrNull() in 3528..3589 && len == 16 -> true
                else -> false
            }""")
            endControlFlow()

            beginControlFlow("if (!isValid)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()

            if (!property.type.isString()) {
                endControlFlow()
            }

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    // === Numeric Validators ===

    private fun generateMinValidator(
        validator: ValidationValidatorInfo.MinValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Min(${validator.value})") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() < %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMaxValidator(
        validator: ValidationValidatorInfo.MaxValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Max(${validator.value})") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() > %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateBetweenValidator(
        validator: ValidationValidatorInfo.BetweenValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Between(${validator.min}, ${validator.max})") { valueRef ->
        addStatement("val numValue = $valueRef.toDouble()")
        beginControlFlow("if (numValue !in %L..%L)", validator.min, validator.max)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generatePositiveValidator(
        validator: ValidationValidatorInfo.PositiveValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Positive") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() <= 0)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateNegativeValidator(
        validator: ValidationValidatorInfo.NegativeValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Negative") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() >= 0)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateZeroValidator(
        validator: ValidationValidatorInfo.ZeroValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Zero") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() != 0.0)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateIntegerValidator(
        validator: ValidationValidatorInfo.IntegerValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Integer")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            
            beginControlFlow("when ($valueRef)")
            addStatement("is Int, is Long, is Short, is Byte -> { /* Always valid */ }")
            addStatement("is Float -> {")
            indent()
            beginControlFlow("if ($valueRef %% 1 != 0f)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Double -> {")
            indent()
            beginControlFlow("if ($valueRef %% 1 != 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateDecimalValidator(
        validator: ValidationValidatorInfo.DecimalValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Decimal")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            
            beginControlFlow("when ($valueRef)")
            addStatement("is Int, is Long, is Short, is Byte -> {")
            indent()
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            unindent()
            addStatement("}")
            addStatement("is Float -> {")
            indent()
            beginControlFlow("if ($valueRef %% 1 == 0f)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Double -> {")
            indent()
            beginControlFlow("if ($valueRef %% 1 == 0.0)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateDivisibleByValidator(
        validator: ValidationValidatorInfo.DivisibleByValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@DivisibleBy(${validator.value})") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() %% %L != 0.0)", validator.value.toDouble())
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateEvenValidator(
        validator: ValidationValidatorInfo.EvenValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Even") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() %% 2 != 0.0)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateOddValidator(
        validator: ValidationValidatorInfo.OddValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateNumericValidator(property, fieldPath, "@Odd") { valueRef ->
        beginControlFlow("if ($valueRef.toDouble() %% 2 == 0.0)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateDecimalPlacesValidator(
        validator: ValidationValidatorInfo.DecimalPlacesValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DecimalPlaces - validates string representation has exact decimal places")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let")
                    "it"
                } else {
                    "value"
                }

            
            beginControlFlow("when ($valueRef)")
            addStatement("is String -> {")
            indent()
            addStatement("val decimalIndex = $valueRef.indexOf('.')")
            beginControlFlow("if (decimalIndex == -1 || $valueRef.length - decimalIndex - 1 != %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            endControlFlow()

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    // === Boolean Validator ===

    private fun generateAcceptedValidator(
        validator: ValidationValidatorInfo.AcceptedValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Accepted")

            val valueRef =
                if (property.isNullable) {
                    beginControlFlow("value?.let { v ->")
                    "v"
                } else {
                    "value"
                }

            // Generate type-specific check based on property type
            when {
                property.type.qualifiedName == "kotlin.Boolean" -> {
                    addStatement("val isAccepted = $valueRef")
                }
                property.type.qualifiedName == "kotlin.String" -> {
                    addStatement("val isAccepted = $valueRef.lowercase() in setOf(\"1\", \"yes\", \"true\", \"on\")")
                }
                property.type.qualifiedName == "kotlin.Int" -> {
                    addStatement("val isAccepted = $valueRef == 1")
                }
                else -> {
                    // For Any type, use when expression
                    addStatement("val isAccepted = when ($valueRef) {")
                    indent()
                    addStatement("is Boolean -> $valueRef")
                    addStatement("is String -> $valueRef.lowercase() in setOf(\"1\", \"yes\", \"true\", \"on\")")
                    addStatement("is Int -> $valueRef == 1")
                    addStatement("else -> false")
                    unindent()
                    addStatement("}")
                }
            }
            beginControlFlow("if (!isAccepted)")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()

            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    // === Collection Validators ===

    private fun generateSizeValidator(
        validator: ValidationValidatorInfo.SizeValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateCollectionValidator(property, fieldPath, "@Size(min=${validator.min}, max=${validator.max})") { _ ->
        beginControlFlow("if (size !in %L..%L)", validator.min, validator.max)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMinSizeValidator(
        validator: ValidationValidatorInfo.MinSizeValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateCollectionValidator(property, fieldPath, "@MinSize(${validator.value})") { _ ->
        beginControlFlow("if (size < %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMaxSizeValidator(
        validator: ValidationValidatorInfo.MaxSizeValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateCollectionValidator(property, fieldPath, "@MaxSize(${validator.value})") { _ ->
        beginControlFlow("if (size > %L)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateNotEmptyValidator(
        validator: ValidationValidatorInfo.NotEmptyValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@NotEmpty") { valueRef ->
        addStatement("val isEmpty = when ($valueRef) {")
        indent()
        addStatement("is Collection<*> -> $valueRef.isEmpty()")
        addStatement("is Array<*> -> $valueRef.isEmpty()")
        addStatement("is Map<*, *> -> $valueRef.isEmpty()")
        addStatement("is String -> $valueRef.isEmpty()")
        addStatement("else -> true")
        unindent()
        addStatement("}")
        beginControlFlow("if (isEmpty)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateDistinctValidator(
        validator: ValidationValidatorInfo.DistinctValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Distinct") { valueRef ->
        beginControlFlow("when ($valueRef)")
        addStatement("is List<*> -> {")
        indent()
        beginControlFlow("if ($valueRef.size != $valueRef.distinct().size)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
        unindent()
        addStatement("}")
        addStatement("is Array<*> -> {")
        indent()
        addStatement("val arr = $valueRef as Array<*>")
        beginControlFlow("if (arr.size != arr.distinct().size)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
        unindent()
        addStatement("}")
        endControlFlow()
    }

    private fun generateContainsValueValidator(
        validator: ValidationValidatorInfo.ContainsValueValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@ContainsValue") { valueRef ->
        addStatement("val stringValues = when ($valueRef) {")
        indent()
        addStatement("is Collection<*> -> $valueRef.map { it.toString() }")
        addStatement("is Array<*> -> ($valueRef as Array<*>).map { it.toString() }")
        addStatement("else -> emptyList()")
        unindent()
        addStatement("}")
        beginControlFlow("if (%S !in stringValues)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateNotContainsValidator(
        validator: ValidationValidatorInfo.NotContainsValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@NotContains") { valueRef ->
        addStatement("val stringValues = when ($valueRef) {")
        indent()
        addStatement("is Collection<*> -> $valueRef.map { it.toString() }")
        addStatement("is Array<*> -> ($valueRef as Array<*>).map { it.toString() }")
        addStatement("else -> emptyList()")
        unindent()
        addStatement("}")
        beginControlFlow("if (%S in stringValues)", validator.value)
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    // === Date/Time Validators ===

    private fun generateDateFormatValidator(
        validator: ValidationValidatorInfo.DateFormatValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@DateFormat") { valueRef ->
        beginControlFlow("try")
        addStatement("java.time.format.DateTimeFormatter.ofPattern(%S).parse($valueRef)", validator.format)
        nextControlFlow("catch (e: Exception)")
        add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.format}\")"))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateIsoDateValidator(
        validator: ValidationValidatorInfo.IsoDateValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@IsoDate", "isoDateRegex", validator)

    private fun generateIsoDateTimeValidator(
        validator: ValidationValidatorInfo.IsoDateTimeValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@IsoDateTime", "isoDateTimeRegex", validator)

    private fun generateFutureValidator(
        validator: ValidationValidatorInfo.FutureValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Future - uses injectable Clock from context") { valueRef ->
        beginControlFlow("when ($valueRef)")
        addStatement("is java.time.LocalDate -> if (!$valueRef.isAfter(java.time.LocalDate.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        addStatement("is java.time.LocalDateTime -> if (!$valueRef.isAfter(java.time.LocalDateTime.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        addStatement("is java.time.Instant -> if (!$valueRef.isAfter(java.time.Instant.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        endControlFlow()
    }

    private fun generatePastValidator(
        validator: ValidationValidatorInfo.PastValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Past - uses injectable Clock from context") { valueRef ->
        beginControlFlow("when ($valueRef)")
        addStatement("is java.time.LocalDate -> if (!$valueRef.isBefore(java.time.LocalDate.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        addStatement("is java.time.LocalDateTime -> if (!$valueRef.isBefore(java.time.LocalDateTime.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        addStatement("is java.time.Instant -> if (!$valueRef.isBefore(java.time.Instant.now(context.clock))) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        endControlFlow()
    }

    private fun generateTodayValidator(
        validator: ValidationValidatorInfo.TodayValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Today - uses injectable Clock from context") { valueRef ->
        addStatement("val today = java.time.LocalDate.now(context.clock)")
        beginControlFlow("when ($valueRef)")
        addStatement("is java.time.LocalDate -> if (!$valueRef.isEqual(today)) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        addStatement("is java.time.LocalDateTime -> if (!$valueRef.toLocalDate().isEqual(today)) {")
        indent()
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        unindent()
        addStatement("}")
        endControlFlow()
    }

    // === Network Validators ===

    private fun generateIPv4Validator(
        validator: ValidationValidatorInfo.IPv4Validator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@IPv4 - Uses InetAddress validation (safe)") { valueRef ->
        addStatement("val isValid = %T.isValidIPv4($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
        beginControlFlow("if (!isValid)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateIPv6Validator(
        validator: ValidationValidatorInfo.IPv6Validator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@IPv6 - Uses InetAddress validation (safe)") { valueRef ->
        addStatement("val isValid = %T.isValidIPv6($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
        beginControlFlow("if (!isValid)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateIPValidator(
        validator: ValidationValidatorInfo.IPValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateStringValidator(property, fieldPath, "@IP (IPv4 or IPv6) - Uses InetAddress validation (safe)") { valueRef ->
        addStatement("val isValid = %T.isValidIP($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
        beginControlFlow("if (!isValid)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    private fun generateMacAddressValidator(
        validator: ValidationValidatorInfo.MacAddressValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateRegexStringValidator(property, fieldPath, "@MacAddress", "macRegex", validator)

    private fun generatePortValidator(
        validator: ValidationValidatorInfo.PortValidator,
        property: PropertyInfo,
        fieldPath: String,
    ): CodeBlock = generateAnyValidator(property, fieldPath, "@Port") { valueRef ->
        addStatement("val port = when ($valueRef) {")
        indent()
        addStatement("is Int -> $valueRef")
        addStatement("is String -> $valueRef.toIntOrNull()")
        addStatement("else -> null")
        unindent()
        addStatement("}")
        beginControlFlow("if (port == null || port !in 1..65535)")
        add(addErrorMessage(validator))
        add(addFailFastIfNeeded(property, fieldPath))
        endControlFlow()
    }

    // === File Validators ===

    private fun generateMimeTypeValidator(
        validator: ValidationValidatorInfo.MimeTypeValidator,
        property: PropertyInfo,
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
        fieldPath: String,
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
            addStatement("// CustomValidator threw ValidationException - map all errors to current field path")
            addStatement("// This prevents issues with hardcoded field names in custom validators")
            beginControlFlow("ex.errors.values.forEach { messages ->")
            addStatement("errors.addAll(messages)")
            endControlFlow()
            // Note: @FailFast checkpoints are now handled at ValidatorClassGenerator level
            // No need to handle fail-fast here in CustomValidator

            endControlFlow() // end try-catch
        }.build()
    }
}
