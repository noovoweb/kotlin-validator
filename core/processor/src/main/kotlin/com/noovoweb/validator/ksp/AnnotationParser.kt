package com.noovoweb.validator.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * Parses @Validated classes and extracts validators.
 *
 * Converts KSP declarations into our internal representation (ValidatedClassInfo).
 */
internal class AnnotationParser(private val logger: KSPLogger) {
    /**
     * Parse a class annotated with @Validated.
     *
     * @param classDeclaration The class declaration to parse
     * @return ValidatedClassInfo with all properties and validators, or null if invalid
     */
    internal fun parse(classDeclaration: KSClassDeclaration): ValidatedClassInfo? {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()

        logger.info("Parsing @Validated class: $packageName.$className")

        // Extract all properties with validation annotations
        val properties =
            classDeclaration.getAllProperties()
                .mapNotNull { property -> parseProperty(property) }
                .toList()

        if (properties.isEmpty()) {
            logger.warn("No validated properties found in $className")
            return null
        }

        return ValidatedClassInfo(
            packageName = packageName,
            className = className,
            properties = properties,
        )
    }

    /**
     * Parse a single property and extract validation validators.
     */
    private fun parseProperty(property: KSPropertyDeclaration): PropertyInfo? {
        val propertyName = property.simpleName.asString()
        val type = parseType(property.type)

        // Extract all validation validators from annotations
        val validators = mutableListOf<ValidationValidatorInfo>()
        val failFastPositions = mutableListOf<Int>()
        var nestedValidation: NestedValidationInfo? = null
        var annotationIndex = 0
        var validatorCount = 0

        property.annotations.forEach { annotation ->
            val annotationName = annotation.shortName.asString()

            when (annotationName) {
                // Structural annotations
                "FailFast" -> {
                    // Record the validator index where this checkpoint should trigger
                    // (after the validator that precedes this annotation)
                    failFastPositions.add(validatorCount)
                }
                "Valid" -> {
                    val each = annotation.arguments.find { it.name?.asString() == "each" }?.value as? Boolean ?: false
                    nestedValidation = NestedValidationInfo(validateEachElement = each)
                }

                // Parse validation validators
                else -> {
                    // First, try to parse as a built-in validator
                    parseValidationValidator(annotation)?.let { validator ->
                        validators.add(validator)
                        validatorCount++
                    }
                        // If not a built-in validator, check if it's a meta-annotation
                        ?: run {
                            if (isMetaAnnotation(annotation)) {
                                parseCustomValidatorFromMeta(annotation)?.let { validator ->
                                    validators.add(validator)
                                    validatorCount++
                                }
                            } else {
                                // Only warn if it's neither a built-in validator nor a meta-annotation
                                logger.warn("Unknown validation annotation: $annotationName")
                            }
                        }
                }
            }

            annotationIndex++
        }

        // Skip properties with no validators and no nested validation
        if (validators.isEmpty() && nestedValidation == null) {
            return null
        }

        return PropertyInfo(
            name = propertyName,
            type = type,
            validators = validators,
            isNullable = type.isNullable,
            failFastPositions = failFastPositions,
            nestedValidation = nestedValidation,
        )
    }

    /**
     * Parse type information from a type reference.
     */
    private fun parseType(typeRef: KSTypeReference): TypeInfo {
        val type = typeRef.resolve()
        val qualifiedName = type.declaration.qualifiedName?.asString() ?: "Unknown"
        val simpleName = type.declaration.simpleName.asString()
        val isNullable = type.isMarkedNullable

        val typeArguments =
            type.arguments.mapNotNull { typeArg ->
                typeArg.type?.let { parseType(it) }
            }

        return TypeInfo(
            qualifiedName = qualifiedName,
            simpleName = simpleName,
            isNullable = isNullable,
            typeArguments = typeArguments,
        )
    }

    /**
     * Check if an annotation is itself annotated with @CustomValidator (meta-annotation).
     *
     * This enables users to create reusable custom validator annotations like @StrongPassword
     * instead of repeating @CustomValidator(...) everywhere.
     */
    private fun isMetaAnnotation(annotation: KSAnnotation): Boolean {
        val annotationDeclaration = annotation.annotationType.resolve().declaration
        return annotationDeclaration.annotations.any {
            it.shortName.asString() == "CustomValidator"
        }
    }

    /**
     * Extract CustomValidator information from a meta-annotation.
     *
     * When a user creates a custom annotation annotated with @CustomValidator,
     * this function extracts the validator function reference and message.
     *
     * @return CustomValidatorInfo with validator function and message, or null if not found
     */
    private fun parseCustomValidatorFromMeta(annotation: KSAnnotation): ValidationValidatorInfo.CustomValidatorInfo? {
        val annotationDeclaration = annotation.annotationType.resolve().declaration
        val customValidatorAnnotation =
            annotationDeclaration.annotations
                .firstOrNull { it.shortName.asString() == "CustomValidator" }
                ?: return null

        // Extract validator function reference from the meta-annotation
        val validator =
            customValidatorAnnotation.arguments
                .firstOrNull { it.name?.asString() == "validator" }
                ?.value as? String
                ?: return null

        // Extract optional message from the meta-annotation
        val message =
            customValidatorAnnotation.arguments
                .firstOrNull { it.name?.asString() == "message" }
                ?.value as? String

        logger.info("Found meta-annotation with validator: $validator")

        return ValidationValidatorInfo.CustomValidatorInfo(validator, message)
    }

    /**
     * Parse a validation validator from an annotation.
     *
     * @return ValidationValidatorInfo or null if not a validation annotation
     */
    private fun parseValidationValidator(annotation: KSAnnotation): ValidationValidatorInfo? {
        val annotationName = annotation.shortName.asString()
        val message = getAnnotationArgument<String>(annotation, "message")?.takeIf { it.isNotEmpty() }

        return when (annotationName) {
            // String validators
            "Required" -> ValidationValidatorInfo.RequiredValidator(message)
            "Email" -> ValidationValidatorInfo.EmailValidator(message)
            "Url" -> ValidationValidatorInfo.UrlValidator(message)
            "Uuid" -> ValidationValidatorInfo.UuidValidator(message)
            "Length" ->
                ValidationValidatorInfo.LengthValidator(
                    min = getAnnotationArgument<Int>(annotation, "min") ?: 0,
                    max = getAnnotationArgument<Int>(annotation, "max") ?: Int.MAX_VALUE,
                    customMessage = message,
                )
            "MinLength" ->
                ValidationValidatorInfo.MinLengthValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: 0,
                    customMessage = message,
                )
            "MaxLength" ->
                ValidationValidatorInfo.MaxLengthValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: Int.MAX_VALUE,
                    customMessage = message,
                )
            "Pattern" ->
                ValidationValidatorInfo.PatternValidator(
                    pattern = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "Alpha" -> ValidationValidatorInfo.AlphaValidator(message)
            "Alphanumeric" -> ValidationValidatorInfo.AlphanumericValidator(message)
            "Ascii" -> ValidationValidatorInfo.AsciiValidator(message)
            "Lowercase" -> ValidationValidatorInfo.LowercaseValidator(message)
            "Uppercase" -> ValidationValidatorInfo.UppercaseValidator(message)
            "StartsWith" ->
                ValidationValidatorInfo.StartsWithValidator(
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "EndsWith" ->
                ValidationValidatorInfo.EndsWithValidator(
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "Contains" ->
                ValidationValidatorInfo.ContainsValidator(
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "OneOf" ->
                ValidationValidatorInfo.OneOfValidator(
                    values = getAnnotationArrayArgument<String>(annotation, "values"),
                    customMessage = message,
                )
            "NotOneOf" ->
                ValidationValidatorInfo.NotOneOfValidator(
                    values = getAnnotationArrayArgument<String>(annotation, "values"),
                    customMessage = message,
                )
            "Enum" -> {
                val enumClassType = annotation.arguments
                    .firstOrNull { it.name?.asString() == "value" }
                    ?.value as? KSType
                val enumClassName = enumClassType?.declaration?.qualifiedName?.asString() ?: ""
                ValidationValidatorInfo.EnumValidator(enumClassName, message)
            }
            "Json" -> ValidationValidatorInfo.JsonValidator(message)
            "Luhn" -> ValidationValidatorInfo.LuhnValidator(message)
            "CreditCard" -> ValidationValidatorInfo.CreditCardValidator(message)

            // Numeric validators
            "Min" ->
                ValidationValidatorInfo.MinValidator(
                    value = getAnnotationArgument<Double>(annotation, "value") ?: 0.0,
                    customMessage = message,
                )
            "Max" ->
                ValidationValidatorInfo.MaxValidator(
                    value = getAnnotationArgument<Double>(annotation, "value") ?: Double.MAX_VALUE,
                    customMessage = message,
                )
            "Between" ->
                ValidationValidatorInfo.BetweenValidator(
                    min = getAnnotationArgument<Double>(annotation, "min") ?: 0.0,
                    max = getAnnotationArgument<Double>(annotation, "max") ?: Double.MAX_VALUE,
                    customMessage = message,
                )
            "Positive" -> ValidationValidatorInfo.PositiveValidator(message)
            "Negative" -> ValidationValidatorInfo.NegativeValidator(message)
            "Zero" -> ValidationValidatorInfo.ZeroValidator(message)
            "Integer" -> ValidationValidatorInfo.IntegerValidator(message)
            "Decimal" -> ValidationValidatorInfo.DecimalValidator(message)
            "DivisibleBy" ->
                ValidationValidatorInfo.DivisibleByValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: 1,
                    customMessage = message,
                )
            "Even" -> ValidationValidatorInfo.EvenValidator(message)
            "Odd" -> ValidationValidatorInfo.OddValidator(message)
            "DecimalPlaces" ->
                ValidationValidatorInfo.DecimalPlacesValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: 0,
                    customMessage = message,
                )

            // Boolean validators
            "Accepted" -> ValidationValidatorInfo.AcceptedValidator(message)

            // Collection validators
            "Size" ->
                ValidationValidatorInfo.SizeValidator(
                    min = getAnnotationArgument<Int>(annotation, "min") ?: 0,
                    max = getAnnotationArgument<Int>(annotation, "max") ?: Int.MAX_VALUE,
                    customMessage = message,
                )
            "MinSize" ->
                ValidationValidatorInfo.MinSizeValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: 0,
                    customMessage = message,
                )
            "MaxSize" ->
                ValidationValidatorInfo.MaxSizeValidator(
                    value = getAnnotationArgument<Int>(annotation, "value") ?: Int.MAX_VALUE,
                    customMessage = message,
                )
            "NotEmpty" -> ValidationValidatorInfo.NotEmptyValidator(message)
            "Distinct" -> ValidationValidatorInfo.DistinctValidator(message)
            "ContainsValue" ->
                ValidationValidatorInfo.ContainsValueValidator(
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "NotContains" ->
                ValidationValidatorInfo.NotContainsValidator(
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )

            // Date/Time validators
            "DateFormat" ->
                ValidationValidatorInfo.DateFormatValidator(
                    format = getAnnotationArgument<String>(annotation, "format") ?: "",
                    customMessage = message,
                )
            "IsoDate" -> ValidationValidatorInfo.IsoDateValidator(message)
            "IsoDateTime" -> ValidationValidatorInfo.IsoDateTimeValidator(message)
            "Future" -> ValidationValidatorInfo.FutureValidator(message)
            "Past" -> ValidationValidatorInfo.PastValidator(message)
            "Today" -> ValidationValidatorInfo.TodayValidator(message)

            // Network validators
            "IPv4" -> ValidationValidatorInfo.IPv4Validator(message)
            "IPv6" -> ValidationValidatorInfo.IPv6Validator(message)
            "IP" -> ValidationValidatorInfo.IPValidator(message)
            "MacAddress" -> ValidationValidatorInfo.MacAddressValidator(message)
            "Port" -> ValidationValidatorInfo.PortValidator(message)

            // File validators
            "MimeType" ->
                ValidationValidatorInfo.MimeTypeValidator(
                    values = getAnnotationArrayArgument<String>(annotation, "values"),
                    customMessage = message,
                )
            "FileExtension" ->
                ValidationValidatorInfo.FileExtensionValidator(
                    values = getAnnotationArrayArgument<String>(annotation, "values"),
                    customMessage = message,
                )
            "MaxFileSize" ->
                ValidationValidatorInfo.MaxFileSizeValidator(
                    bytes = getAnnotationArgument<Long>(annotation, "bytes") ?: Long.MAX_VALUE,
                    customMessage = message,
                )

            // Conditional validators
            "Same" ->
                ValidationValidatorInfo.SameValidator(
                    field = getAnnotationArgument<String>(annotation, "field") ?: "",
                    customMessage = message,
                )
            "Different" ->
                ValidationValidatorInfo.DifferentValidator(
                    field = getAnnotationArgument<String>(annotation, "field") ?: "",
                    customMessage = message,
                )
            "RequiredIf" ->
                ValidationValidatorInfo.RequiredIfValidator(
                    field = getAnnotationArgument<String>(annotation, "field") ?: "",
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "RequiredUnless" ->
                ValidationValidatorInfo.RequiredUnlessValidator(
                    field = getAnnotationArgument<String>(annotation, "field") ?: "",
                    value = getAnnotationArgument<String>(annotation, "value") ?: "",
                    customMessage = message,
                )
            "RequiredWith" ->
                ValidationValidatorInfo.RequiredWithValidator(
                    fields = getAnnotationArrayArgument<String>(annotation, "fields"),
                    customMessage = message,
                )
            "RequiredWithout" ->
                ValidationValidatorInfo.RequiredWithoutValidator(
                    fields = getAnnotationArrayArgument<String>(annotation, "fields"),
                    customMessage = message,
                )

            // Custom validators
            "CustomValidator" ->
                ValidationValidatorInfo.CustomValidatorInfo(
                    validatorFunctionFqn = getAnnotationArgument<String>(annotation, "validator") ?: "",
                    customMessage = message,
                )

            else -> null
        }
    }

    /**
     * Get a single annotation argument value.
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> getAnnotationArgument(
        annotation: KSAnnotation,
        argName: String,
    ): T? {
        return annotation.arguments
            .find { it.name?.asString() == argName }
            ?.value as? T
    }

    /**
     * Get an array annotation argument value.
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> getAnnotationArrayArgument(
        annotation: KSAnnotation,
        argName: String,
    ): List<T> {
        val value =
            annotation.arguments
                .find { it.name?.asString() == argName }
                ?.value

        return when (value) {
            is ArrayList<*> -> value.filterIsInstance<T>()
            is Array<*> -> value.filterIsInstance<T>()
            else -> emptyList()
        }
    }
}
