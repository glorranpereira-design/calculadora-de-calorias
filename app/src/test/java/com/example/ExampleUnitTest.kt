package com.example

import com.example.model.ActivityLevel
import com.example.model.BiologicalSex
import com.example.model.NutritionCalculator
import com.example.model.NutritionGoal
import com.example.model.NutritionInput
import com.example.ui.theme.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test Mifflin-St Jeor formula for male`() {
        val input = NutritionInput(
            sex = BiologicalSex.MALE,
            age = 25,
            weightKg = 80.0,
            heightCm = 180,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = NutritionGoal.MAINTENANCE
        )
        val result = NutritionCalculator.calculate(input)
        assertEquals(1805, result.tmb)
        assertEquals(2166, result.get)
        assertEquals(2166, result.targetCalories)
    }

    @Test
    fun `test Mifflin-St Jeor formula for female`() {
        val input = NutritionInput(
            sex = BiologicalSex.FEMALE,
            age = 30,
            weightKg = 60.0,
            heightCm = 165,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            goal = NutritionGoal.WEIGHT_LOSS
        )
        val result = NutritionCalculator.calculate(input)
        assertEquals(1320, result.tmb)
        assertEquals(2046, result.get)
        assertEquals(1546, result.targetCalories)
    }

    @Test
    fun `test protein cap at 35 percent prevents negative carbohydrates`() {
        val input = NutritionInput(
            sex = BiologicalSex.FEMALE,
            age = 45,
            weightKg = 130.0,
            heightCm = 155,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = NutritionGoal.WEIGHT_LOSS
        )
        val result = NutritionCalculator.calculate(input)
        assertTrue("Carb grams must be strictly positive", result.carbs.grams > 0)
        assertTrue("Protein calories must be at most 35% of target", result.protein.calories <= (result.targetCalories * 0.35) + 10)
        assertTrue("Sum of macros is positive and healthy", result.protein.grams > 0 && result.fat.grams > 0)
    }

    @Test
    fun `test activity multipliers`() {
        assertEquals(1.2, ActivityLevel.SEDENTARY.factor, 0.001)
        assertEquals(1.375, ActivityLevel.LIGHTLY_ACTIVE.factor, 0.001)
        assertEquals(1.55, ActivityLevel.MODERATELY_ACTIVE.factor, 0.001)
        assertEquals(1.725, ActivityLevel.VERY_ACTIVE.factor, 0.001)
        assertEquals(1.9, ActivityLevel.EXTREMELY_ACTIVE.factor, 0.001)
    }

    @Test
    fun `test goal adjustments`() {
        assertEquals(-500, NutritionGoal.WEIGHT_LOSS.calorieAdjustment)
        assertEquals(0, NutritionGoal.MAINTENANCE.calorieAdjustment)
        assertEquals(400, NutritionGoal.MUSCLE_GAIN.calorieAdjustment)
    }

    @Test
    fun `test app theme modes defined`() {
        val modes = AppThemeMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AppThemeMode.SYSTEM))
        assertTrue(modes.contains(AppThemeMode.LIGHT))
        assertTrue(modes.contains(AppThemeMode.DARK))
    }
}
