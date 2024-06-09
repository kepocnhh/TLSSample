package test.cryptographic.tls.module.receiver

import android.app.Service
import android.content.Intent
import android.os.IBinder

internal class ReceiverService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
