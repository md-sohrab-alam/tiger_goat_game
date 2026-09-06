package com.sohrab.baghbakri.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.settings.AppLanguage
import com.sohrab.baghbakri.settings.AppPreferences
import com.sohrab.baghbakri.settings.AppSettings
import com.sohrab.baghbakri.ui.theme.ScreenBackground
import com.sohrab.baghbakri.ui.theme.ScreenBackgroundDark

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    var settings by remember { mutableStateOf(prefs.load()) }
    val selectedLanguage = prefs.getLanguage()

    fun update(next: AppSettings) {
        settings = next
        prefs.save(next)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackground, ScreenBackgroundDark)))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SectionTitle(stringResource(R.string.language_section))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = selectedLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(language.nativeLabel) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.section_board_help))

            SettingsToggle(
                title = stringResource(R.string.setting_move_hints),
                subtitle = stringResource(R.string.setting_move_hints_sub),
                checked = settings.showMoveHints,
                onCheckedChange = { checked -> update(settings.copy(showMoveHints = checked)) }
            )
            SettingsToggle(
                title = stringResource(R.string.setting_status_tips),
                subtitle = stringResource(R.string.setting_status_tips_sub),
                checked = settings.showStatusTips,
                onCheckedChange = { checked -> update(settings.copy(showStatusTips = checked)) }
            )
            SettingsToggle(
                title = stringResource(R.string.setting_last_move),
                subtitle = stringResource(R.string.setting_last_move_sub),
                checked = settings.showLastMove,
                onCheckedChange = { checked -> update(settings.copy(showLastMove = checked)) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.section_feel))

            SettingsToggle(
                title = stringResource(R.string.setting_haptics),
                subtitle = stringResource(R.string.setting_haptics_sub),
                checked = settings.hapticFeedback,
                onCheckedChange = { checked -> update(settings.copy(hapticFeedback = checked)) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(stringResource(R.string.section_help_legal))

            OutlinedButton(
                onClick = onOpenTutorial,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(stringResource(R.string.how_to_play_tutorial))
            }
            OutlinedButton(
                onClick = onOpenPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(stringResource(R.string.privacy_policy))
            }

            val shareText = stringResource(R.string.share_game_text)
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.share_game))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(stringResource(R.string.share_game))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.section_about))
            Text(
                text = stringResource(R.string.about_version),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.about_offline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.about_regional),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
