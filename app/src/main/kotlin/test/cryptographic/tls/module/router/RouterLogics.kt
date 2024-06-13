package test.cryptographic.tls.module.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.module.app.Injection
import test.cryptographic.tls.provider.Sessions
import java.security.PrivateKey
import java.security.PublicKey

internal class RouterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface State {
        data object NoKeys : State
        class Unauthorized(val publicKey: PublicKey) : State
        class Authorized(val publicKey: PublicKey) : State
    }

    private val _states = MutableStateFlow<State?>(null)
    val states = _states.asStateFlow()

    fun requestState() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) {
            _states.value = State.NoKeys
        } else {
            val authorized = withContext(injection.contexts.default) {
                injection.sessions.privateKey != null
            }
            if (authorized) {
                _states.value = State.Authorized(publicKey = keys.publicKey)
            } else {
                _states.value = State.Unauthorized(publicKey = keys.publicKey)
            }
        }
    }

    fun lock() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = null // todo secureConnection
        }
        _states.value = State.Unauthorized(publicKey = keys.publicKey)
    }

    fun exit() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.locals.keys = null
        }
        _states.value = State.NoKeys
    }

    fun enter(privateKey: PrivateKey) = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = privateKey
        }
        _states.value = State.Authorized(publicKey = keys.publicKey)
    }

    fun auth(keys: Keys, privateKey: PrivateKey) = launch {
        withContext(injection.contexts.default) {
            if (injection.locals.keys != null) TODO()
            injection.locals.keys = keys
        }
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = privateKey
        }
        _states.value = State.Authorized(publicKey = keys.publicKey)
    }
}
