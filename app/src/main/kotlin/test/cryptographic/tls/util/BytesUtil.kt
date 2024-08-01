package test.cryptographic.tls.util

import java.util.Locale
import java.util.UUID

internal object BytesUtil {
    fun writeBytes(bytes: ByteArray, index: Int, value: Int) {
        bytes[index] = value.shr(8 * 3).toByte()
        bytes[index + 1] = value.shr(8 * 2).toByte()
        bytes[index + 2] = value.shr(8).toByte()
        bytes[index + 3] = value.toByte()
    }

    fun writeBytes(bytes: ByteArray, index: Int, value: Long) {
        bytes[index] = value.shr(8 * 7).toByte()
        bytes[index + 1] = value.shr(8 * 6).toByte()
        bytes[index + 2] = value.shr(8 * 5).toByte()
        bytes[index + 3] = value.shr(8 * 4).toByte()
        bytes[index + 4] = value.shr(8 * 3).toByte()
        bytes[index + 5] = value.shr(8 * 2).toByte()
        bytes[index + 6] = value.shr(8).toByte()
        bytes[index + 7] = value.toByte()
    }

    fun writeBytes(bytes: ByteArray, index: Int, value: UUID) {
        writeBytes(bytes, index = index, value.mostSignificantBits)
        writeBytes(bytes, index = index + 8, value.leastSignificantBits)
    }
}

internal fun Byte.toHEX(locale: Locale = Locale.US): String {
    return String.format(locale, "%02x", toInt().and(0xff))
}

internal fun ByteArray.toHEX(locale: Locale = Locale.US): String {
    return joinToString(separator = "") { it.toHEX(locale) }
}
