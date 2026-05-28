package app.pawclock.feature.settings.about

/**
 * Источник для AboutScreen — научная публикация или стандарт, на котором базируется
 * формула расчёта возраста в PawClock.
 *
 * Перечислен на основании §14 спецификации; в Plan 1 покрывает Dog/Cat-формулы.
 * Plan 2 расширит список при добавлении новых видов (кролики, хомяки, etc).
 *
 * Жёстко зафиксирован в коде (не загружается из ресурсов) намеренно — список не
 * должен меняться через локализацию, и его состав влияет на доверие пользователя.
 *
 * @property title краткое название (для UI primary text).
 * @property authors авторы / организация (для UI subtitle).
 * @property year год публикации.
 * @property reference DOI (если есть) или URL стандарта.
 */
internal data class AboutSource(
    val title: String,
    val authors: String,
    val year: Int,
    val reference: String,
)

/**
 * Список научных источников, на которых основаны формулы PawClock в Plan 1.
 *
 * Порядок — в порядке появления в формулах приложения:
 *  1. Wang 2020 — эпигенетическая формула для собак (см. ADR-0006).
 *  2. AKC/AAHA 2019 — табличный метод по размеру для собак.
 *  3. AAHA/AAFP 2021 — кошки (фиксированный метод).
 *  4. McMillan 2024 — expected lifespan для собак по размеру.
 */
internal object AboutSources {
    val all: List<AboutSource> =
        listOf(
            AboutSource(
                title = "Quantitative translation of dog-to-human aging by conserved remodeling of the DNA methylome",
                authors = "Wang T. et al., Cell Systems",
                year = 2020,
                reference = "DOI: 10.1016/j.cels.2020.06.006",
            ),
            AboutSource(
                title = "AAHA Canine Life Stage Guidelines",
                authors = "American Animal Hospital Association",
                year = 2019,
                reference = "aaha.org/aaha-guidelines/life-stage-canine-2019",
            ),
            AboutSource(
                title = "AAHA / AAFP Feline Life Stage Guidelines",
                authors = "American Animal Hospital Association & American Association of Feline Practitioners",
                year = 2021,
                reference = "DOI: 10.1177/1098612X21993657",
            ),
            AboutSource(
                title = "Lifespan in companion dogs and the role of body size",
                authors = "McMillan K. M. et al., Scientific Reports",
                year = 2024,
                reference = "Sample n = 584,734",
            ),
        )
}
