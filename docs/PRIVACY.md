# Privacy Policy — PawClock

_Last updated: 2026-07-24 · Applies to: PawClock for Android (`app.pawclock`) ·
Canonical URL: <https://dnovichkov.github.io/pawclock-android/privacy/>_

PawClock is designed to be private by default. It works fully offline and is built so that
your data never leaves your device.

## Summary (Google Play Data Safety)

- **No data collected.** PawClock does not collect any personal or usage data.
- **No data shared.** PawClock does not share any data with third parties.
- **No analytics or tracking.** There is no Firebase, Crashlytics, Google Analytics, AppMetrica,
  advertising SDK, or any other telemetry.
- **No internet access.** The app ships with the `INTERNET` permission removed
  (`tools:node="remove"`), so it cannot make network requests. There is no account and no cloud sync.

## What PawClock stores, and where

All information you enter — pet profiles (name, species, subcategory, birth date, gender, weight,
notes) and app settings (theme, language) — is stored **only in local storage on your device**
(a local Room database and preferences). It is never transmitted anywhere.

## Permissions

PawClock requests **no runtime permissions** for its core functionality. It does not request
Internet, Location, Contacts, Camera, or storage permissions to run.

## Export / Import

Export and Import are performed **only when you explicitly choose them** and use the Android
Storage Access Framework (the system file picker). You pick the exact file and location; the app
reads or writes only the file you select. PawClock does not upload these files anywhere — where the
file goes (local storage, or a cloud provider you choose in the picker) is entirely under your control.

## Children

PawClock does not collect data from anyone, including children.

## Changes to this policy

Any future change that affects data handling will be reflected in this document and in the app's
Google Play Data Safety section.

## Contact

PawClock is open source under the Apache License 2.0. Questions and issues:
<https://github.com/dnovichkov/pawclock-android>.

---

# Политика конфиденциальности — PawClock

_Обновлено: 2026-07-24 · Относится к: PawClock для Android (`app.pawclock`) ·
Канонический URL: <https://dnovichkov.github.io/pawclock-android/privacy/>_

PawClock создан приватным по умолчанию. Приложение работает полностью офлайн и устроено так,
что ваши данные никогда не покидают устройство.

## Кратко (Google Play Data Safety)

- **Данные не собираются** (No data collected). PawClock не собирает никаких персональных данных
  или данных об использовании.
- **Данные не передаются** (No data shared). PawClock не передаёт никакие данные третьим лицам.
- **Нет аналитики и трекинга.** Нет Firebase, Crashlytics, Google Analytics, AppMetrica, рекламных
  SDK и любой другой телеметрии.
- **Нет доступа в интернет.** Разрешение `INTERNET` удалено из манифеста (`tools:node="remove"`),
  поэтому приложение не может выполнять сетевые запросы. Нет аккаунтов и облачной синхронизации.

## Что и где хранит PawClock

Вся вводимая информация — профили питомцев (имя, вид, подкатегория, дата рождения, пол, вес,
заметки) и настройки приложения (тема, язык) — хранится **только в локальном хранилище вашего
устройства** (локальная база Room и настройки). Эти данные никуда не передаются.

## Разрешения

Для основной работы PawClock **не запрашивает никаких разрешений**. Не запрашиваются интернет,
геолокация, контакты, камера или доступ к хранилищу.

## Экспорт и импорт

Экспорт и импорт выполняются **только по вашему явному выбору** через Storage Access Framework
(системный выбор файлов). Вы сами указываете файл и его расположение; приложение читает или пишет
только выбранный вами файл. PawClock никуда не загружает эти файлы — куда попадёт файл (локальное
хранилище или выбранный вами облачный провайдер), полностью решаете вы.

## Дети

PawClock не собирает данные ни у кого, включая детей.

## Изменения политики

Любое будущее изменение, затрагивающее обработку данных, будет отражено в этом документе и в разделе
Google Play Data Safety.

## Контакты

PawClock — открытый исходный код под лицензией Apache 2.0. Вопросы и обращения:
<https://github.com/dnovichkov/pawclock-android>.
