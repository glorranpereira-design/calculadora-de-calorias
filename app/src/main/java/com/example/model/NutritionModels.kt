package com.example.model

import java.util.Locale
import kotlin.math.roundToInt

enum class BiologicalSex(val label: String) {
    MALE("Masculino"),
    FEMALE("Feminino")
}

enum class ActivityLevel(
    val title: String,
    val description: String,
    val factor: Double
) {
    SEDENTARY(
        title = "Sedentário",
        description = "Pouco ou nenhum exercício",
        factor = 1.2
    ),
    LIGHTLY_ACTIVE(
        title = "Levemente ativo",
        description = "Exercício leve 1 a 3x por semana",
        factor = 1.375
    ),
    MODERATELY_ACTIVE(
        title = "Moderadamente ativo",
        description = "Exercício moderado 3 a 5x por semana",
        factor = 1.55
    ),
    VERY_ACTIVE(
        title = "Muito ativo",
        description = "Exercício intenso 6 a 7x por semana",
        factor = 1.725
    ),
    EXTREMELY_ACTIVE(
        title = "Extremamente ativo",
        description = "Exercício intenso + trabalho físico diário",
        factor = 1.9
    )
}

enum class NutritionGoal(
    val title: String,
    val subtitle: String,
    val calorieAdjustment: Int
) {
    WEIGHT_LOSS(
        title = "Emagrecimento",
        subtitle = "Déficit controlado de 500 kcal para queima sustentável de gordura",
        calorieAdjustment = -500
    ),
    MAINTENANCE(
        title = "Manutenção",
        subtitle = "Equilíbrio calórico para manter peso e estabilidade",
        calorieAdjustment = 0
    ),
    MUSCLE_GAIN(
        title = "Ganho de massa",
        subtitle = "Superávit moderado de 400 kcal para síntese muscular",
        calorieAdjustment = 400
    )
}

data class NutritionInput(
    val sex: BiologicalSex = BiologicalSex.FEMALE,
    val age: Int = 30,
    val weightKg: Double = 68.0,
    val heightCm: Int = 165,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val goal: NutritionGoal = NutritionGoal.WEIGHT_LOSS
)

data class MacroDetail(
    val name: String,
    val grams: Int,
    val calories: Int,
    val percentage: Int,
    val perKg: Double? = null
)

data class MealPortion(
    val mealName: String,
    val timeSuggestion: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
    val practicalExample: String
)

data class NutritionResult(
    val input: NutritionInput,
    val tmb: Int,
    val get: Int,
    val targetCalories: Int,
    val protein: MacroDetail,
    val carbs: MacroDetail,
    val fat: MacroDetail,
    val practicalExplanation: String,
    val mealSuggestions: List<MealPortion>
)

object NutritionCalculator {

    /**
     * Calculates TMB, GET and macronutrients according to strict Mifflin-St Jeor specifications:
     *
     * TMB:
     * Homem: 10 * peso + 6.25 * altura - 5 * idade + 5
     * Mulher: 10 * peso + 6.25 * altura - 5 * idade - 161
     *
     * GET: TMB * fator_atividade
     *
     * Calorias-alvo:
     * Emagrecimento: GET - 500
     * Manutenção: GET
     * Ganho de massa: GET + 400
     *
     * Macros:
     * Proteína: 2g por kg de peso corporal -> kcal = g * 4
     * Gordura: 25% das calorias-alvo -> gordura_g = (calorias-alvo * 0.25) / 9
     * Carboidrato: restante -> kcal = calorias - kcal_prot - (gordura_g * 9) -> g = kcal / 4
     * Se proteína ultrapassar 35% das calorias-alvo (em pesos altos), limitar proteína a 35%.
     */
    fun calculate(input: NutritionInput): NutritionResult {
        val tmbDouble = when (input.sex) {
            BiologicalSex.MALE -> {
                (10.0 * input.weightKg) + (6.25 * input.heightCm) - (5.0 * input.age) + 5.0
            }
            BiologicalSex.FEMALE -> {
                (10.0 * input.weightKg) + (6.25 * input.heightCm) - (5.0 * input.age) - 161.0
            }
        }
        val tmb = tmbDouble.roundToInt().coerceAtLeast(800)

        val getDouble = tmbDouble * input.activityLevel.factor
        val get = getDouble.roundToInt()

        val rawTargetCalories = get + input.goal.calorieAdjustment
        // Ensure a healthy minimal threshold
        val targetCalories = rawTargetCalories.coerceAtLeast(1100)

        // Fat: 25% of target calories
        val fatCalories = targetCalories * 0.25
        val fatGrams = (fatCalories / 9.0).roundToInt().coerceAtLeast(20)
        val actualFatCalories = fatGrams * 9

        // Protein: standard 2g per kg of body weight
        var desiredProteinGrams = 2.0 * input.weightKg
        var proteinCalories = desiredProteinGrams * 4.0

        // Rule: if protein exceeds 35% of target calories, cap at 35%
        val maxProteinCalories = targetCalories * 0.35
        if (proteinCalories > maxProteinCalories) {
            proteinCalories = maxProteinCalories
            desiredProteinGrams = proteinCalories / 4.0
        }
        val proteinGrams = desiredProteinGrams.roundToInt().coerceAtLeast(40)
        val actualProteinCalories = proteinGrams * 4

        // Carbohydrates: remaining calories
        var carbCalories = targetCalories - actualProteinCalories - actualFatCalories
        val minCarbCalories = (targetCalories * 0.15).roundToInt()
        if (carbCalories < minCarbCalories) {
            // Guardrail to keep minimum balanced intake
            carbCalories = minCarbCalories
        }
        val carbGrams = (carbCalories / 4.0).roundToInt().coerceAtLeast(30)
        val actualCarbCalories = carbGrams * 4

        val totalMacroCalories = actualProteinCalories + actualCarbCalories + actualFatCalories
        val proteinPercent = ((actualProteinCalories.toDouble() / totalMacroCalories) * 100).roundToInt()
        val carbPercent = ((actualCarbCalories.toDouble() / totalMacroCalories) * 100).roundToInt()
        val fatPercent = (100 - proteinPercent - carbPercent).coerceAtLeast(0)

        val proteinDetail = MacroDetail(
            name = "Proteína",
            grams = proteinGrams,
            calories = actualProteinCalories,
            percentage = proteinPercent,
            perKg = proteinGrams.toDouble() / input.weightKg
        )

        val carbDetail = MacroDetail(
            name = "Carboidrato",
            grams = carbGrams,
            calories = actualCarbCalories,
            percentage = carbPercent
        )

        val fatDetail = MacroDetail(
            name = "Gordura",
            grams = fatGrams,
            calories = actualFatCalories,
            percentage = fatPercent
        )

        val practicalExplanation = buildPracticalExplanation(
            proteinGrams = proteinGrams,
            carbGrams = carbGrams,
            fatGrams = fatGrams,
            weightKg = input.weightKg
        )

        val mealSuggestions = generateMealSuggestions(
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbGrams = carbGrams,
            fatGrams = fatGrams
        )

        return NutritionResult(
            input = input,
            tmb = tmb,
            get = get,
            targetCalories = targetCalories,
            protein = proteinDetail,
            carbs = carbDetail,
            fat = fatDetail,
            practicalExplanation = practicalExplanation,
            mealSuggestions = mealSuggestions
        )
    }

    private fun buildPracticalExplanation(
        proteinGrams: Int,
        carbGrams: Int,
        fatGrams: Int,
        weightKg: Double
    ): String {
        // Concrete examples without prescribing a rigid closed diet
        val approxChickenBreastGrams = (proteinGrams * 3.3).roundToInt() // ~30g protein per 100g chicken
        val approxCookedRiceGrams = (carbGrams * 3.5).roundToInt() // ~28g carb per 100g cooked rice
        val oliveOilTablespoons = String.format(Locale.US, "%.1f", fatGrams / 13.0) // ~13g fat per spoon

        return "Na prática, uma meta de ${proteinGrams}g de proteína, ${carbGrams}g de carboidrato e ${fatGrams}g de gordura equivale a aproximadamente ${approxChickenBreastGrams}g de peito de frango (ou equivalente em ovos, peixes e laticínios), ${approxCookedRiceGrams}g de arroz cozido (ou raízes como mandioca, batata e aveia) e cerca de $oliveOilTablespoons colheres de sopa de azeite extravirgem (ou castanhas e sementes), distribuídos ao longo das suas refeições do dia."
    }

    private fun generateMealSuggestions(
        targetCalories: Int,
        proteinGrams: Int,
        carbGrams: Int,
        fatGrams: Int
    ): List<MealPortion> {
        return listOf(
            MealPortion(
                mealName = "Café da Manhã",
                timeSuggestion = "07:00 – 08:30",
                calories = (targetCalories * 0.25).roundToInt(),
                proteinGrams = (proteinGrams * 0.25).roundToInt(),
                carbGrams = (carbGrams * 0.25).roundToInt(),
                fatGrams = (fatGrams * 0.25).roundToInt(),
                practicalExample = "Ex: 2 a 3 ovos mexidos com aveia em flocos e 1 fruta fresca"
            ),
            MealPortion(
                mealName = "Almoço",
                timeSuggestion = "12:00 – 13:30",
                calories = (targetCalories * 0.35).roundToInt(),
                proteinGrams = (proteinGrams * 0.35).roundToInt(),
                carbGrams = (carbGrams * 0.35).roundToInt(),
                fatGrams = (fatGrams * 0.35).roundToInt(),
                practicalExample = "Ex: Peito de frango/peixe grelhado, arroz, feijão, azeite e prato cheio de vegetais"
            ),
            MealPortion(
                mealName = "Lanche da Tarde",
                timeSuggestion = "16:00 – 17:00",
                calories = (targetCalories * 0.15).roundToInt(),
                proteinGrams = (proteinGrams * 0.15).roundToInt(),
                carbGrams = (carbGrams * 0.15).roundToInt(),
                fatGrams = (fatGrams * 0.15).roundToInt(),
                practicalExample = "Ex: Iogurte natural com punhado de castanhas ou fruta com canela"
            ),
            MealPortion(
                mealName = "Jantar",
                timeSuggestion = "19:30 – 21:00",
                calories = (targetCalories * 0.25).roundToInt(),
                proteinGrams = (proteinGrams * 0.25).roundToInt(),
                carbGrams = (carbGrams * 0.25).roundToInt(),
                fatGrams = (fatGrams * 0.25).roundToInt(),
                practicalExample = "Ex: Proteína magra, mandioca ou batata cozida, salada generosa e azeite"
            )
        )
    }
}
