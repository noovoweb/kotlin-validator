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
     * Helper to wrap validation logic based on property nullability.
     * For nullable properties, wraps in value?.let { }
     * For non-nullable properties, uses value directly
     */
    private fun wrapInNullabilityCheck(
        property: PropertyInfo,
        validationLogic: CodeBlock.Builder.(valueRef: String) -> Unit
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
     * Helper to add type check for String validators.
     * Only adds the check if property type is not guaranteed to be String.
     */
    private fun addStringTypeCheckIfNeeded(
        property: PropertyInfo,
        valueRef: String,
        validationLogic: CodeBlock.Builder.() -> Unit
    ): CodeBlock {
        return CodeBlock.builder().apply {
            if (property.type.isString()) {
                // Property is guaranteed to be String, no type check needed
                validationLogic()
            } else {
                // Property might not be String, add type check with suppression
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
                validationLogic()
                endControlFlow()
            }
        }.build()
    }

    /**
     * Generate code to add an error message.
     */
    private fun addErrorMessage(validator: ValidationValidatorInfo, args: String? = null): CodeBlock {
        val argsCode = args ?: "null"
        val messageKey = validator.customMessage ?: validator.messageKey
        
        // Always use message provider to allow for i18n and custom message keys
        return CodeBlock.of(
            "errors.add(context.messageProvider.getMessage(%S, %L, context.locale))\n",
            messageKey,
            argsCode
        )
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
            
            if (property.isNullable) {
                // Nullable property - check both null and blank (for Strings)
                if (property.type.isString()) {
                    addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                    beginControlFlow("if (value == null || (value is String && value.isBlank()))")
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
                    // Non-nullable non-String type - only check null (should never be null, but for safety)
                    addStatement("@Suppress(%S)", "SENSELESS_COMPARISON")
                    beginControlFlow("if (value == null)")
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Email")
            
            add(wrapInNullabilityCheck(property) { valueRef ->
                add(addStringTypeCheckIfNeeded(property, valueRef) {
                    // SECURITY: Length check to prevent ReDoS on very long inputs
                    addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
                    beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
                    addStatement(
                        "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                        "field.too_long",
                        RegexSafety.MAX_PATTERN_INPUT_LENGTH
                    )
                    add(addFailFastIfNeeded(property, fieldPath))
                    endControlFlow()
                    // PERFORMANCE: Use cached regex from companion object
                    beginControlFlow("if (!emailRegex.matches($valueRef))")
                    add(addErrorMessage(validator))
                    add(addFailFastIfNeeded(property, fieldPath))
                    endControlFlow()
                })
            })
        }.build()
    }

    private fun generateUrlValidator(
        validator: ValidationValidatorInfo.UrlValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Url - Uses URL class validation (no ReDoS risk)")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            addStatement("// Use ValidationPatterns.isValidURL for safe validation")
            addStatement("val isValid = %T.isValidURL($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
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

    private fun generateUuidValidator(
        validator: ValidationValidatorInfo.UuidValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Uuid")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!uuidRegex.matches($valueRef))")
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

    private fun generateLengthValidator(
        validator: ValidationValidatorInfo.LengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Length(min=%L, max=%L)", validator.min, validator.max)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if ($valueRef.length !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
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

    private fun generateMinLengthValidator(
        validator: ValidationValidatorInfo.MinLengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MinLength(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if ($valueRef.length < %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
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

    private fun generateMaxLengthValidator(
        validator: ValidationValidatorInfo.MaxLengthValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MaxLength(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if ($valueRef.length > %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
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
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for pattern matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.pattern.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Alpha")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!alphaRegex.matches($valueRef))")
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

    private fun generateAlphanumericValidator(
        validator: ValidationValidatorInfo.AlphanumericValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Alphanumeric")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!alphanumericRegex.matches($valueRef))")
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

    private fun generateAsciiValidator(
        validator: ValidationValidatorInfo.AsciiValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Ascii")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!asciiRegex.matches($valueRef))")
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

    private fun generateLowercaseValidator(
        validator: ValidationValidatorInfo.LowercaseValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Lowercase")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if ($valueRef != $valueRef.lowercase())")
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

    private fun generateUppercaseValidator(
        validator: ValidationValidatorInfo.UppercaseValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Uppercase")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if ($valueRef != $valueRef.uppercase())")
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

    private fun generateStartsWithValidator(
        validator: ValidationValidatorInfo.StartsWithValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @StartsWith")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if (!$valueRef.startsWith(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
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

    private fun generateEndsWithValidator(
        validator: ValidationValidatorInfo.EndsWithValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @EndsWith")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if (!$valueRef.endsWith(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
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

    private fun generateContainsValidator(
        validator: ValidationValidatorInfo.ContainsValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Contains")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("if (!$valueRef.contains(%S))", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
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

    private fun generateOneOfValidator(
        validator: ValidationValidatorInfo.OneOfValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @OneOf")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
            addStatement("val allowedValues = setOf($valuesString)")
            beginControlFlow("if ($valueRef.toString() !in allowedValues)")
            add(addErrorMessage(validator, "arrayOf<Any>(allowedValues.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateNotOneOfValidator(
        validator: ValidationValidatorInfo.NotOneOfValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotOneOf")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            val valuesString = validator.values.joinToString(", ") { "\"$it\"" }
            addStatement("val forbiddenValues = setOf($valuesString)")
            beginControlFlow("if ($valueRef.toString() in forbiddenValues)")
            add(addErrorMessage(validator, "arrayOf<Any>(forbiddenValues.joinToString(\", \"))"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateJsonValidator(
        validator: ValidationValidatorInfo.JsonValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Json - Proper JSON structure validation")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            addStatement("// Use ValidationPatterns.isValidJson for proper validation")
            addStatement("val isValid = %T.isValidJson($valueRef)", ClassName("com.noovoweb.validator", "ValidationPatterns"))
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

    private fun generateLuhnValidator(
        validator: ValidationValidatorInfo.LuhnValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Luhn - Luhn algorithm validation (credit cards, IMEI, etc.)")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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

    // === Numeric Validators ===

    private fun generateMinValidator(
        validator: ValidationValidatorInfo.MinValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Min(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() < %L)", validator.value)
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

    private fun generateMaxValidator(
        validator: ValidationValidatorInfo.MaxValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Max(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() > %L)", validator.value)
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

    private fun generateBetweenValidator(
        validator: ValidationValidatorInfo.BetweenValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Between(%L, %L)", validator.min, validator.max)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            addStatement("val numValue = $valueRef.toDouble()")
            beginControlFlow("if (numValue !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
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

    private fun generatePositiveValidator(
        validator: ValidationValidatorInfo.PositiveValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Positive")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() <= 0)")
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

    private fun generateNegativeValidator(
        validator: ValidationValidatorInfo.NegativeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Negative")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() >= 0)")
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

    private fun generateZeroValidator(
        validator: ValidationValidatorInfo.ZeroValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Zero")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() != 0.0)")
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

    private fun generateIntegerValidator(
        validator: ValidationValidatorInfo.IntegerValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Integer")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Decimal")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DivisibleBy(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() %% %L != 0.0)", validator.value.toDouble())
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

    private fun generateEvenValidator(
        validator: ValidationValidatorInfo.EvenValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Even")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() %% 2 != 0.0)")
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

    private fun generateOddValidator(
        validator: ValidationValidatorInfo.OddValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Odd")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Number -> {")
            indent()
            beginControlFlow("if ($valueRef.toDouble() %% 2 == 0.0)")
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

    private fun generateDecimalPlacesValidator(
        validator: ValidationValidatorInfo.DecimalPlacesValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DecimalPlaces - validates string representation has exact decimal places")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Accepted")
            
            val valueRef = if (property.isNullable) {
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
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Size(min=%L, max=%L)", validator.min, validator.max)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            addStatement("val size = when ($valueRef) {")
            indent()
            addStatement("is Collection<*> -> $valueRef.size")
            addStatement("is Array<*> -> $valueRef.size")
            addStatement("is Map<*, *> -> $valueRef.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size !in %L..%L)", validator.min, validator.max)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.min}, ${validator.max})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateMinSizeValidator(
        validator: ValidationValidatorInfo.MinSizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MinSize(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            addStatement("val size = when ($valueRef) {")
            indent()
            addStatement("is Collection<*> -> $valueRef.size")
            addStatement("is Array<*> -> $valueRef.size")
            addStatement("is Map<*, *> -> $valueRef.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size < %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateMaxSizeValidator(
        validator: ValidationValidatorInfo.MaxSizeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @MaxSize(%L)", validator.value)
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            addStatement("val size = when ($valueRef) {")
            indent()
            addStatement("is Collection<*> -> $valueRef.size")
            addStatement("is Array<*> -> $valueRef.size")
            addStatement("is Map<*, *> -> $valueRef.size")
            addStatement("else -> 0")
            unindent()
            addStatement("}")
            beginControlFlow("if (size > %L)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(${validator.value})"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateNotEmptyValidator(
        validator: ValidationValidatorInfo.NotEmptyValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotEmpty")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            
            if (property.isNullable) {
                endControlFlow()
            }
        }.build()
    }

    private fun generateDistinctValidator(
        validator: ValidationValidatorInfo.DistinctValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Distinct")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("val arrayAsList = ($valueRef as Array<*>).toList()")
            addStatement("val distinctList = arrayAsList.distinct()")
            beginControlFlow("if (arrayAsList.size != distinctList.size)")
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

    private fun generateContainsValueValidator(
        validator: ValidationValidatorInfo.ContainsValueValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @ContainsValue")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Collection<*> -> {")
            indent()
            addStatement("val stringValues = ($valueRef as Collection<*>).map { elem -> elem.toString() }")
            beginControlFlow("if (%S !in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Array<*> -> {")
            indent()
            addStatement("val arrayAsList = ($valueRef as Array<*>).toList()")
            addStatement("val stringValues = arrayAsList.map { elem -> elem.toString() }")
            beginControlFlow("if (%S !in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
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

    private fun generateNotContainsValidator(
        validator: ValidationValidatorInfo.NotContainsValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @NotContains")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is Collection<*> -> {")
            indent()
            addStatement("val stringValues = ($valueRef as Collection<*>).map { elem -> elem.toString() }")
            beginControlFlow("if (%S in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is Array<*> -> {")
            indent()
            addStatement("val arrayAsList = ($valueRef as Array<*>).toList()")
            addStatement("val stringValues = arrayAsList.map { elem -> elem.toString() }")
            beginControlFlow("if (%S in stringValues)", validator.value)
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.value}\")"))
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

    // === Date/Time Validators ===

    private fun generateDateFormatValidator(
        validator: ValidationValidatorInfo.DateFormatValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @DateFormat")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            beginControlFlow("try")
            addStatement("java.time.format.DateTimeFormatter.ofPattern(%S).parse($valueRef)", validator.format)
            nextControlFlow("catch (e: Exception)")
            add(addErrorMessage(validator, "arrayOf<Any>(\"${validator.format}\")"))
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

    private fun generateIsoDateValidator(
        validator: ValidationValidatorInfo.IsoDateValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IsoDate")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!isoDateRegex.matches($valueRef))")
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

    private fun generateIsoDateTimeValidator(
        validator: ValidationValidatorInfo.IsoDateTimeValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @IsoDateTime")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            if (!property.type.isString()) {
                addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
                beginControlFlow("if ($valueRef is String)")
            }
            
            // SECURITY: Length check to prevent ReDoS on very long inputs
            addStatement("// Security: Limit input length for regex matching (ReDoS protection)")
            beginControlFlow("if ($valueRef.length > %L)", RegexSafety.MAX_PATTERN_INPUT_LENGTH)
            addStatement(
                "errors.add(context.messageProvider.getMessage(%S, arrayOf<Any>(%L), context.locale))",
                "field.too_long",
                RegexSafety.MAX_PATTERN_INPUT_LENGTH
            )
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            // PERFORMANCE: Use cached regex from companion object
            beginControlFlow("if (!isoDateTimeRegex.matches($valueRef))")
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

    private fun generateFutureValidator(
        validator: ValidationValidatorInfo.FutureValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Future - uses injectable Clock from context")
            
            val valueRef = if (property.isNullable) {
                beginControlFlow("value?.let")
                "it"
            } else {
                "value"
            }
            
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
            beginControlFlow("when ($valueRef)")
            addStatement("is java.time.LocalDate -> {")
            indent()
            addStatement("val now = java.time.LocalDate.now(context.clock)")
            beginControlFlow("if (!$valueRef.isAfter(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.LocalDateTime -> {")
            indent()
            addStatement("val now = java.time.LocalDateTime.now(context.clock)")
            beginControlFlow("if (!$valueRef.isAfter(now))")
            add(addErrorMessage(validator))
            add(addFailFastIfNeeded(property, fieldPath))
            endControlFlow()
            unindent()
            addStatement("}")
            addStatement("is java.time.Instant -> {")
            indent()
            addStatement("val now = java.time.Instant.now(context.clock)")
            beginControlFlow("if (!$valueRef.isAfter(now))")
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

    private fun generatePastValidator(
        validator: ValidationValidatorInfo.PastValidator,
        property: PropertyInfo,
        fieldPath: String
    ): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("// @Past - uses injectable Clock from context")
            beginControlFlow("value?.let")
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S, %S)", "USELESS_IS_CHECK", "USELESS_CAST")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
            addStatement("@Suppress(%S)", "USELESS_IS_CHECK")
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
