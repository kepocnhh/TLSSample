package test.cryptographic.tls.provider

import android.content.Context
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.SecureConnection
import java.net.URL
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

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

    override var publicKey: PublicKey?
        get() {
            val encoded = prefs.getString("publicKey", null) ?: return null
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(encoded.toByteArray())
            return keyFactory.generatePublic(keySpec)
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove("publicKey").commit()
            } else {
                prefs.edit().putString("publicKey", String(value.encoded)).commit()
            }
        }

    override var secureConnection: SecureConnection?
        get() {
            val value = prefs.getString("secureConnection", null) ?: return null
            return serializer.secureConnection.decode(value.toByteArray())
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove("secureConnection").commit()
            } else {
                prefs.edit().putString("secureConnection", String(serializer.secureConnection.encode(value))).commit()
            }
        }
}
