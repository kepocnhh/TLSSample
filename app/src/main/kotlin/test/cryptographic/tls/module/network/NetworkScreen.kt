package test.cryptographic.tls.module.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import test.cryptographic.tls.util.compose.BackHandler
import java.net.InetAddress
import java.net.NetworkInterface

@Composable
internal fun NetworkScreen(
    onBack: () -> Unit,
) {
    BackHandler(block = onBack)
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val itemsState = remember { mutableStateOf<Map<String, List<String>>?>(null) }
        LaunchedEffect(Unit) {
            if (itemsState.value == null) {
                itemsState.value = withContext(Dispatchers.IO) {
                    NetworkInterface.getNetworkInterfaces()
                        .asSequence()
                        .mapIndexed { nii, ni ->
                            val addresses = ni.inetAddresses.toList()
                            val key = """
                                network interface: $nii
                                 name: ${ni.name}(${ni.displayName})
                                 addresses: ${addresses.size}
                                 loopback: ${runCatching { ni.isLoopback.toString() }.getOrElse { it.toString() }}
                                 up: ${runCatching { ni.isUp.toString() }.getOrElse { it.toString() }}
                                 ptp: ${runCatching { ni.isPointToPoint.toString() }.getOrElse { it.toString() }}
                                 virtual: ${ni.isVirtual}
                            """.trimIndent()
                            key to addresses.mapIndexed { ai, address ->
                                """
                                    address: ${ai + 1}/${addresses.size}
                                     host: ${address.hostAddress}
                                     loopback: ${address.isLoopbackAddress}
                                     local: ${address.isSiteLocalAddress}
                                     hostName: ${address.hostName}
                                     canonicalHostName: ${address.canonicalHostName}
                                """.trimIndent()
                            }
                        }.toMap() + InetAddress.getLocalHost().let { address ->
                            mapOf(
                                "localhost" to listOf(
                                    """
                                         host: ${address.hostAddress}
                                         loopback: ${address.isLoopbackAddress}
                                         local: ${address.isSiteLocalAddress}
                                         hostName: ${address.hostName}
                                         canonicalHostName: ${address.canonicalHostName}
                                    """.trimIndent()
                                ),
                            )
                        }
                }
            }
        }
        val items = itemsState.value.orEmpty()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = insets,
        ) {
            items.entries.forEachIndexed { nii, (ni, addresses) ->
                item(key = "ni[$nii]") {
                    BasicText(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        text = ni,
                    )
                }
                addresses.forEachIndexed { ai, address ->
                    item(key = "address[$nii/$ai]") {
                        BasicText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            text = address,
                        )
                    }
                }
            }
        }
    }
}
