package com.polariss.shosu.app

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.polariss.shosu.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@Composable
fun ExecutionButton(
    size: Dp = 240.dp,
    onFinished: () -> Unit = {},
    soundUtils: SoundUtils,
    onPressStart: () -> Unit = {},
    onPressEnd: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var filled by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    var fillJob: Job? by remember { mutableStateOf(null) }
    val vibrator = context.getSystemService(Vibrator::class.java)

    Box(
        modifier = Modifier
            .size(size)
            .pointerInput(filled) {
                detectTapGestures(
                    onPress = {
                        if (filled) return@detectTapGestures

                        onPressStart() // 开始淡出图标

                        fillJob?.cancel()
                        fillJob = coroutineScope.launch {
                            try {
                                scale.animateTo(0.94f, tween(150))
                                vibrator?.vibrate(
                                    VibrationEffect.createWaveform(
                                        longArrayOf(
                                            0,
                                            100,
                                            0
                                        ), 0
                                    )
                                )
                                soundUtils.playHoldSound()
                                animProgress.animateTo(1f, tween(5500, easing = LinearEasing))
                                filled = true
                                vibrator?.cancel()
                                soundUtils.stopHoldSound()
                                soundUtils.playFinishSound()
                                onFinished()
                            } finally {
                                vibrator?.cancel()
                                soundUtils.stopHoldSound()
                            }
                        }

                        try {
                            awaitRelease()
                        } finally {
                            coroutineScope.launch { scale.animateTo(1f, tween(200)) }
                            onPressEnd() // 松手恢复图标

                            if (!filled) {
                                fillJob?.cancelAndJoin()
                                coroutineScope.launch {
                                    animProgress.animateTo(
                                        0f,
                                        tween(
                                            durationMillis = (animProgress.value * 1000).toInt(),
                                            easing = LinearEasing
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDoubleTap = {
                        fillJob?.cancel()
                        coroutineScope.launch {
                            animProgress.animateTo(0f, tween(300, easing = LinearEasing))
                            filled = false
                            vibrator?.cancel()
                            soundUtils.stopHoldSound()
                            onPressEnd()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.BLACK
                        alpha = 200
                        maskFilter = android.graphics.BlurMaskFilter(
                            60f,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }

                    drawContext.canvas.nativeCanvas.drawCircle(
                        center.x,
                        center.y,
                        (this.size.minDimension / 2f) * scale.value, // 半径跟随缩放
                        paint
                    )
                }


                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            contentAlignment = Alignment.Center
        ) {
            // 背景图
            Image(
                painter = painterResource(id = R.drawable.button_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // 填充动画
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animProgress.value)
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF953949))
                )
            }

            // 前景按钮
            Image(
                painter = painterResource(id = R.drawable.button),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // 完成状态
            if (filled) {
                Image(
                    painter = painterResource(id = R.drawable.finish),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}
