package test.cryptographic.tls.module.receiver

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket

internal class Receiver {
    sealed interface State {
        data class Stopped(val starting: Boolean) : State
        data class Started(
            val stopping: Boolean,
            val host: String,
            val port: Int,
        ) : State
    }

    private val _state = MutableStateFlow<State>(State.Stopped(starting = false))
    val state = _state.asStateFlow()
    private var serverSocket: ServerSocket? = null

    private fun onStarting(serverSocket: ServerSocket) {
        if (this.serverSocket != null) TODO()
        this.serverSocket = serverSocket
        _state.value = State.Started(
            stopping = false,
            host = serverSocket.inetAddress.hostAddress!!,
            port = serverSocket.localPort,
        )
        println("[${this::class.java.simpleName}]: started: port: ${serverSocket.localPort}") // todo
        println("[${this::class.java.simpleName}]: inetAddress: ${serverSocket.inetAddress}") // todo
        val localSocketAddress = serverSocket.localSocketAddress
        check(localSocketAddress is InetSocketAddress)
        val message = """
            bound: ${serverSocket.isBound}
            closed: ${serverSocket.isClosed}
            address: ${localSocketAddress.address}
            port: ${localSocketAddress.port}
            hostName: ${localSocketAddress.hostName}
            hostString: ${localSocketAddress.hostString}
            unresolved: ${localSocketAddress.isUnresolved}
        """.trimIndent()
        println("[${this::class.java.simpleName}]: $message") // todo
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

    fun start() {
        val oldState = _state.value
        if (oldState !is State.Stopped) TODO()
        if (oldState.starting) TODO()
        _state.value = State.Stopped(starting = true)
        println("[${this::class.java.simpleName}]: starting...") // todo
        runCatching {
            val port = 40631
            val address = NetworkInterface
                .getNetworkInterfaces()
                .asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .firstOrNull { it.isSiteLocalAddress && !it.isLoopbackAddress }
                ?: TODO("no address!")
            ServerSocket(port, 1, address)
        }.fold(
            onSuccess = ::onStarting,
            onFailure = { error ->
                println("[${this::class.java.simpleName}]: starting error: $error") // todo
            },
        )
        _state.value = State.Stopped(starting = false)
        println("[${this::class.java.simpleName}]: stopped") // todo
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
