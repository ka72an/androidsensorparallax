package com.example.tilt

import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.graphics.RuntimeShader
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.hypot

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<TiltViewModel>()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startListening(this)

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

                var isMenuExpanded by remember { mutableStateOf(false) }
                var isGlossEnabled by remember { mutableStateOf(false) }
                var isInternalParallaxEnabled by remember { mutableStateOf(true) }

                var shadowBlur by remember { mutableFloatStateOf(52.87f) }
                var shadowAlpha by remember { mutableFloatStateOf(0.95f) }
                var shadowOffsetY by remember { mutableFloatStateOf(40.00f) }
                var shadowPadding by remember { mutableFloatStateOf(33.59f) }
                var shadowSpread by remember { mutableFloatStateOf(127.33f) }

                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri -> selectedImageUri = uri }
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    if (selectedImageUri == null) {
                        Button(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) { Text("Pick a Photo") }
                    } else {
                        SolidFloatingCard(
                            uri = selectedImageUri!!,
                            pitch = viewModel.pitch,
                            roll = viewModel.roll,
                            shadowBlur = shadowBlur,
                            shadowAlpha = shadowAlpha,
                            shadowOffsetY = shadowOffsetY,
                            shadowPadding = shadowPadding,
                            shadowSpread = shadowSpread,
                            isGlossEnabled = isGlossEnabled,
                            isInternalParallaxEnabled = isInternalParallaxEnabled,
                            onDoubleTap = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111111))
                            .padding(16.dp)
                    ) {
                        TextButton(
                            onClick = { isMenuExpanded = !isMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isMenuExpanded) "▼ Hide Controls" else "▲ Tune Card Physics",
                                color = Color.White
                            )
                        }

                        if (isMenuExpanded) {
                            ControlSwitch("Glass Reflection", isGlossEnabled) { isGlossEnabled = it }
                            ControlSwitch("Internal Window Depth", isInternalParallaxEnabled) { isInternalParallaxEnabled = it }
                            Spacer(modifier = Modifier.height(8.dp))

                            ControlSlider("Blur Radius", shadowBlur, 10f..120f) { shadowBlur = it }
                            ControlSlider("Opacity (Alpha)", shadowAlpha, 0f..1f) { shadowAlpha = it }
                            ControlSlider("Y-Offset (Drop)", shadowOffsetY, -50f..150f) { shadowOffsetY = it }
                            ControlSlider("Padding (Core Size)", shadowPadding, 0f..60f) { shadowPadding = it }
                            ControlSlider("Spread (Parallax)", shadowSpread, 0f..150f) { shadowSpread = it }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ControlSlider(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            color = Color.Gray,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.labelSmall
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(0.5f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.DarkGray
            )
        )
        Text(
            text = String.format("%.2f", value),
            color = Color.White,
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ControlSwitch(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = name,
            color = Color.Gray,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.DarkGray,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Black
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val METALLIC_GLOSS_SHADER = """
    uniform shader content;
    uniform float2 resolution;    
    uniform float pitch;
    uniform float roll;           

    half4 main(float2 fragCoord) {
        half4 color = content.eval(fragCoord);
        float2 uv = fragCoord / resolution;
        float lightX = 0.5 - (roll * 1.5);
        float lightY = 0.5 - (pitch * 1.5);
        
        float dist = distance(uv, float2(lightX, lightY));
        float gloss = max(0.0, 1.0 - (dist * 2.0));
        color.rgb += gloss * 0.4;
        return color;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SolidFloatingCard(
    uri: Uri,
    pitch: Float,
    roll: Float,
    shadowBlur: Float,
    shadowAlpha: Float,
    shadowOffsetY: Float,
    shadowPadding: Float,
    shadowSpread: Float,
    isGlossEnabled: Boolean,
    isInternalParallaxEnabled: Boolean,
    onDoubleTap: () -> Unit
) {
    val runtimeShader = remember { RuntimeShader(METALLIC_GLOSS_SHADER) }

    Box(contentAlignment = Alignment.Center) {

        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(360.dp, 460.dp)
                .graphicsLayer {
                    val totalLift = hypot(pitch, roll).coerceIn(0f, 1f)

                    val dynamicBlur = shadowBlur + (totalLift * 30f)
                    renderEffect = RenderEffect
                        .createBlurEffect(dynamicBlur, dynamicBlur, Shader.TileMode.DECAL)
                        .asComposeRenderEffect()

                    translationX = roll * shadowSpread
                    translationY = (-pitch * shadowSpread) + shadowOffsetY

                    alpha = shadowAlpha * (totalLift * 1.5f).coerceIn(0f, 1f)
                }
                .padding(shadowPadding.dp)
                .clip(RoundedCornerShape(32.dp))
        )

        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { onDoubleTap() })
                }
                .graphicsLayer {
                    val totalLift = hypot(pitch, roll).coerceIn(0f, 1f)

                    val dynamicScale = 1.0f + (totalLift * 0.15f)
                    scaleX = dynamicScale
                    scaleY = dynamicScale

                    translationX = -roll * 80f
                    translationY = pitch * 80f
                }
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp, 400.dp)
                    .graphicsLayer {
                        rotationX = -pitch * 25f
                        rotationY = -roll * 25f
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(32.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f

                            translationX = roll * 80f
                            translationY = -pitch * 80f

                            if (isInternalParallaxEnabled) {
                                scaleX = 1.2f
                                scaleY = 1.2f
                                translationX = roll * 80f
                                translationY = -pitch * 80f
                            } else {
                                scaleX = 1.0f
                                scaleY = 1.0f
                                translationX = 0f
                                translationY = 0f
                            }

                            if (isGlossEnabled) {
                                runtimeShader.setFloatUniform("resolution", size.width, size.height)
                                runtimeShader.setFloatUniform("pitch", pitch)
                                runtimeShader.setFloatUniform("roll", roll)
                                renderEffect = RenderEffect.createRuntimeShaderEffect(
                                    runtimeShader, "content"
                                ).asComposeRenderEffect()
                            } else {
                                renderEffect = null
                            }
                        }
                )
            }
        }
    }
}
