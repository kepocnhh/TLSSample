package test.cryptographic.tls.provider

import android.content.Context
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.Keys
import java.net.URL

internal class FinalLocals(
    context: Context,
    private val serializer: Serializer,
) : Locals {
    private val prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

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
}
