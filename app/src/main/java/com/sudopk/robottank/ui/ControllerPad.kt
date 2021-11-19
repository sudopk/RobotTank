package com.sudopk.robottank.ui

import android.os.Handler
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sudopk.robottank.drive.ControlButton
import com.sudopk.robottank.drive.DriveSignals

private val BUTTON_SIZE = 130.dp

private val TAG = ControllerPad::class.simpleName

object ControllerPad {
  @Composable
  fun ControllerPad(driveSignals: DriveSignals, handler: Handler) {
    val uis = remember { MutableInteractionSource() }
    val dis = remember { MutableInteractionSource() }
    val lis = remember { MutableInteractionSource() }
    val ris = remember { MutableInteractionSource() }

    // It seems [collectIsPressedAsState] needs to be called each time, so don't call it inside
    // [any] below or it won't correctly.
    val buttonToSource = mapOf(ControlButton.UP to uis.collectIsPressedAsState(),
                               ControlButton.DOWN to dis.collectIsPressedAsState(),
                               ControlButton.LEFT to lis.collectIsPressedAsState(),
                               ControlButton.RIGHT to ris.collectIsPressedAsState())

    // Left/Right buttons override Up/down
    listOf(ControlButton.LEFT, ControlButton.RIGHT, ControlButton.UP, ControlButton.DOWN).any {
      val isPressed = buttonToSource.getValue(it).value
      Log.d(TAG, "$it: $isPressed")
      driveSignals.buttonHeld.set(if (isPressed) it else null)
      return@any isPressed
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Row {
        Spacer(Modifier.size(BUTTON_SIZE))
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.UP) },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = uis) {
          Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Up")
        }
        OutlinedButton(onClick = { driveSignals.scanBtDevices() },
                       modifier = Modifier.size(BUTTON_SIZE)) {
          Icon(Icons.Filled.Refresh, contentDescription = "Scan bluetooth devices")
        }
      }
      Row {
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.LEFT) },
                       modifier = Modifier.size(BUTTON_SIZE),
                       interactionSource = lis) {
          Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Left")
        }
        OutlinedButton(onClick = { driveSignals.blowHorn() },
                       modifier = Modifier.size(BUTTON_SIZE)) {
          Icon(Icons.Filled.Call, contentDescription = "Horn")
        }
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.RIGHT) },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = ris) {
          Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Right")
        }
      }
      Row {
        OutlinedButton(onClick = { driveSignals.switchOnLight() },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = dis) {
          Icon(Icons.Filled.Create, contentDescription = "Light on")
        }
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.DOWN) },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = dis) {
          Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Down")
        }
        OutlinedButton(onClick = { driveSignals.switchOffLight() },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = dis) {
          Icon(Icons.Filled.Delete, contentDescription = "Light off")
        }
      }
    }
  }
}