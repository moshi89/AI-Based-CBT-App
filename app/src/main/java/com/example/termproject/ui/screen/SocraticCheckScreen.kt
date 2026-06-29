package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 4 — [Path A] 소크라테스 반문 질문 화면
// ══════════════════════════════════════════════════════════════

@Composable
fun SocraticCheckScreen(
    result: CbtAnalysis,
    selectedOption: String?,
    currentLanguage: String,
    onOptionSelect: (String) -> Unit,
    onNextClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── 스크롤 가능한 콘텐츠 영역 ──
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 88.dp)
        ) {
            item {
                SanctuaryStepIndicator(currentStep = 2)
                Spacer(modifier = Modifier.height(14.dp))
                ThemeSectionHeader(
                    stepLabel = stringResource(id = R.string.sc_step_label),
                    title = stringResource(id = R.string.sc_title),
                    subtitle = stringResource(id = R.string.sc_subtitle)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SagePrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.sc_step_label),
                            tint = SagePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.socraticQuestion,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = DarkCharcoal,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            items(result.options.size) { index ->
                val option = result.options[index]
                val isSelected = selectedOption == option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { onOptionSelect(option) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SoftSageLight.copy(alpha = 0.15f)
                        else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SagePrimary else Color.LightGray.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = SagePrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkCharcoal,
                            modifier = Modifier.weight(1f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // ── 하단 고정 버튼 ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 14.dp)
        ) {
            SanctuaryButton(
                text = stringResource(id = R.string.sc_accept_btn),
                enabled = selectedOption != null,
                onClick = onNextClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(id = R.string.sc_accept_btn),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
