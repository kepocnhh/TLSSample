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
import sp.kx.http.HttpReceiver
import test.cryptographic.tls.App
import test.cryptographic.tls.util.NetworkUtil
import test.cryptographic.tls.util.compose.BackHandler

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
) {
    BackHandler(block = onBack)
    val insets = WindowInsets.systemBars.asPaddingValues()
//    val logger = remember { App.injection.loggers.create("[Receiver|Screen]") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val logics = App.logics<ReceiverLogics>()
        val state = logics.states.collectAsState().value
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            when (state) {
                is HttpReceiver.State.Started -> {
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
                    is HttpReceiver.State.Stopped -> !state.starting
                    is HttpReceiver.State.Started -> !state.stopping
                }
                val text = when (state) {
                    is HttpReceiver.State.Stopped -> "start"
                    is HttpReceiver.State.Started -> "stop"
                }
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = enabled) {
                            when (state) {
                                is HttpReceiver.State.Stopped -> {
                                    if (!state.starting) {
                                        logics.start()
                                    }
                                }
                                is HttpReceiver.State.Started -> {
                                    if (!state.stopping) {
                                        logics.stop()
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
