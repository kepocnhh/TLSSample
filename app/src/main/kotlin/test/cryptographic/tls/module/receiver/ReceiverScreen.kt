package test.cryptographic.tls.module.receiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket

internal object ReceiverScreen {
    sealed interface State {
        data object Stopped : State
        data object Starting : State
        class Started(
            val hostAddress: String,
            val serverSocket: ServerSocket,
        ) : State {
            override fun toString(): String {
                return "Started($hostAddress)"
            }
        }
        class Stopping(val serverSocket: ServerSocket) : State {
            override fun toString(): String {
                return "Stopping(${serverSocket.hashCode()})"
            }
        }
    }
}

private fun getInetAddress(): InetAddress {
    return NetworkInterface
        .getNetworkInterfaces()
        .asSequence()
        .flatMap { it.inetAddresses.asSequence() }
//        .flatMap { ni -> ni.interfaceAddresses.map { it.address } }
        .filterIsInstance<Inet4Address>()
        .firstOrNull { !it.isLoopbackAddress }
        ?: error("No addresses!")
}

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
) {
    // todo back
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = remember { mutableStateOf<ReceiverScreen.State>(ReceiverScreen.State.Stopped) }
        DisposableEffect(Unit) {
            onDispose {
                println("on dispose: ${state.value}") // todo
                when (val socketState = state.value) {
                    is ReceiverScreen.State.Started -> {
                        state.value = ReceiverScreen.State.Stopping(serverSocket = socketState.serverSocket)
                    }
                    else -> {
                        // noop
                    }
                }
            }
        }
        LaunchedEffect(state.value) {
            println("state: ${state.value}") // todo
            when (val socketState = state.value) {
                is ReceiverScreen.State.Started -> {
                    withContext(Dispatchers.IO) {
                        while (true) {
                            val newState = state.value
                            if (newState !is ReceiverScreen.State.Started) {
                                println("new state: $newState") // todo
                                break
                            }
                            try {
                                socketState.serverSocket.accept().use { socket ->
                                    // todo
                                }
                            } catch (e: Throwable) {
                                println("socket accept error: $e") // todo
                                if (state.value is ReceiverScreen.State.Stopping) break
                                TODO("socket accept error: $e")
                            }
                        }
                        state.value = ReceiverScreen.State.Stopped
                    }
                }
                is ReceiverScreen.State.Starting -> {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            getInetAddress().hostAddress ?: TODO()
                        }.mapCatching { hostAddress ->
                            val portNumber = 40631 // todo
                            ReceiverScreen.State.Started(
                                serverSocket = ServerSocket(portNumber),
                                hostAddress = hostAddress,
                            )
                        }
                    }.fold(
                        onSuccess = {
                            state.value = it
                        },
                        onFailure = { error ->
                            TODO("starting error: $error")
                        },
                    )
                }
                is ReceiverScreen.State.Stopping -> {
                    withContext(Dispatchers.Default) {
                        runCatching { socketState.serverSocket.close() }
                    }
                }
                else -> {
                    // noop
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            when (val socketState = state.value) {
                is ReceiverScreen.State.Started -> {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .wrapContentSize(),
                        text = socketState.hostAddress,
                    )
                }
                else -> {
                    // noop
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                val enabled = when (state.value) {
                    ReceiverScreen.State.Stopped -> true
                    is ReceiverScreen.State.Started -> true
                    else -> false
                }
                val text = when (state.value) {
                    ReceiverScreen.State.Stopped -> "start"
                    is ReceiverScreen.State.Started -> "stop"
                    else -> "..."
                }
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = enabled) {
                            when (val socketState = state.value) {
                                is ReceiverScreen.State.Started -> {
                                    state.value =
                                        ReceiverScreen.State.Stopping(serverSocket = socketState.serverSocket)
                                }

                                ReceiverScreen.State.Stopped -> {
                                    state.value = ReceiverScreen.State.Starting
                                }

                                else -> {
                                    // noop
                                }
                            }
                        }
                        .wrapContentSize(),
                    text = text,
                )
            }
        }
    }
}
