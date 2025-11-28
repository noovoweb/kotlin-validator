package com.noovoweb.validator.unit.file

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxFileSizeDataValidatorTest {
    @Test
    fun `max file size validator accepts files within size limit`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = MaxFileSizeDataValidator()

        val smallFile =
            File(tempDir, "small.txt").apply {
                writeText("x".repeat(1000))
            }
        validator.validate(MaxFileSizeData(file = smallFile))

        val mediumFile =
            File(tempDir, "medium.txt").apply {
                writeText("x".repeat(500000))
            }
        validator.validate(MaxFileSizeData(file = mediumFile))
    }

    @Test
    fun `max file size validator rejects files exceeding size limit`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = MaxFileSizeDataValidator()

        val largeFile =
            File(tempDir, "large.txt").apply {
                writeText("x".repeat(2000000))
            }
        val exception =
            assertThrows<ValidationException> {
                validator.validate(MaxFileSizeData(file = largeFile))
            }
        assertTrue(exception.errors.containsKey("file"))
    }

    @Test
    fun `max file size validator allows null when not required`() =
        runTest {
            val validator = MaxFileSizeDataValidator()
            validator.validate(MaxFileSizeData(file = null))
        }

    @Test
    fun `max file size validator provides error message`(
        @TempDir tempDir: File,
    ) = runTest {
        val validator = MaxFileSizeDataValidator()

        val largeFile =
            File(tempDir, "toolarge.txt").apply {
                writeText("x".repeat(3000000))
            }
        val exception =
            assertThrows<ValidationException> {
                validator.validate(MaxFileSizeData(file = largeFile))
            }

        assertTrue(exception.errors.containsKey("file"))
        assertFalse(exception.errors["file"]!!.isEmpty())
    }
}
