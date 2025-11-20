package com.noovoweb.validator

/**
 * Validates that a file's MIME type is one of the allowed types.
 *
 * Uses Files.probeContentType() wrapped in IO dispatcher (non-blocking).
 *
 * Supports: File, Path, String (file path)
 *
 * @param values Array of allowed MIME types
 * @param message Custom error message (optional)
 *
 * Message key: `field.mimetype`
 *
 * Example:
 * ```kotlin
 * @MimeType(["image/png", "image/jpeg", "image/gif"])
 * val avatar: File?
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MimeType(
    val values: Array<String>,
    val message: String = ""
)

/**
 * Validates that a file's extension is one of the allowed extensions.
 *
 * Pure string checking, no I/O (non-blocking).
 *
 * Supports: File, Path, String (file path)
 *
 * @param values Array of allowed extensions (without dot)
 * @param message Custom error message (optional)
 *
 * Message key: `field.fileextension`
 *
 * Example:
 * ```kotlin
 * @FileExtension(["jpg", "png", "gif"])
 * val photo: File?
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FileExtension(
    val values: Array<String>,
    val message: String = ""
)

/**
 * Validates that a file size does not exceed the specified value in bytes.
 *
 * Uses file.length() wrapped in IO dispatcher (non-blocking).
 *
 * Supports: File, Path
 *
 * @param bytes Maximum file size in bytes
 * @param message Custom error message (optional)
 *
 * Message key: `field.maxfilesize`
 *
 * Example:
 * ```kotlin
 * @MaxFileSize(1048576)  // 1 MB
 * val document: File?
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MaxFileSize(
    val bytes: Long,
    val message: String = ""
)
