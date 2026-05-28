package app.pawclock.data.care

import android.content.Context
import app.pawclock.domain.care.AssetSource
import java.io.IOException
import java.io.InputStream

/**
 * Android-реализация [AssetSource] поверх `AssetManager`.
 *
 * `AssetManager.open(path)` бросает [IOException] для отсутствующих файлов,
 * что противоречит null-контракту [AssetSource]. Этот адаптер ловит IOException
 * и возвращает null — выравнивая exception-based API Android с nullable-return.
 *
 * Hilt-wiring отложен на Task 17 (когда `:app` получит `@HiltAndroidApp`).
 * До этого момента класс инстанцируется вручную из места применения.
 */
class AndroidAssetSource(
    private val context: Context,
) : AssetSource {
    override fun open(path: String): InputStream? =
        try {
            context.assets.open(path)
        } catch (_: IOException) {
            null
        }
}
