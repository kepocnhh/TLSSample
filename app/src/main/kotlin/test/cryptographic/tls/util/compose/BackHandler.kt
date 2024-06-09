package test.cryptographic.tls.util.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner

@Composable
internal fun BackHandler(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onBackPressedDispatcher: OnBackPressedDispatcher = LocalContext.current.let {
        check(it is AppCompatActivity)
        it.onBackPressedDispatcher
    },
    enabled: Boolean = true,
    block: () -> Unit,
) {
    val onBack = rememberUpdatedState(block).value
    val callback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBack()
            }
        }
    }
    SideEffect {
        callback.isEnabled = enabled
    }
    DisposableEffect(lifecycleOwner, onBackPressedDispatcher) {
        onBackPressedDispatcher.addCallback(lifecycleOwner, callback)
        onDispose {
            callback.remove()
        }
    }
}
