package com.sudopk.robottank

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.sudopk.robottank.bt.BtHelper
import com.sudopk.robottank.bt.DeviceWithState
import com.sudopk.robottank.drive.DriveController
import com.sudopk.robottank.ui.ControllerPad
import java.util.Arrays

private const val PERMISSION_CODE = 1
private val TAG = RobotTankActivity::class.simpleName

class RobotTankActivity : AppCompatActivity() {
  private val scannedDevices = mutableStateMapOf<String, DeviceWithState>()
  private lateinit var driveController: DriveController
  private lateinit var handler: Handler
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handler = Handler(Looper.getMainLooper())
    val manager = getSystemService<BluetoothManager>()
    val adapter = manager?.adapter!!
    driveController = DriveController(this, adapter, scannedDevices)
    driveController.startEventLoop()

    setContent {
      Column(Modifier.fillMaxSize()) {
        ControllerPad.ControllerPad(driveController, handler)
        Spacer(modifier = Modifier.height(32.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
          scannedDevices.forEach { (_, state) ->
            OutlinedButton({ driveController.connect(state) }, Modifier.fillMaxWidth()) {
              val connected = if (state.socket != null) "C" else ""
              Text("${state.device.name} (${state.device.address}) $connected")
            }
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()

    if (ActivityCompat.checkSelfPermission(this,
                                           Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(this,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        PERMISSION_CODE)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    driveController.stopEventLoop()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == PERMISSION_CODE) {
      if (!(grantResults contentEquals intArrayOf(PackageManager.PERMISSION_GRANTED))) {
        Log.d(TAG, "${Arrays.toString(permissions)} denied")
      }
    }
  }
}