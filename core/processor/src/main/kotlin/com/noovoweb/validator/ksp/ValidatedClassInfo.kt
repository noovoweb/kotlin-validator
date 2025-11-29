package com.noovoweb.validator.ksp

/**
 * Represents a class annotated with @Validated.
 *
 * @property packageName Package name of the class
 * @property className Simple class name
 * @property properties List of validated properties
 */
internal data class ValidatedClassInfo(
    val packageName: String,
    val className: String,
    val properties: List<PropertyInfo>,
) {
    /**
     * Get the fully qualified name of the class.
     */
    val qualifiedName: String
        get() = "$packageName.$className"

    /**
     * Get the generated validator class name.
     */
    val validatorClassName: String
        get() = "${className}Validator"
}

/**
 * Represents a property with validation validators.
 *
 * @property name Property name
 * @property type Type information
 * @property validators List of validation validators to apply
 * @property isNullable Whether the property type is nullable
 * @property failFastPositions Positions where @FailFast checkpoints are placed (indices in the annotation order)
 * @property nestedValidation Nested validation info if @Valid is present
 */
internal data class PropertyInfo(
    val name: String,
    val type: TypeInfo,
    val validators: List<ValidationValidatorInfo>,
    val isNullable: Boolean,
    val failFastPositions: List<Int>,
    val nestedValidation: NestedValidationInfo?,
) {
    /**
     * Check if this property has any validation validators.
     */
    internal fun hasValidators(): Boolean = validators.isNotEmpty()

    /**
     * Check if this property requires nested validation.
     */
    internal fun hasNestedValidation(): Boolean = nestedValidation != null

    /**
     * Get the validation method name for this property.
     */
    internal fun getValidationMethodName(): String = "validate${name.capitalize()}"

    /**
     * Check if there's a @FailFast checkpoint after the given validator index.
     * Returns true if we should stop validation after this validator failed.
     *
     * Example: @Required @Email @FailFast @MaxLength
     * - validatorIndex 0 (@Required): checkpoint at position 2, so shouldFailFastAfter(0) = false
     * - validatorIndex 1 (@Email): checkpoint at position 2, so shouldFailFastAfter(1) = true
     * - validatorIndex 2 (@MaxLength): no checkpoint after, so shouldFailFastAfter(2) = false
     */
    internal fun shouldFailFastAfter(validatorIndex: Int): Boolean {
        return failFastPositions.contains(validatorIndex + 1)
    }
}

/**
 * Represents nested validation configuration.
 *
 * @property validateEachElement Whether to validate each element in a collection (@Valid(each = true))
 */
internal data class NestedValidationInfo(
    val validateEachElement: Boolean,
)

/**
 * Extension function to capitalize first letter.
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
