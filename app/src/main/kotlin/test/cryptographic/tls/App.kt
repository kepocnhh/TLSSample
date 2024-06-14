package test.cryptographic.tls

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import sp.kx.logics.Logics
import sp.kx.logics.LogicsFactory
import sp.kx.logics.LogicsProvider
import sp.kx.logics.contains
import sp.kx.logics.get
import sp.kx.logics.remove
import test.cryptographic.tls.module.app.Injection
import test.cryptographic.tls.provider.Contexts
import test.cryptographic.tls.provider.FinalAssets
import test.cryptographic.tls.provider.FinalLocals
import test.cryptographic.tls.provider.FinalLoggers
import test.cryptographic.tls.provider.FinalRemotes
import test.cryptographic.tls.provider.FinalSecrets
import test.cryptographic.tls.provider.FinalSerializer
import test.cryptographic.tls.provider.Secrets
import test.cryptographic.tls.provider.Serializer
import test.cryptographic.tls.provider.Sessions

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val secrets: Secrets = FinalSecrets()
        val serializer: Serializer = FinalSerializer(secrets)
        _injection = Injection(
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
            loggers = FinalLoggers(),
            locals = FinalLocals(
                context = this,
                serializer = serializer,
            ),
            remotes = { address ->
                FinalRemotes(
                    address = address,
                    serializer = serializer,
                    secrets = secrets,
                )
            },
            serializer = serializer,
            sessions = Sessions(),
            assets = FinalAssets(this),
            secrets = secrets,
        )
    }

    companion object {
        private var _injection: Injection? = null
        val injection: Injection get() = checkNotNull(_injection)

        private val _logicsProvider = LogicsProvider(
            factory = object : LogicsFactory {
                override fun <T : Logics> create(type: Class<T>): T {
                    return type
                        .getConstructor(Injection::class.java)
                        .newInstance(injection)
                }
            },
        )

        @Composable
        inline fun <reified T : Logics> logics(label: String = T::class.java.name): T {
            val (contains, logic) = synchronized(App::class.java) {
                remember { _logicsProvider.contains<T>(label = label) } to _logicsProvider.get<T>(label = label)
            }
            DisposableEffect(Unit) {
                onDispose {
                    if (!contains) _logicsProvider.remove<T>(label = label)
                }
            }
            return logic
        }
    }
}
