package test.cryptographic.tls.module.receiver

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import kotlin.coroutines.CoroutineContext

internal class Receiver {
    sealed interface State {
        data class Stopped(val starting: Boolean) : State
        data class Started(val stopping: Boolean, val portNumber: Int) : State
    }

    private val _state = MutableStateFlow<State>(State.Stopped(starting = false))
    val state = _state.asStateFlow()
    private var serverSocket: ServerSocket? = null

    private fun onStarting(serverSocket: ServerSocket) {
        if (this.serverSocket != null) TODO()
        this.serverSocket = serverSocket
        _state.value = State.Started(stopping = false, portNumber = serverSocket.localPort)
        println("[${this::class.java.simpleName}]: started: port: ${serverSocket.localPort}") // todo
        while (true) {
            val currentState = _state.value
            if (currentState !is State.Started) break
            if (currentState.stopping) break
            try {
                serverSocket.accept().use { socket ->
                    // todo
                }
            } catch (e: Throwable) {
                if (_state.value == currentState.copy(stopping = true)) break
                TODO("socket accept error: $e")
            }
        }
    }

    fun start(coroutineScope: CoroutineScope, coroutineContext: CoroutineContext) {
        val oldState = _state.value
        if (oldState !is State.Stopped) TODO()
        if (oldState.starting) TODO()
        _state.value = State.Stopped(starting = true)
        println("[${this::class.java.simpleName}]: starting...") // todo
        coroutineScope.launch {
            withContext(coroutineContext) {
                runCatching {
                    val portNumber = 40631
                    ServerSocket(portNumber)
                }.fold(
                    onSuccess = ::onStarting,
                    onFailure = { error ->
                        TODO("starting error: $error")
                    },
                )
            }
            _state.value = State.Stopped(starting = false)
            println("[${this::class.java.simpleName}]: stopped") // todo
        }
    }

    fun stop() {
        val oldState = _state.value
        if (oldState !is State.Started) TODO()
        if (oldState.stopping) TODO()
        val serverSocket = serverSocket ?: TODO()
        _state.value = oldState.copy(stopping = true)
        println("[${this::class.java.simpleName}]: stopping...") // todo
        this.serverSocket = null
        runCatching {
            serverSocket.close()
        }.fold(
            onSuccess = {
                println("[${this::class.java.simpleName}]: socket(${serverSocket.hashCode()}) is closed") // todo
            },
            onFailure = { error ->
                println("[${this::class.java.simpleName}]: close socket(${serverSocket.hashCode()}) error: $error") // todo
            },
        )
    }
}
