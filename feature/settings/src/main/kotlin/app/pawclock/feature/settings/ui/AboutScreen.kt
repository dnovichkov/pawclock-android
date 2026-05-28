package app.pawclock.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pawclock.feature.settings.R
import app.pawclock.feature.settings.about.AboutSource
import app.pawclock.feature.settings.about.AboutSources

/**
 * Экран "О приложении" (§5.3, Task 21 / Plan 1).
 *
 * Статический контент:
 *  - название и версия приложения (передаётся параметром, по умолчанию плейсхолдер);
 *  - лицензия Apache 2.0;
 *  - ссылка на GitHub-репо;
 *  - дисклеймер из §3.3 спецификации;
 *  - список научных источников (см. [AboutSources]).
 *
 * AboutScreen — pure Composable, без ViewModel'и: данные либо статичны, либо
 * приходят через параметры (`appVersion`). Это упрощает testability и снижает
 * накладные расходы — экран не имеет state'а, который должен сохраняться при rotation.
 *
 * @param appVersion версия приложения для отображения (обычно из `BuildConfig.VERSION_NAME`).
 *   Передача параметром, а не чтение из BuildConfig напрямую, чтобы:
 *   (1) этот модуль не зависел от `:app` BuildConfig;
 *   (2) Compose preview мог рендерить с любой версией.
 * @param onBack кнопка возврата в Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appVersion: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.about_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        AboutBody(appVersion = appVersion, padding = padding)
    }
}

@Composable
private fun AboutBody(
    appVersion: String,
    padding: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SIDE_PADDING_DP.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP_DP.dp),
    ) {
        AppIdentityBlock(appVersion = appVersion)

        HorizontalDivider()

        LicenseBlock()

        HorizontalDivider()

        RepositoryBlock()

        HorizontalDivider()

        DisclaimerBlock()

        HorizontalDivider()

        SourcesBlock()
    }
}

@Composable
private fun AppIdentityBlock(appVersion: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SECTION_INNER_GAP_DP.dp),
        modifier = Modifier.padding(top = SECTION_TOP_PADDING_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.about_app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.about_version, appVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(VERSION_TEST_TAG),
        )
        Text(
            text = stringResource(R.string.about_app_tagline),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LicenseBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_INNER_GAP_DP.dp)) {
        Text(
            text = stringResource(R.string.about_license_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.about_license_name),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.about_license_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RepositoryBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_INNER_GAP_DP.dp)) {
        Text(
            text = stringResource(R.string.about_repository_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.about_repository_url),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(REPOSITORY_LINK_TEST_TAG),
        )
        Text(
            text = stringResource(R.string.about_repository_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DisclaimerBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_INNER_GAP_DP.dp)) {
        Text(
            text = stringResource(R.string.about_disclaimer_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.about_disclaimer_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(DISCLAIMER_TEST_TAG),
        )
    }
}

@Composable
private fun SourcesBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_INNER_GAP_DP.dp)) {
        Text(
            text = stringResource(R.string.about_sources_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(SOURCES_HEADER_TEST_TAG),
        )
        AboutSources.all.forEachIndexed { index, source ->
            SourceItem(source = source)
            if (index != AboutSources.all.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = SOURCE_DIVIDER_PADDING_DP.dp),
                )
            }
        }
    }
}

@Composable
private fun SourceItem(source: AboutSource) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SOURCE_LINE_GAP_DP.dp),
        modifier = Modifier.testTag(sourceItemTag(source)),
    ) {
        Text(
            text = source.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "${source.authors} (${source.year})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = source.reference,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

internal fun sourceItemTag(source: AboutSource): String =
    "about_source_${source.authors.substringBefore(" ").lowercase()}_${source.year}"

internal const val VERSION_TEST_TAG: String = "about_version"
internal const val DISCLAIMER_TEST_TAG: String = "about_disclaimer"
internal const val REPOSITORY_LINK_TEST_TAG: String = "about_repository_link"
internal const val SOURCES_HEADER_TEST_TAG: String = "about_sources_header"

private const val SIDE_PADDING_DP: Int = 16
private const val SECTION_GAP_DP: Int = 24
private const val SECTION_INNER_GAP_DP: Int = 8
private const val SECTION_TOP_PADDING_DP: Int = 16
private const val SOURCE_DIVIDER_PADDING_DP: Int = 8
private const val SOURCE_LINE_GAP_DP: Int = 4
