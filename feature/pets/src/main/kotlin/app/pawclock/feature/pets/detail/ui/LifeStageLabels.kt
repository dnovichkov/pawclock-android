package app.pawclock.feature.pets.detail.ui

import app.pawclock.feature.pets.R
import app.pawclock.model.LifeStage

// Маппинг стадии жизни в строковый ресурс разбит по виду (диспетчер + под-функции),
// чтобы цикломатическая сложность каждой функции оставалась в пределах detekt-порога
// по мере добавления новых видов (Plan 2, Tasks 4–10). Вынесено в отдельный файл,
// чтобы не превышать file-level TooManyFunctions порог в PetDetailScreen.
@androidx.annotation.StringRes
internal fun lifeStageLabelRes(stage: LifeStage): Int =
    when (stage) {
        is LifeStage.Dog -> dogLifeStageLabelRes(stage)
        is LifeStage.Cat -> catLifeStageLabelRes(stage)
        is LifeStage.Rabbit -> rabbitLifeStageLabelRes(stage)
        is LifeStage.Hamster -> hamsterLifeStageLabelRes(stage)
        is LifeStage.GuineaPig -> guineaPigLifeStageLabelRes(stage)
        is LifeStage.Rat -> ratLifeStageLabelRes(stage)
        is LifeStage.Mouse -> mouseLifeStageLabelRes(stage)
        is LifeStage.Ferret -> ferretLifeStageLabelRes(stage)
        is LifeStage.Bird -> birdLifeStageLabelRes(stage)
        is LifeStage.Reptile -> reptileLifeStageLabelRes(stage)
        is LifeStage.Horse -> horseLifeStageLabelRes(stage)
        is LifeStage.Fish -> fishLifeStageLabelRes(stage)
    }

@androidx.annotation.StringRes
private fun dogLifeStageLabelRes(stage: LifeStage.Dog): Int =
    when (stage) {
        LifeStage.Dog.Puppy -> R.string.life_stage_dog_puppy
        LifeStage.Dog.YoungAdult -> R.string.life_stage_dog_young_adult
        LifeStage.Dog.MatureAdult -> R.string.life_stage_dog_mature_adult
        LifeStage.Dog.Senior -> R.string.life_stage_dog_senior
        LifeStage.Dog.EndOfLife -> R.string.life_stage_dog_end_of_life
    }

@androidx.annotation.StringRes
private fun catLifeStageLabelRes(stage: LifeStage.Cat): Int =
    when (stage) {
        LifeStage.Cat.Kitten -> R.string.life_stage_cat_kitten
        LifeStage.Cat.YoungAdult -> R.string.life_stage_cat_young_adult
        LifeStage.Cat.MatureAdult -> R.string.life_stage_cat_mature_adult
        LifeStage.Cat.Senior -> R.string.life_stage_cat_senior
        LifeStage.Cat.EndOfLife -> R.string.life_stage_cat_end_of_life
    }

@androidx.annotation.StringRes
private fun rabbitLifeStageLabelRes(stage: LifeStage.Rabbit): Int =
    when (stage) {
        LifeStage.Rabbit.Infancy -> R.string.life_stage_rabbit_infancy
        LifeStage.Rabbit.Adolescence -> R.string.life_stage_rabbit_adolescence
        LifeStage.Rabbit.YoungAdult -> R.string.life_stage_rabbit_young_adult
        LifeStage.Rabbit.Adult -> R.string.life_stage_rabbit_adult
        LifeStage.Rabbit.Senior -> R.string.life_stage_rabbit_senior
    }

@androidx.annotation.StringRes
private fun hamsterLifeStageLabelRes(stage: LifeStage.Hamster): Int =
    when (stage) {
        LifeStage.Hamster.Pup -> R.string.life_stage_hamster_pup
        LifeStage.Hamster.Juvenile -> R.string.life_stage_hamster_juvenile
        LifeStage.Hamster.Adult -> R.string.life_stage_hamster_adult
        LifeStage.Hamster.Senior -> R.string.life_stage_hamster_senior
        LifeStage.Hamster.VerySenior -> R.string.life_stage_hamster_very_senior
    }

@androidx.annotation.StringRes
private fun guineaPigLifeStageLabelRes(stage: LifeStage.GuineaPig): Int =
    when (stage) {
        LifeStage.GuineaPig.Pup -> R.string.life_stage_guinea_pig_pup
        LifeStage.GuineaPig.Juvenile -> R.string.life_stage_guinea_pig_juvenile
        LifeStage.GuineaPig.Adult -> R.string.life_stage_guinea_pig_adult
        LifeStage.GuineaPig.Senior -> R.string.life_stage_guinea_pig_senior
        LifeStage.GuineaPig.Geriatric -> R.string.life_stage_guinea_pig_geriatric
    }

@androidx.annotation.StringRes
private fun ratLifeStageLabelRes(stage: LifeStage.Rat): Int =
    when (stage) {
        LifeStage.Rat.Pup -> R.string.life_stage_rat_pup
        LifeStage.Rat.Juvenile -> R.string.life_stage_rat_juvenile
        LifeStage.Rat.Adult -> R.string.life_stage_rat_adult
        LifeStage.Rat.Senior -> R.string.life_stage_rat_senior
        LifeStage.Rat.EndOfLife -> R.string.life_stage_rat_end_of_life
    }

@androidx.annotation.StringRes
private fun mouseLifeStageLabelRes(stage: LifeStage.Mouse): Int =
    when (stage) {
        LifeStage.Mouse.Pup -> R.string.life_stage_mouse_pup
        LifeStage.Mouse.Juvenile -> R.string.life_stage_mouse_juvenile
        LifeStage.Mouse.Adult -> R.string.life_stage_mouse_adult
        LifeStage.Mouse.Senior -> R.string.life_stage_mouse_senior
        LifeStage.Mouse.EndOfLife -> R.string.life_stage_mouse_end_of_life
    }

@androidx.annotation.StringRes
private fun ferretLifeStageLabelRes(stage: LifeStage.Ferret): Int =
    when (stage) {
        LifeStage.Ferret.Kit -> R.string.life_stage_ferret_kit
        LifeStage.Ferret.Juvenile -> R.string.life_stage_ferret_juvenile
        LifeStage.Ferret.Adult -> R.string.life_stage_ferret_adult
        LifeStage.Ferret.Senior -> R.string.life_stage_ferret_senior
        LifeStage.Ferret.Geriatric -> R.string.life_stage_ferret_geriatric
    }

@androidx.annotation.StringRes
private fun birdLifeStageLabelRes(stage: LifeStage.Bird): Int =
    when (stage) {
        LifeStage.Bird.Hatchling -> R.string.life_stage_bird_hatchling
        LifeStage.Bird.Juvenile -> R.string.life_stage_bird_juvenile
        LifeStage.Bird.Adult -> R.string.life_stage_bird_adult
        LifeStage.Bird.Senior -> R.string.life_stage_bird_senior
        LifeStage.Bird.Geriatric -> R.string.life_stage_bird_geriatric
    }

@androidx.annotation.StringRes
private fun reptileLifeStageLabelRes(stage: LifeStage.Reptile): Int =
    when (stage) {
        LifeStage.Reptile.Hatchling -> R.string.life_stage_reptile_hatchling
        LifeStage.Reptile.Juvenile -> R.string.life_stage_reptile_juvenile
        LifeStage.Reptile.Adult -> R.string.life_stage_reptile_adult
        LifeStage.Reptile.Senior -> R.string.life_stage_reptile_senior
    }

@androidx.annotation.StringRes
private fun horseLifeStageLabelRes(stage: LifeStage.Horse): Int =
    when (stage) {
        LifeStage.Horse.Foal -> R.string.life_stage_horse_foal
        LifeStage.Horse.Yearling -> R.string.life_stage_horse_yearling
        LifeStage.Horse.YoungAdult -> R.string.life_stage_horse_young_adult
        LifeStage.Horse.Adult -> R.string.life_stage_horse_adult
        LifeStage.Horse.Senior -> R.string.life_stage_horse_senior
    }

@androidx.annotation.StringRes
private fun fishLifeStageLabelRes(stage: LifeStage.Fish): Int =
    when (stage) {
        LifeStage.Fish.Fry -> R.string.life_stage_fish_fry
        LifeStage.Fish.Juvenile -> R.string.life_stage_fish_juvenile
        LifeStage.Fish.Adult -> R.string.life_stage_fish_adult
        LifeStage.Fish.Senior -> R.string.life_stage_fish_senior
    }
