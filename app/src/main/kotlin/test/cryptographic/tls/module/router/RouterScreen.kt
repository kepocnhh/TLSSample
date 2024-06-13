package test.cryptographic.tls.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import test.cryptographic.tls.App
import test.cryptographic.tls.module.auth.AuthScreen
import test.cryptographic.tls.module.enter.EnterScreen
import test.cryptographic.tls.module.main.MainScreen
import test.cryptographic.tls.module.network.NetworkScreen
import test.cryptographic.tls.module.receiver.ReceiverScreen
import test.cryptographic.tls.module.transmitter.TransmitterScreen
import java.security.PublicKey

@Composable
internal fun RouterScreen(
    publicKey: PublicKey,
    onLock: () -> Unit,
) {
    val state = remember { mutableStateOf<MainScreen.State?>(null) }
    when (state.value) {
        MainScreen.State.Network -> NetworkScreen(onBack = {state.value = null})
        MainScreen.State.Receiver -> ReceiverScreen(onBack = {state.value = null})
        MainScreen.State.Transmitter -> TransmitterScreen(onBack = {state.value = null})
        null -> {
            MainScreen(
                publicKey = publicKey,
                onState = {
                    state.value = it
                },
                onLock = onLock,
            )
        }
    }
}

@Composable
internal fun RouterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val logics = App.logics<RouterLogics>()
        val state = logics.states.collectAsState().value
        LaunchedEffect(state) {
            if (state == null) logics.requestState()
        }
        when (state) {
            is RouterLogics.State.Authorized -> RouterScreen(
                publicKey = state.publicKey,
                onLock = {
                    logics.lock()
                },
            )
            RouterLogics.State.NoKeys -> AuthScreen(
                onAuth = { keys, privateKey ->
                    logics.auth(keys = keys, privateKey = privateKey)
                },
            )
            is RouterLogics.State.Unauthorized -> EnterScreen(
                onEnter = { privateKey ->
                    logics.enter(privateKey = privateKey)
                },
                onExit = {
                     logics.exit()
                },
            )
            null -> {
                // noop
            }
        }
    }
}
