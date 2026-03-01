package com.mikeisesele.clearr

import androidx.compose.ui.window.ComposeUIViewController
import com.mikeisesele.clearr.runtime.IosClearrRuntime
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    ClearrApp(runtime = IosClearrRuntime())
}
