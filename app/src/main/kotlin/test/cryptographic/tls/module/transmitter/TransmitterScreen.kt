package test.cryptographic.tls.module.transmitter

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
import test.cryptographic.tls.util.compose.BackHandler
import test.cryptographic.tls.util.showToast

@Composable
internal fun TransmitterScreen(
    onBack: () -> Unit,
) {
    BackHandler(block = onBack)
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            val logics = App.logics<TransmitterLogics>()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                logics.events.collect { event ->
                    when (event) {
                        is TransmitterLogics.Event.OnAddressError -> {
                            context.showToast("address error: ${event.error}") // todo
                        }
                        is TransmitterLogics.Event.OnSync -> {
                            event.result.fold(
                                onSuccess = {
                                    context.showToast("sync success")
                                },
                                onFailure = { error ->
                                    context.showToast("sync error: $error")
                                },
                            )
                        }
                    }
                }
            }
            val savedAddress = logics.addressState.collectAsState().value
            val addressState = remember { mutableStateOf("") }
            LaunchedEffect(savedAddress) {
                if (savedAddress == null) {
                    logics.requestAddressState()
                } else {
                    addressState.value = savedAddress.toString()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Center),
            ) {
                BasicText(
                    modifier = Modifier,
                    text = "address",
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    value = addressState.value,
                    onValueChange = { addressState.value = it },
                )
            }
            val enabled = addressState.value.isNotBlank()
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = enabled) {
                        logics.sync(spec = addressState.value)
                    }
                    .wrapContentSize(),
                text = "sync",
            )
        }
    }
}
