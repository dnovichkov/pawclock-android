package app.pawclock.model

/**
 * Стадия жизни питомца. Дискриминируется типом — для собак и кошек разные подтипы.
 *
 * Каждая стадия имеет:
 *  - [displayKey] для поиска локализованной строки и пути к care-recommendations
 *    (формат: `{species_id}_{stage_id}`, например `dog_senior`);
 *  - [ordinal] для упорядочивания (стадии монотонно растут от младшей к старшей).
 *
 * Для собак — стадии AAHA 2019 Canine Life Stage Guidelines.
 * Для кошек — стадии AAHA/AAFP 2021 Feline Life Stage Guidelines (DOI: 10.1177/1098612X21993657).
 *
 * См. спецификацию PawClock §4.1, §4.2.
 */
sealed class LifeStage(
    val displayKey: String,
    val ordinal: Int,
) {
    /**
     * Стадии жизни собак (AAHA 2019).
     *
     * Пороги senior зависят от размера ([DogSize]) и реализуются в `:core:calculator`:
     *  - Toy/Small: senior с 11+ лет
     *  - Medium: senior с 9+ лет
     *  - Large: senior с 7+ лет
     *  - Giant: senior с 5+ лет (§4.1 спецификации, McMillan 2024)
     */
    sealed class Dog(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Puppy : Dog(displayKey = "dog_puppy", ordinal = 0)

        data object YoungAdult : Dog(displayKey = "dog_young_adult", ordinal = 1)

        data object MatureAdult : Dog(displayKey = "dog_mature_adult", ordinal = 2)

        data object Senior : Dog(displayKey = "dog_senior", ordinal = 3)

        data object EndOfLife : Dog(displayKey = "dog_end_of_life", ordinal = 4)

        companion object {
            fun all(): List<Dog> = listOf(Puppy, YoungAdult, MatureAdult, Senior, EndOfLife)
        }
    }

    /**
     * Стадии жизни кошек (AAHA/AAFP 2021).
     *
     * Пороги по возрасту:
     *  - Kitten: 0–1 год
     *  - Young Adult: 1–6 лет
     *  - Mature Adult: 7–10 лет
     *  - Senior: 10+ лет
     *  - End of Life: приближение к expected lifespan (12–18 indoor, 2–5 outdoor)
     */
    sealed class Cat(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Kitten : Cat(displayKey = "cat_kitten", ordinal = 0)

        data object YoungAdult : Cat(displayKey = "cat_young_adult", ordinal = 1)

        data object MatureAdult : Cat(displayKey = "cat_mature_adult", ordinal = 2)

        data object Senior : Cat(displayKey = "cat_senior", ordinal = 3)

        data object EndOfLife : Cat(displayKey = "cat_end_of_life", ordinal = 4)

        companion object {
            fun all(): List<Cat> = listOf(Kitten, YoungAdult, MatureAdult, Senior, EndOfLife)
        }
    }
}
