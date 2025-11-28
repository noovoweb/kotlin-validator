package com.noovoweb.validator.unit.file

import com.noovoweb.validator.MaxFileSize
import com.noovoweb.validator.Validated
import java.io.File

@Validated
data class MaxFileSizeData(
    @MaxFileSize(1048576) // 1 MB
    val file: File?,
)
