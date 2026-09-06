package com.sohrab.baghbakri.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.ui.theme.ScreenBackground
import com.sohrab.baghbakri.ui.theme.ScreenBackgroundDark

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackground, ScreenBackgroundDark)))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.privacy_policy),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.privacy_updated),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            PolicySection(R.string.privacy_overview_title, R.string.privacy_overview_body)
            PolicySection(R.string.privacy_collect_title, R.string.privacy_collect_body)
            PolicySection(R.string.privacy_stored_title, R.string.privacy_stored_body)
            PolicySection(R.string.privacy_ads_title, R.string.privacy_ads_body)
            PolicySection(R.string.privacy_children_title, R.string.privacy_children_body)
            PolicySection(R.string.privacy_changes_title, R.string.privacy_changes_body)
            PolicySection(R.string.privacy_contact_title, R.string.privacy_contact_body)
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
private fun PolicySection(titleRes: Int, bodyRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
    Text(
        text = stringResource(bodyRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    )
}
