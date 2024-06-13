package test.cryptographic.tls.module.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.module.app.Injection
import java.security.KeyStore
import java.security.PrivateKey

internal class AuthLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Event {
        class OnAuth(val result: Result<Pair<Keys, PrivateKey>>) : Event
    }

    data class State(val loading: Boolean)

    private val loggers = injection.loggers.create("[Auth]")

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _states = MutableStateFlow(State(loading = false))
    val states = _states.asStateFlow()

    fun auth(
        file: String,
        password: String,
        alias: String,
        pin: String,
    ) = launch {
        _states.value = State(loading = true)
        val result = withContext(injection.contexts.default) {
            runCatching {
                if (password.isBlank()) error("Password is blank!")
                if (pin.isBlank()) error("PIN is blank!")
                val keyStore = KeyStore.getInstance("PKCS12")
                loggers.debug("read \"$file\"...")
                injection.assets.getAsset(name = file).use {
                    loggers.debug("load key store...")
                    keyStore.load(it, password.toCharArray())
                }
                val asymmetricKey = keyStore.getKey(alias, password.toCharArray()) ?: error("No \"$alias\"!")
                loggers.debug("private key: ${injection.secrets.hash(asymmetricKey.encoded)}")
                check(asymmetricKey is PrivateKey)
                val certificate = keyStore.getCertificate(alias)
                loggers.debug("certificate: ${injection.secrets.hash(certificate.encoded)}")
                val publicKey = certificate.publicKey
                loggers.debug("public key: ${injection.secrets.hash(publicKey.encoded)}")
                val secretKey = injection.secrets.getSecretKey(password = pin)
                loggers.debug("secret key: ${injection.secrets.hash(secretKey.encoded)}")
                val keys = Keys(
                    publicKey = publicKey,
                    encryptedPrivateKey = injection.secrets.encrypt(secretKey, asymmetricKey.encoded),
                )
                keys to asymmetricKey
            }
        }
        _events.emit(Event.OnAuth(result))
        _states.value = State(loading = false)
    }
}
