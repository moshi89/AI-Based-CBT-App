package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.termproject.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// ══════════════════════════════════════════════════════════════
// STEP 8 — [Path B] 실시간 타이머 및 보행 현수 상태 (구글 맵 트래킹 포함)
// ══════════════════════════════════════════════════════════════

@Composable
fun TimerActiveScreen(
    activity: BehavioralActivity,
    durationSecs: Int,
    walkDistanceMeters: Int,
    isPaused: Boolean,
    trackedPath: List<LatLng>,
    navigationPath: List<LatLng>,
    currentLocation: LatLng?,
    destinationLocation: LatLng?,
    currentLanguage: String,
    onPauseToggle: () -> Unit,
    onCompleteClick: () -> Unit
) {
    val context = LocalContext.current
    val displayMin = durationSecs / 60
    val displaySec = durationSecs % 60
    val formattedTime = String.format("%02d:%02d", displayMin, displaySec)

    val resNameId = context.resources.getIdentifier("act_${activity.id}_name", "string", context.packageName)
    val displayName = if (resNameId != 0) context.getString(resNameId) else activity.name


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SanctuaryStepIndicator(currentStep = 4)
            Spacer(modifier = Modifier.height(10.dp))
            ThemeSectionHeader(
                stepLabel = stringResource(id = R.string.ta_step_label),
                title = displayName,
                subtitle = stringResource(id = R.string.ta_subtitle)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 🗺️ Real-time Google Map Tracking Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentLocation == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(currentLocation, 16f)
                        }

                        // 사용자 위치 변화에 맞춰 카메라 중심 부드럽게 팔로우
                        LaunchedEffect(currentLocation) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentLocation, 16f),
                                1000
                            )
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
                        ) {
                            if (destinationLocation != null) {
                                Marker(
                                    state = MarkerState(position = destinationLocation),
                                    title = stringResource(id = R.string.map_destination_prefix) + displayName,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                )
                            }

                            if (navigationPath.isNotEmpty()) {
                                Polyline(
                                    points = navigationPath,
                                    color = Color(0xFF004D40).copy(alpha = 0.35f),
                                    width = 8f
                                )
                            }

                            if (trackedPath.isNotEmpty()) {
                                Polyline(
                                    points = trackedPath,
                                    color = Color(0xFF004D40),
                                    width = 10f
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compact Circular Timer display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White, CircleShape)
                        .border(3.dp, if (isPaused) Color.LightGray else Color(0xFF004D40), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkCharcoal,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.ta_walk_time),
                            fontSize = 10.sp,
                            color = MediumGray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White, CircleShape)
                        .border(3.dp, SagePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${walkDistanceMeters}m",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF004D40),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.ta_walk_distance),
                            fontSize = 10.sp,
                            color = MediumGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPauseToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) Color(0xFF004D40) else Color.LightGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh,
                        contentDescription = if (isPaused) stringResource(id = R.string.ta_resume_btn) else stringResource(id = R.string.ta_pause_btn),
                        tint = if (isPaused) Color.White else DarkCharcoal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPaused) stringResource(id = R.string.ta_resume_btn)
                               else stringResource(id = R.string.ta_pause_btn),
                        color = if (isPaused) Color.White else DarkCharcoal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SanctuaryButton(
            text = stringResource(id = R.string.ta_complete_btn),
            onClick = onCompleteClick,
            backgroundColor = Color(0xFF004D40),
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.ta_complete_btn),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}
