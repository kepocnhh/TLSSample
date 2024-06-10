package test.cryptographic.tls.module.receiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import test.cryptographic.tls.util.NetworkUtil
import test.cryptographic.tls.util.compose.BackHandler

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
) {
    BackHandler(block = onBack)
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val receiver = remember { Receiver() }
        DisposableEffect(Unit) {
            onDispose {
                when (val state = receiver.state.value) {
                    is Receiver.State.Started -> {
                        if (!state.stopping) {
                            receiver.stop()
                        }
                    }
                    else -> {
                        // noop
                    }
                }
            }
        }
        val state = receiver.state.collectAsState().value
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
//            val hostState = remember { mutableStateOf<String?>(null) }
//            LaunchedEffect(state) {
//                when (state) {
//                    is Receiver.State.Started -> {
//                        // todo address online
//                        withContext(Dispatchers.IO) {
//                            runCatching {
//                                NetworkUtil.getLocalAddress().hostAddress ?: TODO()
//                            }
//                        }.fold(
//                            onSuccess = { host ->
//                                hostState.value = host
//                            },
//                            onFailure = {
//                                TODO()
//                            },
//                        )
//                    }
//                    is Receiver.State.Stopped -> {
//                        hostState.value = null
//                    }
//                }
//            }
            when (state) {
                is Receiver.State.Started -> {
//                    val host = hostState.value
//                    if (host != null) {
//                        BasicText(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(64.dp)
//                                .wrapContentSize(),
//                            text = "$host:${state.portNumber}",
//                        )
//                    }
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .wrapContentSize(),
                        text = "${state.host}:${state.port}",
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
                val enabled = when (state) {
                    is Receiver.State.Stopped -> !state.starting
                    is Receiver.State.Started -> !state.stopping
                }
                val text = when (state) {
                    is Receiver.State.Stopped -> "start"
                    is Receiver.State.Started -> "stop"
                }
                val scope = rememberCoroutineScope()
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = enabled) {
                            when (state) {
                                is Receiver.State.Stopped -> {
                                    if (!state.starting) {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                receiver.start()
                                            }
                                        }
                                    }
                                }
                                is Receiver.State.Started -> {
                                    if (!state.stopping) {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                receiver.stop()
                                            }
                                        }
                                    }
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
