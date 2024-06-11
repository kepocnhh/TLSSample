package test.cryptographic.tls.module.receiver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sp.kx.http.HttpReceiver
import sp.kx.logics.Logics
import test.cryptographic.tls.module.app.Injection
import java.io.Closeable

internal class ReceiverLogics private constructor(
    private val injection: Injection,
    private val receiver: HttpReceiver,
) : Logics(
    coroutineContext = injection.contexts.main,
    tags = mapOf(
        "receiver" to Closeable { receiver.stop() },
    ),
) {
    constructor(injection: Injection) : this(injection, HttpReceiver(ReceiverRouting(injection)))

    private val logger = injection.loggers.create("[Receiver]")

    val states = receiver.states

    fun start() = launch {
        logger.debug("starting...")
        withContext(Dispatchers.Default) {
            val port = 40631 // todo
            logger.debug("starting: $port")
            receiver.start(port = port)
        }
    }

    fun stop() = launch {
        logger.debug("stopping...")
        withContext(Dispatchers.Default) {
            receiver.stop()
        }
    }
}
