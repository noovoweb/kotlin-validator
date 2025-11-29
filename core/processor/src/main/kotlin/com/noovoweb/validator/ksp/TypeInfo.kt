package com.noovoweb.validator.ksp

/**
 * Represents type information for a property.
 *
 * @property qualifiedName Fully qualified type name (e.g., "kotlin.String", "com.example.User")
 * @property simpleName Simple type name (e.g., "String", "User")
 * @property isNullable Whether the type is nullable
 * @property typeArguments Generic type arguments (e.g., List<String> has ["String"])
 */
internal data class TypeInfo(
    val qualifiedName: String,
    val simpleName: String,
    val isNullable: Boolean,
    val typeArguments: List<TypeInfo> = emptyList(),
) {
    /**
     * Check if this type is a String.
     */
    internal fun isString(): Boolean = qualifiedName == "kotlin.String"

    /**
     * Check if this type is a numeric type.
     */
    internal fun isNumeric(): Boolean = qualifiedName in NUMERIC_TYPES

    /**
     * Check if this type is a Boolean.
     */
    internal fun isBoolean(): Boolean = qualifiedName == "kotlin.Boolean"

    /**
     * Check if this type is a collection.
     */
    internal fun isCollection(): Boolean = qualifiedName in COLLECTION_TYPES

    /**
     * Check if this type is a File type.
     */
    internal fun isFile(): Boolean = qualifiedName in FILE_TYPES

    /**
     * Check if this type is a date/time type.
     */
    internal fun isDateTime(): Boolean = qualifiedName in DATE_TIME_TYPES

    companion object {
        private val NUMERIC_TYPES =
            setOf(
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Short",
                "kotlin.Byte",
            )

        private val COLLECTION_TYPES =
            setOf(
                "kotlin.collections.List",
                "kotlin.collections.Set",
                "kotlin.collections.Collection",
                "kotlin.Array",
            )

        private val FILE_TYPES =
            setOf(
                "java.io.File",
                "java.nio.file.Path",
            )

        private val DATE_TIME_TYPES =
            setOf(
                "java.time.LocalDate",
                "java.time.LocalDateTime",
                "java.time.Instant",
                "java.time.ZonedDateTime",
                "java.time.OffsetDateTime",
            )
    }
}
