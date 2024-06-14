package test.cryptographic.tls.module.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import test.cryptographic.tls.App
import java.security.PublicKey

internal object MainScreen {
    enum class State {
        Network,
        Receiver,
        Transmitter,
    }
}

@Composable
internal fun MainScreen(
    publicKey: PublicKey,
    onState: (MainScreen.State) -> Unit,
    onLock: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val insets = WindowInsets.systemBars.asPaddingValues()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            val text = App.injection.secrets.hash(publicKey.encoded)
            check(text.length == 64)
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .wrapContentSize(),
                text = "${text.substring(0, 32)}\n${text.substring(32, 64)}",
                style = TextStyle(fontFamily = FontFamily.Monospace),
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .clickable(onClick = onLock)
                    .wrapContentSize(),
                text = "lock",
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            MainScreen.State.entries.forEach { state ->
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable {
                            onState(state)
                        }
                        .wrapContentSize(),
                    text = state.name,
                )
            }
        }
    }
}
