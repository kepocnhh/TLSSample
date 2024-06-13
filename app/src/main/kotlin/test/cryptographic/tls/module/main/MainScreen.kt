package test.cryptographic.tls.module.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            // todo public hash
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
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable(onClick = onLock)
                    .wrapContentSize(),
                text = "Lock",
            )
        }
    }
}
