package app.pawclock

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Корневой [Application] класс PawClock.
 *
 * `@HiltAndroidApp` инициализирует Hilt-DI-граф приложения и генерирует базовый
 * application-компонент (`PawClockApplication_HiltComponents`). Без этой аннотации
 * Hilt не может предоставлять зависимости в `@AndroidEntryPoint`-аннотированные
 * activities/services и `@HiltViewModel`-аннотированные view models.
 *
 * Регистрация в [AndroidManifest.xml] — `android:name=".PawClockApplication"`
 * на `<application>`.
 *
 * Задачи `onCreate()` будут расширены в более поздних задачах:
 *  - Task 22 (Localization): `AppCompatDelegate.setApplicationLocales(...)`
 *  - Plan 3 (Widgets/Notifications): инициализация WorkManager-плановщика
 *
 * Пока — пустое тело, делегируем всё Hilt-генерируемому суперклассу.
 */
@HiltAndroidApp
class PawClockApplication : Application()
