# Care content tracker

Этот документ отслеживает статус научного наполнения care-рекомендаций
(`app/src/main/assets/care/{species}/{stage}/{ru,en}.json`).

После Plan 2 (Task 13) для всех 12 реализованных видов созданы **placeholder**-файлы
со структурой по модели `CareRecommendation` (см. `:core:model`) и обязательным
дисклеймером §3.3. Реальный научно-обоснованный текст — отдельный content-pass
**после Plan 2** (см. Overview плана `2026-05-28-all-species-and-export-import.md`,
раздел «Что НЕ входит в этот план»).

Все placeholder-поля помечены `TODO(content-pass-after-plan-2)` и выявляются поиском:

```bash
grep -rl "TODO(content-pass-after-plan-2)" app/src/main/assets/care
```

Наличие и структурная валидность всех файлов проверяется:

```bash
bash scripts/verify-care-assets.sh
```

а также JVM-тестом `CareAssetsIntegrityTest` в `:core:domain`
(структура + наличие + дисклеймер для каждого вида × стадии × {ru, en}).

## Статус (Status)

Легенда: `TODO` — placeholder; `DONE` — научный контент вычитан ветеринарным
источником и заменён в обоих локалях (ru + en).

| Вид | Стадии (LifeStage) | Локали | Источник (§14) | Статус |
|---|---|---|---|---|
| Dog (`dog`) | puppy, young_adult, mature_adult, senior, end_of_life | ru, en | AAHA 2019 Canine Life Stage Guidelines | TODO (Plan 1 placeholder) |
| Cat (`cat`) | kitten, young_adult, mature_adult, senior, end_of_life | ru, en | AAHA/AAFP 2021 Feline Life Stage Guidelines | TODO (Plan 1 placeholder) |
| Rabbit (`rabbit`) | infancy, adolescence, young_adult, adult, senior | ru, en | House Rabbit Society & Oxbow Rabbit Life Stages | TODO |
| Hamster (`hamster`) | pup, juvenile, adult, senior, very_senior | ru, en | RVC VetCompass Hamster Study & PetMD | TODO |
| Guinea Pig (`guinea_pig`) | pup, juvenile, adult, senior, geriatric | ru, en | Oxbow Guinea Pig Lifespan and Life Stages | TODO |
| Rat (`rat`) | pup, juvenile, adult, senior, end_of_life | ru, en | Sengupta 2013, Int J Prev Med 4(6):624-630 | TODO |
| Mouse (`mouse`) | pup, juvenile, adult, senior, end_of_life | ru, en | Dutta & Sengupta 2016, Life Sciences 152:244-248 | TODO |
| Ferret (`ferret`) | kit, juvenile, adult, senior, geriatric | ru, en | The Senior Ferret (PMC7129291) & Oxbow Ferret Life Stages | TODO |
| Bird (`bird`) | hatchling, juvenile, adult, senior, geriatric | ru, en | AAV Care for Senior Parrots & Lafeber Vet | TODO |
| Reptile (`reptile`) | hatchling, juvenile, adult, senior | ru, en | PetPlace, Reptile Centre & A-Z Animals lifespan sheets | TODO |
| Horse (`horse`) | foal, yearling, young_adult, adult, senior | ru, en | AAEP Senior Horse Care & PetMD (Kaela Schraer DVM) | TODO |
| Fish (`fish`) | fry, juvenile, adult, senior | ru, en | PetMD How Long Do Fish Live & Kodama Koi Farm | TODO |

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
