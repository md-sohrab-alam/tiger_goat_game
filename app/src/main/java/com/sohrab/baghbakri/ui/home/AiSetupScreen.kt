package com.sohrab.baghbakri.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.game.AiDifficulty
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.PlayerSide

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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Play vs AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Play as",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSide == PlayerSide.GOAT,
                onClick = { humanSide = PlayerSide.GOAT.name },
                label = { Text("Goats") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedSide == PlayerSide.TIGER,
                onClick = { humanSide = PlayerSide.TIGER.name },
                label = { Text("Tigers") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.titleMedium,
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
                    label = { Text(level.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Game")
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
