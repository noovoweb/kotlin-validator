package com.noovoweb.validator.ksp

/**
 * Sealed class hierarchy representing all validation validators.
 *
 * Each validation annotation is converted to a ValidationValidatorInfo instance
 * during KSP processing.
 */
sealed class ValidationValidatorInfo {
    abstract val customMessage: String?
    abstract val messageKey: String

    // === String Validators ===

    data class RequiredValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.required"
    }

    data class EmailValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.email"
    }

    data class UrlValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.url"
    }

    data class UuidValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.uuid"
    }

    data class LengthValidator(
        val min: Int,
        val max: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.length"
    }

    data class MinLengthValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.minlength"
    }

    data class MaxLengthValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.maxlength"
    }

    data class PatternValidator(
        val pattern: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.pattern"
    }

    data class AlphaValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.alpha"
    }

    data class AlphanumericValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.alphanumeric"
    }

    data class AsciiValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.ascii"
    }

    data class LowercaseValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.lowercase"
    }

    data class UppercaseValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.uppercase"
    }

    data class StartsWithValidator(
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.startswith"
    }

    data class EndsWithValidator(
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.endswith"
    }

    data class ContainsValidator(
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.contains"
    }

    data class OneOfValidator(
        val values: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.oneof"
    }

    data class NotOneOfValidator(
        val values: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.notoneof"
    }

    data class JsonValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.json"
    }

    data class LuhnValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.luhn"
    }

    // === Numeric Validators ===

    data class MinValidator(
        val value: Double,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.min"
    }

    data class MaxValidator(
        val value: Double,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.max"
    }

    data class BetweenValidator(
        val min: Double,
        val max: Double,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.between"
    }

    data class PositiveValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.positive"
    }

    data class NegativeValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.negative"
    }

    data class ZeroValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.zero"
    }

    data class IntegerValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.integer"
    }

    data class DecimalValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.decimal"
    }

    data class DivisibleByValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.divisibleby"
    }

    data class EvenValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.even"
    }

    data class OddValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.odd"
    }

    data class DecimalPlacesValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.decimalplaces"
    }

    // === Boolean Validators ===

    data class AcceptedValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.accepted"
    }

    // === Collection Validators ===

    data class SizeValidator(
        val min: Int,
        val max: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.size"
    }

    data class MinSizeValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.minsize"
    }

    data class MaxSizeValidator(
        val value: Int,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.maxsize"
    }

    data class NotEmptyValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.notempty"
    }

    data class DistinctValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.distinct"
    }

    data class ContainsValueValidator(
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.containsvalue"
    }

    data class NotContainsValidator(
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.notcontains"
    }

    // === Date/Time Validators ===

    data class DateFormatValidator(
        val format: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.dateformat"
    }

    data class IsoDateValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.isodate"
    }

    data class IsoDateTimeValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.isodatetime"
    }

    data class FutureValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.future"
    }

    data class PastValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.past"
    }

    data class TodayValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.today"
    }

    // === Network Validators ===

    data class IPv4Validator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.ipv4"
    }

    data class IPv6Validator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.ipv6"
    }

    data class IPValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.ip"
    }

    data class MacAddressValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.macaddress"
    }

    data class PortValidator(
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.port"
    }

    // === File Validators ===

    data class MimeTypeValidator(
        val values: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.mimetype"
    }

    data class FileExtensionValidator(
        val values: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.fileextension"
    }

    data class MaxFileSizeValidator(
        val bytes: Long,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.maxfilesize"
    }

    // === Conditional Validators ===

    data class SameValidator(
        val field: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.same"
    }

    data class DifferentValidator(
        val field: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.different"
    }

    data class RequiredIfValidator(
        val field: String,
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.requiredif"
    }

    data class RequiredUnlessValidator(
        val field: String,
        val value: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.requiredunless"
    }

    data class RequiredWithValidator(
        val fields: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.requiredwith"
    }

    data class RequiredWithoutValidator(
        val fields: List<String>,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.requiredwithout"
    }

    // === Custom Validators ===

    data class CustomValidatorInfo(
        val validatorFunctionFqn: String,
        override val customMessage: String?,
    ) : ValidationValidatorInfo() {
        override val messageKey = "field.custom"
    }
}
