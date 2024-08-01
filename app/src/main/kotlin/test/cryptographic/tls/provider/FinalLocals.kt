package test.cryptographic.tls.provider

import android.content.Context
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.Keys
import java.net.URL
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class FinalLocals(
    context: Context,
    private val serializer: Serializer,
) : Locals {
    private val prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    init {
        val version = prefs.getInt("version", -1)
        if (version < VERSION) {
            prefs.edit()
                .clear()
                .putInt("version", VERSION)
                .commit()
        }
    }

    override var address: URL?
        get() {
            val spec = prefs.getString("address", null) ?: return null
            return URL(spec)
        }
        set(value) {
            if (value?.toString() == null) {
                prefs.edit().remove("address").commit()
            } else {
                prefs.edit().putString("address", value.toString()).commit()
            }
        }

    override var keys: Keys?
        get() {
            val text = prefs.getString("keys", null) ?: return null
            return serializer.keys.decode(text.toByteArray())
        }
        set(value) {
            if (value?.toString() == null) {
                prefs.edit().remove("keys").commit()
            } else {
                prefs.edit().putString("keys", String(serializer.keys.encode(value))).commit()
            }
        }

    override var requested: Map<UUID, Duration>
        get() {
            return prefs.getStringSet("requested", null)?.associate {
                val split = it.split(",")
                check(split.size == 2)
                UUID.fromString(split[0]) to split[1].toLong().milliseconds
            }.orEmpty()
        }
        set(value) {
            if (value.isEmpty()) {
                prefs.edit().remove("requested").commit()
            } else {
                prefs.edit().putStringSet("requested", value.map { (id, time) -> "$id,${time.inWholeMilliseconds}" }.toSet()).commit()
            }
        }

    companion object {
        const val VERSION = 2
    }
}
