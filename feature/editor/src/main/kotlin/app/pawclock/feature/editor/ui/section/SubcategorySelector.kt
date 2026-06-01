package app.pawclock.feature.editor.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.feature.editor.R
import app.pawclock.feature.editor.SubcategoryOption
import app.pawclock.model.Species

/**
 * Селектор подкатегории (размер/тип в зависимости от вида) — Plan 2, Task 15.
 *
 * Если опций нет (species не выбран или вид без подкатегорий — GuineaPig/Rat/Mouse/Ferret),
 * composable не рендерится на уровне родителя; здесь предполагается options.isNotEmpty().
 *
 * Метка резолвится через `(species, id)`, а не только по `id`: id подкатегорий
 * пересекаются между видами (например, `small`/`medium`/`large`/`giant` есть и у
 * [app.pawclock.model.DogSize], и у [app.pawclock.model.RabbitSize]; `dwarf` —
 * у Rabbit и Hamster), поэтому без species корректную метку выбрать нельзя.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SubcategorySelector(
    species: Species?,
    options: List<SubcategoryOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.pet_editor_subcategory_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            options.forEach { option ->
                val labelRes = subcategoryLabelRes(species, option.id)
                FilterChip(
                    selected = selectedId == option.id,
                    onClick = {
                        // Повторный тап — снять выбор.
                        if (selectedId == option.id) onSelect(null) else onSelect(option.id)
                    },
                    label = {
                        Text(
                            text = if (labelRes != 0) stringResource(labelRes) else option.label,
                        )
                    },
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedId == option.id,
                        ),
                )
            }
        }
    }
}

/**
 * Маппит подкатегорию `(species, id)` на string-resource id. Возвращает `0` для
 * неизвестных комбинаций, чтобы caller мог упасть на `option.label` без NPE.
 *
 * Диспетчеризация по виду + per-species под-функции: id пересекаются между видами,
 * а единый плоский `when (id)` дал бы коллизии и зашкаливающую цикломатическую сложность.
 *
 * `internal` — для [SpeciesLocalizationTest] (Plan 2, Task 22).
 */
@androidx.annotation.StringRes
internal fun subcategoryLabelRes(
    species: Species?,
    id: String,
): Int =
    when (species) {
        Species.Dog -> dogSizeLabelRes(id)
        Species.Cat -> catTypeLabelRes(id)
        Species.Rabbit -> rabbitSizeLabelRes(id)
        Species.Hamster -> hamsterTypeLabelRes(id)
        Species.Bird -> birdTypeLabelRes(id)
        Species.Reptile -> reptileTypeLabelRes(id)
        Species.Horse -> horseTypeLabelRes(id)
        Species.Fish -> fishTypeLabelRes(id)
        else -> 0
    }

@androidx.annotation.StringRes
private fun dogSizeLabelRes(id: String): Int =
    when (id) {
        "toy" -> R.string.dog_size_toy
        "small" -> R.string.dog_size_small
        "medium" -> R.string.dog_size_medium
        "large" -> R.string.dog_size_large
        "giant" -> R.string.dog_size_giant
        else -> 0
    }

@androidx.annotation.StringRes
private fun catTypeLabelRes(id: String): Int =
    when (id) {
        "indoor_short_hair" -> R.string.cat_type_indoor_short_hair
        "indoor_long_hair" -> R.string.cat_type_indoor_long_hair
        "outdoor" -> R.string.cat_type_outdoor
        "large_breed" -> R.string.cat_type_large_breed
        else -> 0
    }

@androidx.annotation.StringRes
private fun rabbitSizeLabelRes(id: String): Int =
    when (id) {
        "dwarf" -> R.string.rabbit_size_dwarf
        "small" -> R.string.rabbit_size_small
        "medium" -> R.string.rabbit_size_medium
        "large" -> R.string.rabbit_size_large
        "giant" -> R.string.rabbit_size_giant
        else -> 0
    }

@androidx.annotation.StringRes
private fun hamsterTypeLabelRes(id: String): Int =
    when (id) {
        "syrian" -> R.string.hamster_type_syrian
        "dwarf" -> R.string.hamster_type_dwarf
        "roborovski" -> R.string.hamster_type_roborovski
        "chinese" -> R.string.hamster_type_chinese
        "winter_white" -> R.string.hamster_type_winter_white
        else -> 0
    }

@androidx.annotation.StringRes
private fun birdTypeLabelRes(id: String): Int =
    when (id) {
        "budgerigar" -> R.string.bird_type_budgerigar
        "cockatiel" -> R.string.bird_type_cockatiel
        "canary" -> R.string.bird_type_canary
        "lovebird" -> R.string.bird_type_lovebird
        "conure" -> R.string.bird_type_conure
        "amazon" -> R.string.bird_type_amazon
        "african_grey" -> R.string.bird_type_african_grey
        "cockatoo" -> R.string.bird_type_cockatoo
        "macaw" -> R.string.bird_type_macaw
        "pigeon" -> R.string.bird_type_pigeon
        else -> 0
    }

@androidx.annotation.StringRes
private fun reptileTypeLabelRes(id: String): Int =
    when (id) {
        "box_turtle" -> R.string.reptile_type_box_turtle
        "red_eared_slider" -> R.string.reptile_type_red_eared_slider
        "bearded_dragon" -> R.string.reptile_type_bearded_dragon
        "ball_python" -> R.string.reptile_type_ball_python
        "corn_snake" -> R.string.reptile_type_corn_snake
        "green_iguana" -> R.string.reptile_type_green_iguana
        "leopard_gecko" -> R.string.reptile_type_leopard_gecko
        "crested_gecko" -> R.string.reptile_type_crested_gecko
        else -> 0
    }

@androidx.annotation.StringRes
private fun horseTypeLabelRes(id: String): Int =
    when (id) {
        "pony" -> R.string.horse_type_pony
        "light_horse" -> R.string.horse_type_light_horse
        "draft_horse" -> R.string.horse_type_draft_horse
        "thoroughbred" -> R.string.horse_type_thoroughbred
        else -> 0
    }

@androidx.annotation.StringRes
private fun fishTypeLabelRes(id: String): Int =
    when (id) {
        "goldfish" -> R.string.fish_type_goldfish
        "koi" -> R.string.fish_type_koi
        "betta" -> R.string.fish_type_betta
        "guppy" -> R.string.fish_type_guppy
        "angelfish" -> R.string.fish_type_angelfish
        "neon_tetra" -> R.string.fish_type_neon_tetra
        "tropical" -> R.string.fish_type_tropical
        "discus" -> R.string.fish_type_discus
        else -> 0
    }

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
