package com.sudopk.robottank.ui

import android.os.Handler
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    val buttonToSource = mapOf(ControlButton.UP to uis,
           ControlButton.DOWN to dis,
          ControlButton.LEFT to lis,
          ControlButton.RIGHT to ris)

    // Left/Right buttons override Up/down
    listOf(ControlButton.LEFT, ControlButton.RIGHT, ControlButton.UP, ControlButton.DOWN).any {
      val isPressed by buttonToSource.getValue(it).collectIsPressedAsState()
      Log.d(TAG, "$it: $isPressed")
      driveSignals.buttonHeld.set(if (isPressed) it else null)
      return@any isPressed
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      OutlinedButton(onClick = { driveSignals.signal(ControlButton.UP) },
                     modifier = Modifier.size(BUTTON_SIZE), interactionSource = uis) {
        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Up")
      }
      Row {
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.LEFT) },
                       modifier = Modifier.size(BUTTON_SIZE),
                       interactionSource = lis) {
          Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Left")
        }
        OutlinedButton(onClick = { driveSignals.scanBtDevices() },
                       modifier = Modifier.size(BUTTON_SIZE)) {
          Icon(Icons.Filled.Refresh, contentDescription = "Scan bluetooth devices")
        }
        OutlinedButton(onClick = { driveSignals.signal(ControlButton.RIGHT) },
                       modifier = Modifier.size(BUTTON_SIZE), interactionSource = ris) {
          Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Right")
        }
      }
      OutlinedButton(onClick = { driveSignals.signal(ControlButton.DOWN) },
                     modifier = Modifier.size(BUTTON_SIZE), interactionSource = dis) {
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Down")
      }
    }
  }
}