package com.sudopk.robottank.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.Arrays
import java.util.UUID

private val TAG = BtHelper::class.simpleName

// https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)
// https://developer.android.com/guide/topics/connectivity/bluetooth/connect-bluetooth-devices#connect-client
// The UUID for serial port profile (SPP).
private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
private val SPP_PARCEL_UUID = ParcelUuid(SPP_UUID)

object BtHelper {
  fun scanDevices(
    context: Context,
    adapter: BluetoothAdapter,
    scannedDevices: MutableMap<String, DeviceWithState>,
  ) {
    val bondedDevices = adapter.bondedDevices
    Log.d(TAG, "Bonded devices: ${bondedDevices.size}")
//    bondedDevices.forEach {
//      if (it.supportsSpp()) {
//        scannedDevices[it.address] = DeviceWithState(it, connected = false)
//      }
//    }

    val discoveryReceiver = BtDevicesDiscovery(adapter, scannedDevices)
    listOf(BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
           BluetoothAdapter.ACTION_DISCOVERY_STARTED,
           BluetoothDevice.ACTION_FOUND).forEach {
      context.registerReceiver(discoveryReceiver, IntentFilter(it))
    }
    if (!adapter.isDiscovering) {
      if (!adapter.startDiscovery()) {
        Log.e(TAG, "Failed to start bluetooth discovery")
      }
    }
  }

  fun connect(
    adapter: BluetoothAdapter,
    state: DeviceWithState,
  ): BluetoothSocket {
    adapter.cancelDiscovery()

    val socket = state.device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
    socket.connect()
    return socket
  }
}

class BtDevicesDiscovery(
  private val adapter: BluetoothAdapter,
  private val scannedDevices: MutableMap<String, DeviceWithState>,
) : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
        scannedDevices.forEach { it.value.socket?.close() }
        scannedDevices.clear()
      }
      // When discovery finds a device
      BluetoothDevice.ACTION_FOUND -> {
        // Get the BluetoothDevice object from the Intent
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
        if (device.supportsSpp()) {
          scannedDevices[device.address] = DeviceWithState(device)
        }
      }
      BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
        adapter.cancelDiscovery()
        context.unregisterReceiver(this)
      }
    }
  }
}

fun <T> MutableLiveData<T>.notifyObservers() {
  this.value = this.value
}

//JDY-30 B2:2B:04:05:86:ED 1f00 12 1
//00001101-0000-1000-8000-00805f9b34fb, 00000000-0000-1000-8000-00805f9b34fb, 00000000-0000-1000-8000-00805f9b34fb

data class DeviceWithState(val device: BluetoothDevice, val socket: BluetoothSocket? = null)

fun BluetoothDevice.supportsSpp(): Boolean {
  if (bondState == BluetoothDevice.BOND_BONDED && type == BluetoothDevice.DEVICE_TYPE_CLASSIC && uuids != null && SPP_PARCEL_UUID in uuids) {
    Log.d(TAG, "$name $address $bluetoothClass $bondState $type ${Arrays.toString(uuids)}")
    return true
  }
  return false
}