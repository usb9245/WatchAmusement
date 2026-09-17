package box.mon.amusement.watch

import android.app.Service
import android.util.Log
import android.content.Intent
import android.content.ComponentName;
import android.content.pm.ComponentInfo
import android.nfc.NfcAdapter
import android.nfc.cardemulation.HostNfcFService
import android.nfc.cardemulation.NfcFCardEmulation
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.core.view.KeyEventDispatcher
import kotlin.system.exitProcess

class FeliCaService : HostNfcFService() {
    override fun processNfcFPacket(
        commandPacket: ByteArray?,
        extras: Bundle?
    ): ByteArray? = null

    override fun onDeactivated(reason: Int) {
    }
}