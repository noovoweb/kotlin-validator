package com.noovoweb.validator

import java.nio.file.Files
import java.nio.file.Path

/**
 * Content-based MIME type detection from a file's leading "magic bytes".
 *
 * Used by `@MimeType` so file-type checks reflect the actual bytes rather than the
 * attacker-chosen filename extension. A file named `avatar.png` whose contents are a
 * PDF (or any other recognized binary type) is detected as its true type and rejected.
 *
 * Detection is authoritative for the binary formats with known signatures below. Formats
 * with no reliable magic bytes — plain text, CSV, JSON, SVG — cannot be identified from
 * content, so for those [detectMimeType] falls back to [Files.probeContentType], which is
 * typically extension-based. Do not rely on `@MimeType` alone to keep text-based payloads
 * out of an image allowlist; pair it with server-side storage that never executes uploads.
 */
public object FileSignatures {
    private data class Signature(val mimeType: String, val offset: Int, val bytes: IntArray)

    // Ordered most-specific first. Bytes are unsigned (0x00..0xFF).
    private val SIGNATURES: List<Signature> = listOf(
        Signature("image/png", 0, intArrayOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
        Signature("image/jpeg", 0, intArrayOf(0xFF, 0xD8, 0xFF)),
        Signature("image/gif", 0, intArrayOf(0x47, 0x49, 0x46, 0x38)), // "GIF8" (87a and 89a)
        Signature("image/bmp", 0, intArrayOf(0x42, 0x4D)),
        Signature("image/webp", 8, intArrayOf(0x57, 0x45, 0x42, 0x50)), // "WEBP" after RIFF header
        Signature("image/tiff", 0, intArrayOf(0x49, 0x49, 0x2A, 0x00)), // little-endian
        Signature("image/tiff", 0, intArrayOf(0x4D, 0x4D, 0x00, 0x2A)), // big-endian
        Signature("application/pdf", 0, intArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)), // "%PDF-"
        Signature("application/zip", 0, intArrayOf(0x50, 0x4B, 0x03, 0x04)), // also docx/xlsx/jar
        Signature("application/gzip", 0, intArrayOf(0x1F, 0x8B)),
        Signature("application/ogg", 0, intArrayOf(0x4F, 0x67, 0x67, 0x53))
    )

    /** Number of leading bytes read for signature matching. */
    private const val HEADER_BYTES: Int = 16

    /**
     * Returns the MIME type detected from [path]'s content signature, or the result of
     * [Files.probeContentType] when no known signature matches. Null if the file cannot
     * be read or the type cannot be determined by either means.
     */
    public fun detectMimeType(path: Path): String? {
        val header = readHeader(path)
        if (header != null) {
            for (signature in SIGNATURES) {
                if (matches(header, signature)) return signature.mimeType
            }
        }
        // No recognized binary signature: best-effort fallback for text/unknown formats.
        return probe(path)
    }

    private fun readHeader(path: Path): ByteArray? = try {
        Files.newInputStream(path).use { stream ->
            val buffer = ByteArray(HEADER_BYTES)
            val read = stream.read(buffer)
            if (read <= 0) ByteArray(0) else buffer.copyOf(read)
        }
    } catch (e: Exception) {
        null
    }

    private fun matches(header: ByteArray, signature: Signature): Boolean {
        if (header.size < signature.offset + signature.bytes.size) return false
        for (i in signature.bytes.indices) {
            if ((header[signature.offset + i].toInt() and 0xFF) != signature.bytes[i]) return false
        }
        return true
    }

    private fun probe(path: Path): String? = try {
        Files.probeContentType(path)
    } catch (e: Exception) {
        null
    }
}
