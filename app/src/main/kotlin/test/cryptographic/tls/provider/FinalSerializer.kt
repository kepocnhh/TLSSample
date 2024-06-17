package test.cryptographic.tls.provider

import org.json.JSONObject
import test.cryptographic.tls.entity.Keys

internal class FinalSerializer(
    private val secrets: Secrets,
) : Serializer {
    override val keys = object : Transformer<Keys, ByteArray> {
        override fun encode(decoded: Keys): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): Keys {
            return JSONObject(String(encoded)).toKeys()
        }
    }

    override val ints = object : Transformer<Int, ByteArray> {
        override fun encode(decoded: Int): ByteArray {
            return "$decoded".toByteArray()
        }

        override fun decode(encoded: ByteArray): Int {
            return String(encoded).toInt()
        }
    }

    private fun Keys.toJSONObject(): JSONObject {
        return JSONObject()
            .put("publicKey", secrets.base64(publicKey.encoded))
            .put("encryptedPrivateKey", secrets.base64(encryptedPrivateKey))
    }

    private fun JSONObject.toKeys(): Keys {
        return Keys(
            publicKey = secrets.toPublicKey(secrets.base64(getString("publicKey"))),
            encryptedPrivateKey = secrets.base64(getString("encryptedPrivateKey")),
        )
    }
}
