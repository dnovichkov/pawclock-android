# RuStore — комплект для публикации

Чек-лист подачи PawClock в [RuStore Console](https://console.rustore.ru).
Тексты и графика НЕ дублируются: единый источник — `fastlane/metadata/android/ru/`
(см. таблицу ниже). При обновлении текстов правьте fastlane, а не этот файл.

## Артефакт сборки

RuStore принимает **AAB** (рекомендуется) или APK. Используйте тот же
release-keystore, что и для Google Play (`docs/RELEASE.md` → GitHub Secrets):

```bash
./gradlew :app:bundleRelease   # app/build/outputs/bundle/release/app-release.aab
```

`applicationId` — `app.pawclock` (без `.debug`-суффикса).

## Поля карточки → откуда брать

| Поле RuStore | Лимит | Источник |
|---|---|---|
| Название | 50 симв. | `fastlane/metadata/android/ru/title.txt` (25 симв.) |
| Краткое описание | 80 симв. | `fastlane/metadata/android/ru/short_description.txt` (76 симв.) |
| Полное описание | 4000 симв. | `fastlane/metadata/android/ru/full_description.txt` |
| «Что нового» | 500 симв. | `fastlane/metadata/android/ru/changelogs/<versionCode>.txt` |
| Иконка 512×512 PNG | — | `fastlane/metadata/android/ru/images/icon.png` |
| Скриншоты (мин. 1, лучше 4+) | — | `fastlane/metadata/android/ru/images/phoneScreenshots/` |

Скриншоты сняты с эмулятора 1080×2400 (Medium Phone, API 36); при необходимости
перегенерировать — см. `scripts/generate-store-assets.py` (иконка/feature graphic)
и раздел Screenshots в `fastlane/README.md`.

## Анкеты и обязательные поля

- **Категория**: «Дом и сад» или «Образ жизни» (на усмотрение; приложение —
  утилита для владельцев питомцев).
- **Возрастной рейтинг**: 0+ (нет UGC, рекламы, покупок, сбора данных).
- **Политика конфиденциальности**: обязателен публичный URL — контент готов в
  `docs/PRIVACY.md`, требуется хостинг (GitHub Pages / pawclock.app). Тот же URL
  используется для Google Play Data Safety.
- **Email поддержки**: указать актуальный.
- **Сбор данных**: приложение не собирает и не передаёт данные (INTERNET
  permission удалён на уровне манифеста) — во всех анкетах смело «нет».

## Отличия от Google Play

- Feature graphic (1024×500) RuStore не использует — файл нужен только для Play.
- Обязательного закрытого тестирования (как 12 тестеров / 14 дней в Play) нет —
  можно публиковать сразу после модерации (обычно 1-3 рабочих дня).
- en-US-локаль карточки RuStore не обязательна; при желании тексты лежат в
  `fastlane/metadata/android/en-US/`.
