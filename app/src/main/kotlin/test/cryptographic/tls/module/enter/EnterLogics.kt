package test.cryptographic.tls.module.enter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cryptographic.tls.module.app.Injection
import java.security.PrivateKey

internal class EnterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Event {
        class OnEnter(val result: Result<PrivateKey>) : Event
    }

    data class State(val loading: Boolean)

    private val loggers = injection.loggers.create("[Enter]")

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _states = MutableStateFlow(State(loading = false))
    val states = _states.asStateFlow()

    fun enter(pin: String) = launch {
        _states.value = State(loading = true)
        val result = withContext(injection.contexts.default) {
            runCatching {
                if (pin.isBlank()) error("PIN is blank!")
                val secretKey = injection.secrets.getSecretKey(password = pin)
                loggers.debug("secret key: ${injection.secrets.hash(secretKey.encoded)}")
                val keys = injection.locals.keys ?: TODO()
                val encoded = injection.secrets.decrypt(secretKey, keys.encryptedPrivateKey)
                loggers.debug("private key: ${injection.secrets.hash(encoded)}")
                injection.secrets.toPrivateKey(encoded)
            }
        }
        _events.emit(Event.OnEnter(result))
        _states.value = State(loading = false)
    }
}
