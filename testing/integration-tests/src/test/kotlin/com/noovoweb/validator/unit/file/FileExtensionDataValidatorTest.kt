package com.noovoweb.validator.unit.file

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileExtensionDataValidatorTest {
    @Test
    fun `file extension validator accepts valid extensions`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = FileExtensionDataValidator()

        val jpgFile = File(tempDir, "image.jpg").apply { createNewFile() }
        validator.validate(FileExtensionData(file = jpgFile))

        val pngFile = File(tempDir, "image.png").apply { createNewFile() }
        validator.validate(FileExtensionData(file = pngFile))

        val gifFile = File(tempDir, "image.gif").apply { createNewFile() }
        validator.validate(FileExtensionData(file = gifFile))
    }

    @Test
    fun `file extension validator rejects invalid extensions`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = FileExtensionDataValidator()

        val pdfFile = File(tempDir, "document.pdf").apply { createNewFile() }
        val exception =
            assertThrows<ValidationException> {
                validator.validate(FileExtensionData(file = pdfFile))
            }
        assertTrue(exception.errors.containsKey("file"))
    }

    @Test
    fun `file extension validator allows null when not required`() =
        runTest {
            val validator = FileExtensionDataValidator()
            validator.validate(FileExtensionData(file = null))
        }

    @Test
    fun `file extension validator provides error message`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = FileExtensionDataValidator()

        val txtFile = File(tempDir, "file.txt").apply { createNewFile() }
        val exception =
            assertThrows<ValidationException> {
                validator.validate(FileExtensionData(file = txtFile))
            }

        assertTrue(exception.errors.containsKey("file"))
        assertFalse(exception.errors["file"]!!.isEmpty())
    }
}
