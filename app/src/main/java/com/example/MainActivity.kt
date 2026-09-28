package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EbookOfferSheet
import com.example.ui.components.ThemeSettingsSheet
import com.example.ui.screens.InputScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.NutricaoNaPraticaTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NutritionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NutritionViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (uiState.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            NutricaoNaPraticaTheme(darkTheme = isDarkTheme) {
                MainApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: NutritionViewModel,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val ebookSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Trigger snackbars
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Android back button handling
    BackHandler(enabled = uiState.currentScreen == AppScreen.RESULT) {
        viewModel.navigateToInput()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.RESULT) {
                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                } else {
                    slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                }
            },
            label = "screen_transition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                AppScreen.INPUT -> {
                    InputScreen(
                        input = uiState.input,
                        isDarkTheme = isDarkTheme,
                        onSexChange = viewModel::updateSex,
                        onAgeChange = viewModel::updateAge,
                        onWeightChange = viewModel::updateWeight,
                        onHeightChange = viewModel::updateHeight,
                        onActivityLevelChange = viewModel::updateActivityLevel,
                        onGoalChange = viewModel::updateGoal,
                        onCalculateClick = viewModel::navigateToResult,
                        onToggleQuickTheme = viewModel::toggleQuickTheme,
                        onOpenThemeSettings = { viewModel.setThemeSettingsOpen(true) }
                    )
                }
                AppScreen.RESULT -> {
                    ResultScreen(
                        result = uiState.result,
                        isDarkTheme = isDarkTheme,
                        onBackToEdit = viewModel::navigateToInput,
                        onOpenEbookSheet = { viewModel.setEbookSheetOpen(true) },
                        onSavePlan = viewModel::saveCurrentCalculation,
                        onToggleQuickTheme = viewModel::toggleQuickTheme,
                        onOpenThemeSettings = { viewModel.setThemeSettingsOpen(true) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Meu Ponto de Partida — Nutrição na Prática")
                                putExtra(Intent.EXTRA_TEXT, viewModel.generateShareText())
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Compartilhar ponto de partida")
                            )
                        }
                    )
                }
            }
        }

        // Upsell bottom sheet for the complete e-book
        if (uiState.isEbookSheetOpen) {
            EbookOfferSheet(
                onDismissRequest = { viewModel.setEbookSheetOpen(false) },
                sheetState = ebookSheetState
            )
        }

        // Theme and visual settings bottom sheet
        if (uiState.isThemeSettingsOpen) {
            ThemeSettingsSheet(
                currentThemeMode = uiState.themeMode,
                onThemeSelect = viewModel::setThemeMode,
                onDismissRequest = { viewModel.setThemeSettingsOpen(false) },
                sheetState = themeSheetState
            )
        }
    }
}
