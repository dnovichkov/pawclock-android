package app.pawclock.domain.care

import java.io.InputStream

/**
 * Абстракция чтения файлов из read-only бандла (Android `assets/` или any equivalent).
 *
 * Изолирует [CareRepositoryImpl] от прямой зависимости на `android.content.res.AssetManager`,
 * чтобы вся бизнес-логика fallback locale и десериализации тестировалась pure JVM юнит-тестами.
 *
 * Production-реализация — `AndroidAssetSource` в `:app` — оборачивает `AssetManager.open()`.
 * Контракт:
 *  - возвращает [InputStream] для существующего пути;
 *  - возвращает `null` если файла нет (НЕ бросает `IOException` или `FileNotFoundException`);
 *  - вызывающая сторона ответственна за закрытие потока.
 *
 * `fun interface` — позволяет тестам подменять реализацию лямбдой или коротким
 * factory'ом без объявления отдельного класса.
 */
fun interface AssetSource {
    /**
     * Открывает поток для чтения файла по относительному пути от корня assets.
     *
     * @param path путь от корня assets, например `"care/dog/puppy/ru.json"`
     * @return [InputStream] или `null`, если файл отсутствует
     */
    fun open(path: String): InputStream?
}
