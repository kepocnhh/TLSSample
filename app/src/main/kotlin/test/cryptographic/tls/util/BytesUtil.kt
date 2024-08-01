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

    fun readInt(bytes: ByteArray, index: Int): Int {
        return bytes[index].toInt().and(0xff).shl(8 * 3)
            .or(bytes[index + 1].toInt().and(0xff).shl(8 * 2))
            .or(bytes[index + 2].toInt().and(0xff).shl(8 * 1))
            .or(bytes[index + 3].toInt().and(0xff))
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

    fun readLong(bytes: ByteArray, index: Int): Long {
        return bytes[index].toLong().and(0xff).shl(8 * 7)
            .or(bytes[index + 1].toLong().and(0xff).shl(8 * 6))
            .or(bytes[index + 2].toLong().and(0xff).shl(8 * 5))
            .or(bytes[index + 3].toLong().and(0xff).shl(8 * 4))
            .or(bytes[index + 4].toLong().and(0xff).shl(8 * 3))
            .or(bytes[index + 5].toLong().and(0xff).shl(8 * 2))
            .or(bytes[index + 6].toLong().and(0xff).shl(8 * 1))
            .or(bytes[index + 7].toLong().and(0xff))
    }

    fun writeBytes(bytes: ByteArray, index: Int, value: UUID) {
        writeBytes(bytes, index = index, value.mostSignificantBits)
        writeBytes(bytes, index = index + 8, value.leastSignificantBits)
    }

    fun readUUID(bytes: ByteArray, index: Int): UUID {
        return UUID(readLong(bytes = bytes, index = index), readLong(bytes = bytes, index = index + 8))
    }
}

internal fun Byte.toHEX(locale: Locale = Locale.US): String {
    return String.format(locale, "%02x", toInt().and(0xff))
}

internal fun ByteArray.toHEX(locale: Locale = Locale.US): String {
    return joinToString(separator = "") { it.toHEX(locale) }
}
