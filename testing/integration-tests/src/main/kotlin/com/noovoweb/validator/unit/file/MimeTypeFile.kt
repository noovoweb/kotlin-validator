package com.noovoweb.validator.unit.file

import com.noovoweb.validator.MimeType
import com.noovoweb.validator.Validated
import java.io.File

@Validated
data class MimeTypeFile(
    @MimeType(["image/png", "image/jpeg", "image/gif"])
    val file: File?
)
