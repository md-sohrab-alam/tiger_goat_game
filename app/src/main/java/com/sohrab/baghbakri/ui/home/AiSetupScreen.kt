package com.sohrab.baghbakri.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.game.AiDifficulty
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.ui.game.localizedLabel
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.GoatPieceBorder
import com.sohrab.baghbakri.ui.theme.GoatTurnColor
import com.sohrab.baghbakri.ui.theme.GoatTurnContainer
import com.sohrab.baghbakri.ui.theme.ScreenBackground
import com.sohrab.baghbakri.ui.theme.ScreenBackgroundDark
import com.sohrab.baghbakri.ui.theme.TigerPiece
import com.sohrab.baghbakri.ui.theme.TigerPieceDark
import com.sohrab.baghbakri.ui.theme.TigerTurnColor
import com.sohrab.baghbakri.ui.theme.TigerTurnContainer

@Composable
fun AiSetupScreen(
    onStart: (GameSession) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var humanSide by rememberSaveable { mutableStateOf(PlayerSide.GOAT.name) }
    var difficulty by rememberSaveable { mutableStateOf(AiDifficulty.MEDIUM.name) }

    val selectedSide = PlayerSide.valueOf(humanSide)
    val selectedDifficulty = AiDifficulty.valueOf(difficulty)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ScreenBackground, ScreenBackgroundDark))
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.play_vs_ai),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ai_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.play_as),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SidePickCard(
                title = stringResource(R.string.goats),
                detail = stringResource(R.string.goats_detail),
                selected = selectedSide == PlayerSide.GOAT,
                isTiger = false,
                onClick = { humanSide = PlayerSide.GOAT.name },
                modifier = Modifier.weight(1f)
            )
            SidePickCard(
                title = stringResource(R.string.tigers),
                detail = stringResource(R.string.tigers_detail),
                selected = selectedSide == PlayerSide.TIGER,
                isTiger = true,
                onClick = { humanSide = PlayerSide.TIGER.name },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.difficulty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiDifficulty.entries.forEach { level ->
                FilterChip(
                    selected = selectedDifficulty == level,
                    onClick = { difficulty = level.name },
                    label = { Text(level.localizedLabel()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = {
                onStart(
                    GameSession(
                        mode = GameMode.VS_AI,
                        humanSide = selectedSide,
                        difficulty = selectedDifficulty
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedSide == PlayerSide.TIGER) TigerTurnColor else GoatTurnColor
            )
        ) {
            Text(
                text = stringResource(R.string.start_game),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun SidePickCard(
    title: String,
    detail: String,
    selected: Boolean,
    isTiger: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isTiger) TigerTurnColor else GoatTurnColor
    val container = if (isTiger) TigerTurnContainer else GoatTurnContainer
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) accent else accent.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isTiger) TigerPieceDark else GoatPieceBorder),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isTiger) TigerPiece else GoatPiece)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.selected),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}
