package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun ConfettiOverlay(show: Boolean) {
    if (!show) return
    KonfettiView(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x4ADE80, 0x6366F1),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(200),
                position = Position.Relative(0.5, 0.0)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfettiOverlayPreview() {
    ClearrTheme {
        // Preview shows nothing because show=false; set to true to render confetti
        ConfettiOverlay(show = false)
    }
}
