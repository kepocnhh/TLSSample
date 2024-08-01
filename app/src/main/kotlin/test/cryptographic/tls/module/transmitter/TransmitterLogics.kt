package test.cryptographic.tls.module.transmitter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cryptographic.tls.module.app.Injection
import java.net.URL

internal class TransmitterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Event {
        class OnSync(val result: Result<String>) : Event
        class OnAddressError(val error: Throwable) : Event
    }

    data class State(val loading: Boolean)

    data class AddressState(val url: URL)

    private val logger = injection.loggers.create("[Transmitter]")
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    private val _states = MutableStateFlow(State(loading = false))
    val states = _states.asStateFlow()
    private val _addressState = MutableStateFlow<AddressState?>(null)
    val addressState = _addressState.asStateFlow()

    private suspend fun sync(address: URL, message: String) {
        logger.debug("sync: $address")
        withContext(injection.contexts.default) {
            val keys = injection.locals.keys ?: TODO()
            val privateKey = injection.sessions.privateKey ?: TODO()
            runCatching {
                logger.debug("public:key: ${injection.secrets.hash(keys.publicKey.encoded)}")
                val response = injection.remotes(address).sessionStart(
                    publicKey = keys.publicKey,
                    privateKey = privateKey,
                )
                injection.locals.address = address
                logger.debug("public:key:receiver: ${injection.secrets.hash(response.publicKey.encoded)}")
                logger.debug("secret:key: ${injection.secrets.hash(response.secretKey.encoded)}")
                logger.debug("session:ID: ${response.sessionId}")
                injection.remotes(address).double(
                    secretKey = response.secretKey,
                    privateKey = privateKey,
                    publicKey = response.publicKey,
                    sessionId = response.sessionId,
                    number = message.toInt(),
                )
            }
        }.fold(
            onSuccess = { number ->
                logger.debug("$message * 2 = $number")
                _events.emit(Event.OnSync(Result.success("$message * 2 = $number")))
            },
            onFailure = { error ->
                logger.warning("sync error: $error")
                _events.emit(Event.OnSync(Result.failure(error)))
            },
        )
    }

    private suspend fun double(address: URL, message: String) {
        logger.debug("double: $address\nmessage: $message")
        withContext(injection.contexts.default) {
            val keys = injection.locals.keys ?: TODO()
            val privateKey = injection.sessions.privateKey ?: TODO()
            runCatching {
                // todo save address
                injection.remotes(address).double(
                    privateKey = privateKey,
                    publicKey = keys.publicKey,
                    number = message.toInt(),
                )
            }
        }.fold(
            onSuccess = { number ->
                logger.debug("$message * 2 = $number")
                _events.emit(Event.OnSync(Result.success("$message * 2 = $number")))
            },
            onFailure = { error ->
                logger.warning("sync error: $error")
                _events.emit(Event.OnSync(Result.failure(error)))
            },
        )
    }

    fun sync(
        spec: String,
        message: String,
    ) = launch {
        _states.value = State(loading = true)
        withContext(injection.contexts.default) {
            runCatching {
                val url = URL(spec)
                val protocols = setOf("http", "https")
                val protocol = url.protocol
                if (!protocols.contains(protocol)) error("Protocol \"$protocol\" is not supported!")
                url
            }.recoverCatching {
                if (spec.isEmpty()) error("Spec is empty!")
                if (spec.isBlank()) error("Spec is blank!")
                URL("http://$spec")
            }
        }.fold(
            onSuccess = { address ->
                double(address = address, message = message)
            },
            onFailure = { error ->
                logger.warning("url parse error: $error")
                _events.emit(Event.OnAddressError(error = error))
            },
        )
        _states.value = State(loading = false)
    }

    fun requestAddressState() = launch {
        val address = withContext(injection.contexts.default) {
            injection.locals.address
        }
        if (address != null) {
            _addressState.value = AddressState(url = address)
        }
    }
}
