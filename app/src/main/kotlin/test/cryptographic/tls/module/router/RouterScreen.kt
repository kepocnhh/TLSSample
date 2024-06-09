package test.cryptographic.tls.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import test.cryptographic.tls.module.main.MainScreen
import test.cryptographic.tls.module.receiver.ReceiverScreen

@Composable
internal fun RouterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = remember { mutableStateOf<MainScreen.State?>(null) }
        when (state.value) {
            MainScreen.State.Receiver -> ReceiverScreen(onBack = {state.value = null})
            null -> MainScreen(onState = {state.value = it})
        }
    }
}
