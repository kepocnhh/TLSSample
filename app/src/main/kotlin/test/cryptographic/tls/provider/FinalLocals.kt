package test.cryptographic.tls.provider

import android.content.Context
import test.cryptographic.tls.BuildConfig
import java.net.URL

internal class FinalLocals(context: Context) : Locals {
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
}
