# Care content tracker

Этот документ отслеживает статус научного наполнения care-рекомендаций
(`app/src/main/assets/care/{species}/{stage}/{ru,en}.json`).

Plan 2 (Task 13) создал **placeholder**-файлы со структурой по модели `CareRecommendation`
(см. `:core:model`) и обязательным дисклеймером §3.3. **Plan 3 (Tasks 2–13) заменил все
placeholder'ы реальным husbandry-контентом** по опубликованным ветеринарным guidelines §14 —
для всех 12 видов × стадий × {ru, en}. Уровень контента — общая гид по стадиям жизни, не
индивидуальная медицина (см. [ADR-0010](adr/0010-care-content-from-published-guidelines.md)).

Отсутствие placeholder'ов проверяется поиском (должен вернуть пусто):

```bash
grep -rl "TODO" app/src/main/assets/care
```

Проверки:

```bash
bash scripts/verify-care-assets.sh    # структура + наличие + дисклеймер (все виды)
bash scripts/verify-care-content.sh   # готовность контента: no-TODO + дисклеймер §3.3 + min-length
```

а также JVM-тесты в `:core:domain`: `CareAssetsIntegrityTest` (структура) и `CareContentQualityTest`
(готовность контента + инвариант `contentComplete == Species.implemented()`).

## Статус (Status)

Легенда: `TODO` — placeholder; `DONE (Plan 3)` — реальный husbandry-контент наполнен по
опубликованным guidelines §14 в обеих локалях (ru + en) и проходит quality-гейт
(`CareContentQualityTest` / `verify-care-content.sh`). **Профессиональная ветеринарная вычитка —
отдельный рекомендованный шаг** (external Post-Completion), см. [ADR-0010](adr/0010-care-content-from-published-guidelines.md).

| Вид | Стадии (LifeStage) | Локали | Источник (§14) | Статус |
|---|---|---|---|---|
| Dog (`dog`) | puppy, young_adult, mature_adult, senior, end_of_life | ru, en | AAHA 2019 Canine Life Stage Guidelines | DONE (Plan 3) |
| Cat (`cat`) | kitten, young_adult, mature_adult, senior, end_of_life | ru, en | AAHA/AAFP 2021 Feline Life Stage Guidelines | DONE (Plan 3) |
| Rabbit (`rabbit`) | infancy, adolescence, young_adult, adult, senior | ru, en | House Rabbit Society & Oxbow Rabbit Life Stages | DONE (Plan 3) |
| Hamster (`hamster`) | pup, juvenile, adult, senior, very_senior | ru, en | RVC VetCompass Hamster Study & PetMD | DONE (Plan 3) |
| Guinea Pig (`guinea_pig`) | pup, juvenile, adult, senior, geriatric | ru, en | Oxbow Guinea Pig Lifespan and Life Stages | DONE (Plan 3) |
| Rat (`rat`) | pup, juvenile, adult, senior, end_of_life | ru, en | Sengupta 2013, Int J Prev Med 4(6):624-630 | DONE (Plan 3) |
| Mouse (`mouse`) | pup, juvenile, adult, senior, end_of_life | ru, en | Dutta & Sengupta 2016, Life Sciences 152:244-248 | DONE (Plan 3) |
| Ferret (`ferret`) | kit, juvenile, adult, senior, geriatric | ru, en | The Senior Ferret (PMC7129291) & Oxbow Ferret Life Stages | DONE (Plan 3) |
| Bird (`bird`) | hatchling, juvenile, adult, senior, geriatric | ru, en | AAV Care for Senior Parrots & Lafeber Vet | DONE (Plan 3) |
| Reptile (`reptile`) | hatchling, juvenile, adult, senior | ru, en | PetPlace, Reptile Centre & A-Z Animals lifespan sheets | DONE (Plan 3) |
| Horse (`horse`) | foal, yearling, young_adult, adult, senior | ru, en | AAEP Senior Horse Care & PetMD (Kaela Schraer DVM) | DONE (Plan 3) |
| Fish (`fish`) | fry, juvenile, adult, senior | ru, en | PetMD How Long Do Fish Live & Kodama Koi Farm | DONE (Plan 3) |

Всего: **58 стадий × 2 локали = 116 файлов** (48 новых из Plan 2 видов × 2 + 20 из Plan 1).

> Примечание: у `fish` поле `dental_care` опущено намеренно (неприменимо к рыбам;
> в модели `CareRecommendation.dentalCare` nullable, `= null`).

## Content-pass checklist (после Plan 2)

Для каждого вида при замене placeholder на реальный контент:

- [ ] вычитать первоисточник §14 для каждой стадии;
- [ ] заполнить `stage_description`, `nutrition`, `activity`,
      `veterinary_check_frequency`, `warning_signs` (и `dental_care`, если применимо)
      на ru и en;
- [ ] сверить `source_url` / `source_name` с актуальной ссылкой;
- [ ] оставить `disclaimer` без изменений (§3.3);
- [ ] обновить статус вида в таблице выше на `DONE`;
- [ ] прогнать `bash scripts/verify-care-assets.sh` и `:core:domain:test`.
