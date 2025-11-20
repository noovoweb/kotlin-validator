package com.noovoweb.validator.unit.file

import com.noovoweb.validator.FileExtension
import com.noovoweb.validator.Validated
import java.io.File

@Validated
data class FileExtensionData(
    @FileExtension(["jpg", "png", "gif"])
    val file: File?
)
