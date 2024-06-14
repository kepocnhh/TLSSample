package test.cryptographic.tls.module.enter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import test.cryptographic.tls.App
import test.cryptographic.tls.util.showToast
import java.security.PrivateKey

@Composable
internal fun EnterScreen(
    onEnter: (PrivateKey) -> Unit,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val insets = WindowInsets.systemBars.asPaddingValues()
        val context = LocalContext.current
        val logics = App.logics<EnterLogics>()
        LaunchedEffect(Unit) {
            logics.events.collect { event ->
                when (event) {
                    is EnterLogics.Event.OnEnter -> {
                        event.result.fold(
                            onSuccess = { privateKey ->
                                onEnter(privateKey)
                            },
                            onFailure = { error ->
                                context.showToast("enter error: $error")
                            },
                        )
                    }
                }
            }
        }
        val state = logics.states.collectAsState().value
        val pinState = remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(Color.LightGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    modifier = Modifier,
                    text = "pin",
                )
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    singleLine = true,
                    value = pinState.value,
                    onValueChange = { text ->
                        val digits = "0123456789"
                        val contains = text.all { digits.contains(it) }
                        if (text.length < 5 && contains) {
                            pinState.value = text
                        }
                    },
                )
            }
            val filled = true
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !state.loading) {
                            onExit()
                        }
                        .wrapContentSize(),
                    text = "exit",
                )
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !state.loading && filled) {
                            val pin = pinState.value
                            pinState.value = ""
                            logics.enter(pin = pin)
                        }
                        .wrapContentSize(),
                    text = "enter",
                )
            }
        }
    }
}
