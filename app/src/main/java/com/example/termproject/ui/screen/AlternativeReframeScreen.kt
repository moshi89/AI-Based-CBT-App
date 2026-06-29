package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 4 — [Path A] 대안 생각 합리화 및 저장 화면 (Alternative Reframe)
// ══════════════════════════════════════════════════════════════

@Composable
fun AlternativeReframeScreen(
    result: CbtAnalysis,
    selectedOption: String?,
    currentLanguage: String,
    onSaveClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            ThemeSectionHeader(
                stepLabel = stringResource(id = R.string.ar_step_label),
                title = stringResource(id = R.string.ar_title),
                subtitle = stringResource(id = R.string.ar_subtitle)
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.ar_alternatives_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(result.alternativeThoughts) { thought ->
            val cardShape = RoundedCornerShape(16.dp)
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = SoftSageLight.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = SagePrimary.copy(alpha = 0.2f),
                        shape = cardShape
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Alternative",
                        tint = SagePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = thought,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkCharcoal,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 선택 옵션 리캡 카드 (Step 1 original thought 카드 디자인과 통일)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.ar_accepted_link),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedOption ?: "",
                        fontSize = 13.sp,
                        color = DarkCharcoal,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SanctuaryButton(
                text = stringResource(id = R.string.ar_save_btn),
                onClick = onSaveClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.ar_save_btn),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}