package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.model.ActivityLevel
import com.example.model.BiologicalSex
import com.example.model.NutritionCalculator
import com.example.model.NutritionGoal
import com.example.model.NutritionInput
import com.example.model.NutritionResult
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppScreen {
    INPUT,
    RESULT
}

data class SavedPlanSummary(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val title: String,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
    val input: NutritionInput
)

data class NutritionUiState(
    val currentScreen: AppScreen = AppScreen.INPUT,
    val input: NutritionInput = NutritionInput(),
    val result: NutritionResult = NutritionCalculator.calculate(NutritionInput()),
    val isEbookSheetOpen: Boolean = false,
    val isThemeSettingsOpen: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val savedHistory: List<SavedPlanSummary> = emptyList(),
    val snackbarMessage: String? = null
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("nutricao_na_pratica_prefs", Context.MODE_PRIVATE)
    private val keyThemeMode = "pref_key_app_theme_mode"

    private val _uiState = MutableStateFlow(
        NutritionUiState(
            themeMode = loadSavedThemeMode()
        )
    )
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private fun loadSavedThemeMode(): AppThemeMode {
        val savedName = sharedPrefs.getString(keyThemeMode, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        sharedPrefs.edit().putString(keyThemeMode, mode.name).apply()
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleQuickTheme() {
        val current = _uiState.value.themeMode
        val nextMode = when (current) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    fun setThemeSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isThemeSettingsOpen = isOpen) }
    }

    fun updateSex(sex: BiologicalSex) {
        _uiState.update { state ->
            val newInput = state.input.copy(sex = sex)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun updateAge(age: Int) {
        val safeAge = age.coerceIn(14, 100)
        _uiState.update { state ->
            val newInput = state.input.copy(age = safeAge)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun updateWeight(weightKg: Double) {
        val safeWeight = (weightKg.coerceIn(35.0, 250.0) * 10).toInt() / 10.0
        _uiState.update { state ->
            val newInput = state.input.copy(weightKg = safeWeight)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun updateHeight(heightCm: Int) {
        val safeHeight = heightCm.coerceIn(120, 230)
        _uiState.update { state ->
            val newInput = state.input.copy(heightCm = safeHeight)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun updateActivityLevel(activityLevel: ActivityLevel) {
        _uiState.update { state ->
            val newInput = state.input.copy(activityLevel = activityLevel)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun updateGoal(goal: NutritionGoal) {
        _uiState.update { state ->
            val newInput = state.input.copy(goal = goal)
            state.copy(
                input = newInput,
                result = NutritionCalculator.calculate(newInput)
            )
        }
    }

    fun applyPreset(presetInput: NutritionInput) {
        _uiState.update { state ->
            state.copy(
                input = presetInput,
                result = NutritionCalculator.calculate(presetInput)
            )
        }
    }

    fun navigateToResult() {
        _uiState.update { it.copy(currentScreen = AppScreen.RESULT) }
    }

    fun navigateToInput() {
        _uiState.update { it.copy(currentScreen = AppScreen.INPUT) }
    }

    fun setEbookSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isEbookSheetOpen = isOpen) }
    }

    fun saveCurrentCalculation() {
        val result = _uiState.value.result
        val input = _uiState.value.input
        val item = SavedPlanSummary(
            timestamp = "Salvo agora",
            title = "${input.goal.title} (${input.weightKg} kg, ${input.age}a)",
            targetCalories = result.targetCalories,
            proteinGrams = result.protein.grams,
            carbGrams = result.carbs.grams,
            fatGrams = result.fat.grams,
            input = input
        )
        _uiState.update { state ->
            state.copy(
                savedHistory = listOf(item) + state.savedHistory.take(4),
                snackbarMessage = "Ponto de partida salvo com sucesso!"
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun generateShareText(): String {
        val res = _uiState.value.result
        val inp = res.input
        return """
            📊 Meu Ponto de Partida — Nutrição na Prática
            
            🎯 Objetivo: ${inp.goal.title}
            🔥 Calorias-Alvo: ${res.targetCalories} kcal/dia
            ⚡ Gasto Diário (GET): ${res.get} kcal/dia
            🌱 TMB: ${res.tmb} kcal/dia
            
            Distribuição de Macronutrientes:
            🍗 Proteína: ${res.protein.grams}g (${res.protein.percentage}%)
            🍚 Carboidrato: ${res.carbs.grams}g (${res.carbs.percentage}%)
            🥑 Gordura: ${res.fat.grams}g (${res.fat.percentage}%)
            
            Calculado com a ferramenta Nutrição na Prática (Mifflin-St Jeor).
            *Estimativa de ponto de partida. Não substitui consulta nutricional individual.*
        """.trimIndent()
    }
}
