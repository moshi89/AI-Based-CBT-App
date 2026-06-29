package com.example.termproject.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import android.location.Location // ✅ Required for distanceBetween
import android.content.Intent
import android.net.Uri
// ══════════════════════════════════════════════════════════════
// STEP 7 — [Path B] 행동적 목적지 실제 구글 맵 화면
// ══════════════════════════════════════════════════════════════
fun openGoogleMaps(
    context: Context,
    navigationPath: List<LatLng>
) {
    if (navigationPath.size < 2) return

    val destination = navigationPath.last()

    val waypoints = navigationPath
        .drop(1)
        .dropLast(1)
        .joinToString("|") {
            "${it.latitude},${it.longitude}"
        }

    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1" +
                "&destination=${destination.latitude},${destination.longitude}"

    )

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    context.startActivity(intent)
}
@Composable
fun MapNavigationScreen(
    activity: BehavioralActivity,
    currentLocation: LatLng?,
    destinationLocation: LatLng?,
    navigationPath: List<LatLng>,
    currentLanguage: String,
    onPermissionGranted: () -> Unit,
    onStartClick: () -> Unit,
    onArrived: () -> Unit,
    trackedPath: List<LatLng>
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasPermission = true
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            onPermissionGranted()
        }
    }
// In MapNavigationScreen, add this LaunchedEffect
    LaunchedEffect(currentLocation, destinationLocation) {
        if (currentLocation != null && destinationLocation != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                destinationLocation.latitude, destinationLocation.longitude,
                results
            )
            // If distance < 50 meters, trigger arrival
            if (results[0] < 50) {
                onArrived() // You need to pass this callback to the screen
            }
        }
    }
    // 로컬라이징된 필드 바인딩 (Context 기반 동적 획득)
    val resNameId = context.resources.getIdentifier("act_${activity.id}_name", "string", context.packageName)
    val displayName = if (resNameId != 0) context.getString(resNameId) else activity.name

    val resPurposeId = context.resources.getIdentifier("act_${activity.id}_purpose", "string", context.packageName)
    val displayPurpose = if (resPurposeId != 0) context.getString(resPurposeId) else activity.purpose

    val resDurationId = context.resources.getIdentifier("act_${activity.id}_duration", "string", context.packageName)
    val displayDuration = if (resDurationId != 0) context.getString(resDurationId) else activity.durationText

    val resInstructionId = context.resources.getIdentifier("act_${activity.id}_instruction", "string", context.packageName)
    val displayInstruction = if (resInstructionId != 0) context.getString(resInstructionId) else activity.instruction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SanctuaryStepIndicator(currentStep = 3)
            Spacer(modifier = Modifier.height(14.dp))
            ThemeSectionHeader(
                stepLabel = stringResource(id = R.string.map_step_label),
                title = displayName,
                subtitle = stringResource(id = R.string.map_subtitle_format, displayDuration, activity.distance)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🗺️ Google Maps View Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4E8)),
                border = BorderStroke(1.dp, SagePrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!hasPermission) {
                        // 권한 미허가 시 안내 및 요청 버튼
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location Icon",
                                tint = SagePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(id = R.string.map_permission_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.map_permission_body),
                                fontSize = 11.sp,
                                color = MediumGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.map_permission_btn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (currentLocation == null) {
                        // GPS 신호 대기 중
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SagePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(id = R.string.map_gps_searching),
                                fontSize = 13.sp,
                                color = SagePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.map_gps_hint),
                                fontSize = 10.sp,
                                color = MediumGray
                            )
                        }
                    } else {
                        // 실제 구글 맵 렌더링
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(currentLocation, 15.5f)
                        }

                        // 사용자 위치 변화에 맞춰 부드럽게 지도의 중심 이동
                        LaunchedEffect(currentLocation) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentLocation, 15.5f),
                                1000
                            )
                        }

                        // In MapNavigationScreen.kt
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {

                            currentLocation?.let {
                                Marker(
                                    state = MarkerState(position = it),
                                    title = "Current Location"
                                )
                            }

                            destinationLocation?.let {
                                Marker(
                                    state = MarkerState(position = it),
                                    title = "Destination"
                                )
                            }

                            if (navigationPath.isNotEmpty()) {
                                Polyline(
                                    points = navigationPath,
                                    width = 10f
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = stringResource(id = R.string.map_guidelines_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = displayInstruction,
                                    fontSize = 11.sp,
                                    color = DarkCharcoal,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }


            Button(
                onClick = {
                    openGoogleMaps(
                        context,
                        navigationPath
                    )
                },
                enabled = navigationPath.isNotEmpty()
            ) {
                Text("Navigate with Google Maps")
            }

        }




        SanctuaryButton(
            text = stringResource(id = R.string.map_start_btn),
            onClick = onStartClick,
            backgroundColor = Color(0xFF004D40),
            enabled = hasPermission && currentLocation != null,
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.map_start_cd),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}




