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
        class OnSync(val result: Result<Unit>) : Event
        class OnAddressError(val error: Throwable) : Event
    }

    data class AddressState(val url: URL)

    private val logger = injection.loggers.create("[Transmitter]")
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    private val _addressState = MutableStateFlow<AddressState?>(null)
    val addressState = _addressState.asStateFlow()

    private suspend fun sync(address: URL) {
        withContext(injection.contexts.default) {
            runCatching {
                injection.remotes(address).hello()
            }
        }.fold(
            onSuccess = {
                withContext(injection.contexts.default) {
                    injection.locals.address = address
                }
                _events.emit(Event.OnSync(Result.success(Unit)))
            },
            onFailure = { error ->
                logger.warning("sync error: $error")
                _events.emit(Event.OnSync(Result.failure(error)))
            },
        )
    }

    fun sync(spec: String) = launch {
        withContext(injection.contexts.default) {
            runCatching {
                val url = URL(spec)
                val protocols = setOf("http")
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
                sync(address = address)
            },
            onFailure = { error ->
                logger.warning("url parse error: $error")
                _events.emit(Event.OnAddressError(error = error))
            },
        )
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
