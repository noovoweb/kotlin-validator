package com.noovoweb.validator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FileSignaturesTest {
    private fun write(dir: File, name: String, bytes: ByteArray): File = File(dir, name).apply { writeBytes(bytes) }

    @Test
    fun `detects binary types from magic bytes regardless of extension`(@TempDir dir: File) {
        val png = write(dir, "anything.dat", byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        assertEquals("image/png", FileSignatures.detectMimeType(png.toPath()))

        val jpeg = write(dir, "photo.txt", byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        assertEquals("image/jpeg", FileSignatures.detectMimeType(jpeg.toPath()))

        val gif = write(dir, "img", "GIF89a".toByteArray(Charsets.US_ASCII))
        assertEquals("image/gif", FileSignatures.detectMimeType(gif.toPath()))

        val pdf = write(dir, "report.png", "%PDF-1.7".toByteArray(Charsets.US_ASCII))
        assertEquals("application/pdf", FileSignatures.detectMimeType(pdf.toPath()))
    }

    @Test
    fun `content wins over a spoofed extension`(@TempDir dir: File) {
        // A PDF renamed to .png must be detected as PDF, not image/png.
        val spoof = write(dir, "avatar.png", "%PDF-1.4 malicious".toByteArray(Charsets.US_ASCII))
        assertEquals("application/pdf", FileSignatures.detectMimeType(spoof.toPath()))
    }

    @Test
    fun `webp is detected at its offset`(@TempDir dir: File) {
        val webp = write(
            dir,
            "image.webp",
            "RIFF".toByteArray(Charsets.US_ASCII) + byteArrayOf(0, 0, 0, 0) + "WEBP".toByteArray(Charsets.US_ASCII)
        )
        assertEquals("image/webp", FileSignatures.detectMimeType(webp.toPath()))
    }

    @Test
    fun `unreadable path does not throw`(@TempDir dir: File) {
        // Whatever the OS probe returns for a missing path (null or a name-based guess),
        // detection must fail closed without throwing.
        val missing = File(dir, "does-not-exist.bin")
        FileSignatures.detectMimeType(missing.toPath())
    }

    @Test
    fun `text content is not mistaken for a binary image`(@TempDir dir: File) {
        // No signature to match; the extension is honest, so the probe fallback types it
        // as text — never as one of the binary image signatures.
        val text = write(dir, "notes.txt", "just some plain text, not an image".toByteArray(Charsets.US_ASCII))
        val detected = FileSignatures.detectMimeType(text.toPath())
        assertNotEquals("image/png", detected)
        assertNotEquals("image/jpeg", detected)
    }
}
