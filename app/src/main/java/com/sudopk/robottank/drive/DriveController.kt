package com.sudopk.robottank.drive

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.sudopk.robottank.bt.BtHelper
import com.sudopk.robottank.bt.DeviceWithState
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val TAG = DriveController::class.simpleName

enum class ControlButton {
  UP, DOWN, LEFT, RIGHT
}

class DriveController(
  private val context: Context,
  private val adapter: BluetoothAdapter,
  private val scannedDevices: MutableMap<String, DeviceWithState>,
) : DriveSignals {
  private val mutex = Mutex()
  override val buttonHeld = AtomicReference<ControlButton>()
  private var eventJob: Job? = null
  private var btSocket: BluetoothSocket? = null

  fun startEventLoop() {
    stopEventLoop()
    Log.d(TAG, "Starting event loop")
    eventJob = CoroutineScope(Dispatchers.IO).launch {
      while (true) {
        buttonHeld.get()?.let { signal(it) }
        delay(50)
      }
    }
  }

  fun stopEventLoop() {
    Log.d(TAG, "Stopping any running event loop")
    eventJob?.cancel()
    btSocket?.close()
  }

  override fun signal(button: ControlButton) {
    Log.d(TAG, button.toString())
    sendCommand(button.toString().lowercase())
  }

  private fun sendCommand(command: String) {
    val connected = btSocket?.isConnected ?: false
    if (connected) {
      btSocket?.outputStream?.write("$command\n".encodeToByteArray())
    }
  }

  override fun blowHorn() {
    sendCommand("horn")
  }

  override fun switchOnLight() {
    sendCommand("lon")
  }

  override fun switchOffLight() {
    sendCommand("loff")
  }

  override fun scanBtDevices() {
    btSocket?.close()
    btSocket = null
    BtHelper.scanDevices(context, adapter, scannedDevices)
  }

  override fun connect(state: DeviceWithState) {
    if (state.socket != null) {
      scannedDevices[state.device.address] = state.copy(socket = null)
      btSocket?.close()
      btSocket = null
      return
    }
    CoroutineScope(Dispatchers.IO).launch {
      mutex.withLock {
        btSocket = BtHelper.connect(adapter, state)
        scannedDevices[state.device.address] = state.copy(socket = btSocket)
      }
    }
  }
}

interface DriveSignals {
  val buttonHeld: AtomicReference<ControlButton>

  fun signal(button: ControlButton)
  fun scanBtDevices()
  fun connect(state: DeviceWithState)
  fun blowHorn()
  fun switchOnLight()
  fun switchOffLight()
}