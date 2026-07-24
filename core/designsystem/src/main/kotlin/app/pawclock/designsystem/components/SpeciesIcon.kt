package app.pawclock.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import app.pawclock.core.designsystem.R
import app.pawclock.model.Species

/**
 * Стилизованная векторная иконка вида животного (§5.6).
 *
 * Заменяет emoji-плейсхолдеры 🐶/🐱 Plan 1 на моно-линейные [androidx.compose.material3.Icon]
 * из набора `ic_species_*` (Android Vector Drawables в `:core:designsystem`). Так как иконки
 * монохромные, [Icon] перекрашивает их в [tint] — поэтому одна иконка адаптируется к светлой/тёмной
 * теме и контексту (аватар в карточке, leadingIcon в чипе) без отдельных ассетов.
 *
 * @param species вид животного — маппится в drawable через [speciesIconRes] (exhaustive `when`,
 *   компилятор гарантирует наличие иконки у каждого будущего вида).
 * @param contentDescription описание для TalkBack; `null` если иконка декоративна (рядом есть текст).
 * @param tint цвет заливки; по умолчанию наследуется из `LocalContentColor` окружения.
 */
@Composable
fun SpeciesIcon(
    species: Species,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(speciesIconRes(species)),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

/**
 * Маппинг [Species] → ресурс drawable иконки.
 *
 * Exhaustive `when` по sealed [Species]: при добавлении нового вида компиляция упадёт, пока для него
 * не добавят иконку — это исключает «забытый» fallback на «?» (как было с emoji в Plan 1).
 */
@DrawableRes
fun speciesIconRes(species: Species): Int =
    when (species) {
        Species.Dog -> R.drawable.ic_species_dog
        Species.Cat -> R.drawable.ic_species_cat
        Species.Rabbit -> R.drawable.ic_species_rabbit
        Species.Hamster -> R.drawable.ic_species_hamster
        Species.GuineaPig -> R.drawable.ic_species_guinea_pig
        Species.Rat -> R.drawable.ic_species_rat
        Species.Mouse -> R.drawable.ic_species_mouse
        Species.Ferret -> R.drawable.ic_species_ferret
        Species.Bird -> R.drawable.ic_species_bird
        Species.Reptile -> R.drawable.ic_species_reptile
        Species.Horse -> R.drawable.ic_species_horse
        Species.Fish -> R.drawable.ic_species_fish
    }
