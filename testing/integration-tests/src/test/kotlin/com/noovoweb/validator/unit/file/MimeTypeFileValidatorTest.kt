package com.noovoweb.validator.unit.file

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MimeTypeFileValidatorTest {

    @Test
    fun `mime type validator accepts valid mime types`(@TempDir tempDir: File) = runTest {
        val validator = MimeTypeFileValidator()

        val pngFile = File(tempDir, "image.png").apply {
            createNewFile()
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        }
        validator.validate(MimeTypeFile(file = pngFile))

        val jpegFile = File(tempDir, "image.jpg").apply {
            createNewFile()
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
        }
        validator.validate(MimeTypeFile(file = jpegFile))

        val gifFile = File(tempDir, "image.gif").apply {
            createNewFile()
            writeBytes(byteArrayOf(0x47, 0x49, 0x46, 0x38, 0x39, 0x61))
        }
        validator.validate(MimeTypeFile(file = gifFile))
    }

    @Test
    fun `mime type validator rejects invalid mime types`(@TempDir tempDir: File) = runTest {
        val validator = MimeTypeFileValidator()

        val pdfFile = File(tempDir, "document.pdf").apply {
            createNewFile()
            writeBytes(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D))
        }
        val exception = assertThrows<ValidationException> {
            validator.validate(MimeTypeFile(file = pdfFile))
        }
        assertTrue(exception.errors.containsKey("file"))
    }

    @Test
    fun `mime type validator allows null when not required`() = runTest {
        val validator = MimeTypeFileValidator()
        validator.validate(MimeTypeFile(file = null))
    }

    @Test
    fun `mime type validator provides error message`(@TempDir tempDir: File) = runTest {
        val validator = MimeTypeFileValidator()

        val txtFile = File(tempDir, "file.txt").apply {
            createNewFile()
            writeText("plain text content")
        }
        val exception = assertThrows<ValidationException> {
            validator.validate(MimeTypeFile(file = txtFile))
        }

        assertTrue(exception.errors.containsKey("file"))
        assertFalse(exception.errors["file"]!!.isEmpty())
    }
}
