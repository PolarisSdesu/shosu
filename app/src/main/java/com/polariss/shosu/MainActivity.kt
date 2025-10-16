package com.polariss.shosu

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.polariss.shosu.app.ExecutionButton
import com.polariss.shosu.app.SoundUtils
import com.polariss.shosu.ui.theme.ShosuTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val soundUtils = remember { SoundUtils(context) }
    DisposableEffect(Unit) {
        onDispose { soundUtils.release() }
    }

    LaunchedEffect(Unit) {
        if (shouldShowTip(context)) {
            Toast.makeText(
                context,
                context.getText(R.string.tip_from_ema),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val aboutAlphaAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName

    ShosuTheme {
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = rememberModalBottomSheetState(),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                },
                containerColor = Color(0xFF4B0029),
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .height(100.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getText(R.string.about_title).toString(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        fontSize = 8.sp,
                        lineHeight = 12.sp,
                        text = buildAnnotatedString {
                            append(context.getText(R.string.about_description_zh))
                            append("\n\n")
                            append(context.getText(R.string.about_description_ja))
                            append("\n\n")
                            append(context.getText(R.string.app_name))
                            append("\n")
                            append("${context.getText(R.string.version_label)} $versionName\n")
                            append(context.getText(R.string.developer_info))
                            append("\n")

                            withLink(
                                LinkAnnotation.Url(
                                    url = "https://x.com/KooyuooK"
                                )
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFFFF45AB),
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(context.getText(R.string.x_label))
                                }
                            }
                            append("｜")
                            withLink(
                                LinkAnnotation.Url(
                                    url = "https://space.bilibili.com/294080110"
                                )
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFFFF45AB),
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(context.getText(R.string.bilibili_label))
                                }
                            }
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.FillHeight
            )

            Icon(
                imageVector = Icons.Default.Info,
                tint = Color(0xFF252525).copy(alpha = aboutAlphaAnim.value),
                contentDescription = context.getText(R.string.about_title).toString(),
                modifier = Modifier
                    .padding(20.dp, 48.dp)
                    .clip(CircleShape)
                    .clickable { showBottomSheet = true }
                    .align(Alignment.TopStart),
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ExecutionButton(
                    size = 240.dp,
                    soundUtils = soundUtils,
                    onPressStart = {
                        coroutineScope.launch {
                            aboutAlphaAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(100)
                            )
                        }
                    },
                    onPressEnd = {
                        coroutineScope.launch {
                            aboutAlphaAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(200)
                            )
                        }
                    }
                )
            }
        }
    }
}

fun shouldShowTip(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val shown = prefs.getBoolean("tip_shown", false)
    if (!shown) {
        prefs.edit { putBoolean("tip_shown", true) }
        return true
    }
    return false
}
