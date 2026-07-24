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

    /**
     * Стадии жизни кроликов (Oxbow Rabbit Life Stages, см. §4.3 спецификации).
     *
     * Пороги по возрасту (единые для всех пород):
     *  - Infancy: 0–3 мес.
     *  - Adolescence: 3–6 мес.
     *  - YoungAdult: 6–12 мес.
     *  - Adult: 1–5 лет
     *  - Senior: 5+ лет
     */
    sealed class Rabbit(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Infancy : Rabbit(displayKey = "rabbit_infancy", ordinal = 0)

        data object Adolescence : Rabbit(displayKey = "rabbit_adolescence", ordinal = 1)

        data object YoungAdult : Rabbit(displayKey = "rabbit_young_adult", ordinal = 2)

        data object Adult : Rabbit(displayKey = "rabbit_adult", ordinal = 3)

        data object Senior : Rabbit(displayKey = "rabbit_senior", ordinal = 4)

        companion object {
            fun all(): List<Rabbit> = listOf(Infancy, Adolescence, YoungAdult, Adult, Senior)
        }
    }

    /**
     * Стадии жизни хомяков (RVC VetCompass + Animallama, см. §4.4 спецификации).
     *
     * Пороги по возрасту (единые для всех видов; «старость с 1.5 лет» по §4.4):
     *  - Pup: 0 — ~3 нед.
     *  - Juvenile: ~3 нед. — 2 мес.
     *  - Adult: 2 мес. — 1 год
     *  - Senior: 1 — 1.5 года
     *  - VerySenior: 1.5+ года
     */
    sealed class Hamster(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Pup : Hamster(displayKey = "hamster_pup", ordinal = 0)

        data object Juvenile : Hamster(displayKey = "hamster_juvenile", ordinal = 1)

        data object Adult : Hamster(displayKey = "hamster_adult", ordinal = 2)

        data object Senior : Hamster(displayKey = "hamster_senior", ordinal = 3)

        data object VerySenior : Hamster(displayKey = "hamster_very_senior", ordinal = 4)

        companion object {
            fun all(): List<Hamster> = listOf(Pup, Juvenile, Adult, Senior, VerySenior)
        }
    }

    /**
     * Стадии жизни морских свинок (Oxbow + Animallama, см. §4.5 спецификации).
     *
     * Пороги по возрасту (единые для всех — у морской свинки нет подкатегорий;
     * «senior с 4 лет» по §4.5):
     *  - Pup: 0 — ~3 нед. (0–0.058, до отъёма)
     *  - Juvenile: ~3 нед. — 5 мес. (0.058–0.42, до половой/физической зрелости)
     *  - Adult: 5 мес. — 4 года (0.42–4.0)
     *  - Senior: 4 — 6 лет
     *  - Geriatric: 6+ лет (приближение к верхней границе ЧЖ 5–7)
     */
    sealed class GuineaPig(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Pup : GuineaPig(displayKey = "guinea_pig_pup", ordinal = 0)

        data object Juvenile : GuineaPig(displayKey = "guinea_pig_juvenile", ordinal = 1)

        data object Adult : GuineaPig(displayKey = "guinea_pig_adult", ordinal = 2)

        data object Senior : GuineaPig(displayKey = "guinea_pig_senior", ordinal = 3)

        data object Geriatric : GuineaPig(displayKey = "guinea_pig_geriatric", ordinal = 4)

        companion object {
            fun all(): List<GuineaPig> = listOf(Pup, Juvenile, Adult, Senior, Geriatric)
        }
    }

    /**
     * Стадии жизни крыс (Sengupta 2013, см. §4.6 спецификации).
     *
     * Пороги по возрасту (единые — у крысы нет подкатегорий; «senior с 18 мес.» по §4.6):
     *  - Pup: 0 — ~3 нед. (0–0.058, до отъёма)
     *  - Juvenile: ~3 нед. — 3 мес. (0.058–0.25, до социальной зрелости)
     *  - Adult: 3 мес. — 18 мес. (0.25–1.5)
     *  - Senior: 18 мес. — 30 мес. (1.5–2.5)
     *  - EndOfLife: 30+ мес. (приближение к верхней границе ЧЖ 2–3 года)
     */
    sealed class Rat(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Pup : Rat(displayKey = "rat_pup", ordinal = 0)

        data object Juvenile : Rat(displayKey = "rat_juvenile", ordinal = 1)

        data object Adult : Rat(displayKey = "rat_adult", ordinal = 2)

        data object Senior : Rat(displayKey = "rat_senior", ordinal = 3)

        data object EndOfLife : Rat(displayKey = "rat_end_of_life", ordinal = 4)

        companion object {
            fun all(): List<Rat> = listOf(Pup, Juvenile, Adult, Senior, EndOfLife)
        }
    }

    /**
     * Стадии жизни мышей (Dutta & Sengupta 2016, см. §4.6 спецификации).
     *
     * Пороги по возрасту (единые — у мыши нет подкатегорий; «senior с 12 мес.» по §4.6):
     *  - Pup: 0 — ~3 нед. (0–0.058, до отъёма)
     *  - Juvenile: ~3 нед. — 3 мес. (0.058–0.25, до социальной зрелости)
     *  - Adult: 3 мес. — 12 мес. (0.25–1.0)
     *  - Senior: 12 мес. — 24 мес. (1.0–2.0)
     *  - EndOfLife: 24+ мес. (приближение к верхней границе ЧЖ 1–3 года)
     */
    sealed class Mouse(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Pup : Mouse(displayKey = "mouse_pup", ordinal = 0)

        data object Juvenile : Mouse(displayKey = "mouse_juvenile", ordinal = 1)

        data object Adult : Mouse(displayKey = "mouse_adult", ordinal = 2)

        data object Senior : Mouse(displayKey = "mouse_senior", ordinal = 3)

        data object EndOfLife : Mouse(displayKey = "mouse_end_of_life", ordinal = 4)

        companion object {
            fun all(): List<Mouse> = listOf(Pup, Juvenile, Adult, Senior, EndOfLife)
        }
    }

    /**
     * Стадии жизни хорьков (PMC «Senior Ferret» PMC7129291 + Oxbow Ferret Life Stages,
     * см. §4.7 спецификации).
     *
     * Пороги по возрасту (единые — у хорька нет подкатегорий; «senior с 3–4 лет» по §4.7):
     *  - Kit: 0 — ~4 мес. (0–0.33, до отъёма/ювенильной фазы)
     *  - Juvenile: ~4 мес. — 1 год (0.33–1.0)
     *  - Adult: 1 — 3 года (1.0–3.0)
     *  - Senior: 3 — 5 лет
     *  - Geriatric: 5+ лет (приближение к верхней границе ЧЖ 5–10)
     */
    sealed class Ferret(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Kit : Ferret(displayKey = "ferret_kit", ordinal = 0)

        data object Juvenile : Ferret(displayKey = "ferret_juvenile", ordinal = 1)

        data object Adult : Ferret(displayKey = "ferret_adult", ordinal = 2)

        data object Senior : Ferret(displayKey = "ferret_senior", ordinal = 3)

        data object Geriatric : Ferret(displayKey = "ferret_geriatric", ordinal = 4)

        companion object {
            fun all(): List<Ferret> = listOf(Kit, Juvenile, Adult, Senior, Geriatric)
        }
    }

    /**
     * Стадии жизни птиц (AAV «Care for Senior Parrots» + Lafeber Vet, см. §4.8 спецификации).
     *
     * В отличие от млекопитающих, границы стадий птицы задаются **долей от видовой
     * продолжительности жизни** ([BirdType.averageLifespanYears]), а не абсолютным
     * возрастом — у волнистого попугая (ЧЖ 7 лет) и ара (ЧЖ 50 лет) «взрослость»
     * наступает в очень разном календарном возрасте. Пороги (доля от ЧЖ):
     *  - Hatchling: 0–5 %
     *  - Juvenile: 5–20 %
     *  - Adult: 20–70 %
     *  - Senior: 70–90 %
     *  - Geriatric: 90 %+
     */
    sealed class Bird(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Hatchling : Bird(displayKey = "bird_hatchling", ordinal = 0)

        data object Juvenile : Bird(displayKey = "bird_juvenile", ordinal = 1)

        data object Adult : Bird(displayKey = "bird_adult", ordinal = 2)

        data object Senior : Bird(displayKey = "bird_senior", ordinal = 3)

        data object Geriatric : Bird(displayKey = "bird_geriatric", ordinal = 4)

        companion object {
            fun all(): List<Bird> = listOf(Hatchling, Juvenile, Adult, Senior, Geriatric)
        }
    }

    /**
     * Стадии жизни рептилий (PetPlace + Reptile Centre + A-Z Animals, см. §4.9 спецификации).
     *
     * Как и у птиц, границы стадий заданы **долей от видовой ЧЖ** ([ReptileType.averageLifespanYears]),
     * а не абсолютным возрастом — у геккона (ЧЖ 15) и черепахи (ЧЖ 40) «взрослость» наступает
     * в очень разном календарном возрасте. В отличие от млекопитающих — 4 фазы (§4.9):
     *  - Hatchling: 0–5 %
     *  - Juvenile: 5–20 %
     *  - Adult: 20–75 %
     *  - Senior: 75 %+
     */
    sealed class Reptile(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Hatchling : Reptile(displayKey = "reptile_hatchling", ordinal = 0)

        data object Juvenile : Reptile(displayKey = "reptile_juvenile", ordinal = 1)

        data object Adult : Reptile(displayKey = "reptile_adult", ordinal = 2)

        data object Senior : Reptile(displayKey = "reptile_senior", ordinal = 3)

        companion object {
            fun all(): List<Reptile> = listOf(Hatchling, Juvenile, Adult, Senior)
        }
    }

    /**
     * Стадии жизни лошадей (AAEP Vaccination/Senior Horse Care + PetMD, см. §4.10 спецификации).
     *
     * Пороги по возрасту (единые для всех типов — формула AAEP не зависит от породы;
     * «senior с ~15 лет» по AAEP Senior Horse Care):
     *  - Foal: 0–1 год (жеребёнок до отъёма/первого года)
     *  - Yearling: 1–2 года (годовик)
     *  - YoungAdult: 2–4 года (молодая лошадь, период заездки/созревания)
     *  - Adult: 4–15 лет (зрелость, рабочий/племенной возраст)
     *  - Senior: 15+ лет (приближение к верхней границе ЧЖ 25–30)
     */
    sealed class Horse(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Foal : Horse(displayKey = "horse_foal", ordinal = 0)

        data object Yearling : Horse(displayKey = "horse_yearling", ordinal = 1)

        data object YoungAdult : Horse(displayKey = "horse_young_adult", ordinal = 2)

        data object Adult : Horse(displayKey = "horse_adult", ordinal = 3)

        data object Senior : Horse(displayKey = "horse_senior", ordinal = 4)

        companion object {
            fun all(): List<Horse> = listOf(Foal, Yearling, YoungAdult, Adult, Senior)
        }
    }

    /**
     * Стадии жизни рыб (PetMD + Kodama Koi Farm + AquariumStoreDepot, см. §4.11 спецификации).
     *
     * Как и у птиц и рептилий, границы стадий заданы **долей от видовой ЧЖ**
     * ([FishType.averageLifespanYears]), а не абсолютным возрастом — у гуппи (ЧЖ 2) и кои (ЧЖ 30)
     * «взрослость» наступает в очень разном календарном возрасте. В отличие от млекопитающих —
     * 4 фазы (§4.11):
     *  - Fry: 0–5 %
     *  - Juvenile: 5–20 %
     *  - Adult: 20–75 %
     *  - Senior: 75 %+
     */
    sealed class Fish(
        displayKey: String,
        ordinal: Int,
    ) : LifeStage(displayKey, ordinal) {
        data object Fry : Fish(displayKey = "fish_fry", ordinal = 0)

        data object Juvenile : Fish(displayKey = "fish_juvenile", ordinal = 1)

        data object Adult : Fish(displayKey = "fish_adult", ordinal = 2)

        data object Senior : Fish(displayKey = "fish_senior", ordinal = 3)

        companion object {
            fun all(): List<Fish> = listOf(Fry, Juvenile, Adult, Senior)
        }
    }
}
