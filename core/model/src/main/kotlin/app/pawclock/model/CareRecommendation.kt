package app.pawclock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Care-рекомендация для конкретной пары (вид × стадия жизни × locale).
 *
 * Сериализуется/десериализуется через `kotlinx.serialization` из файлов
 * `assets/care/{species_id}/{stage_id}/{locale}.json`. Все строковые поля
 * локализованы (отдельный файл на каждую locale).
 *
 * Каждый экран рекомендаций ОБЯЗАН отображать [disclaimer] (§3.3 спецификации):
 *
 * > Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача.
 *
 * Disclaimer хранится прямо в JSON, а не как константа Kotlin, чтобы:
 *  1. Текст локализовался автоматически вместе с остальными полями.
 *  2. При наличии локального юридического ограничения disclaimer был extensible per-locale.
 *
 * @property stageDescription 1–2 абзаца описывающие физиологические особенности стадии (§3.3)
 * @property nutrition рекомендации по питанию (тип корма, частота, объём — без брендов)
 * @property activity активность и обогащение среды (упражнения, игрушки, прогулки)
 * @property veterinaryCheckFrequency рекомендованная частота ветеринарных осмотров
 * @property dentalCare стоматология (или null, если неприменимо для вида/стадии — например, рыбы)
 * @property warningSigns тревожные симптомы, требующие обращения к ветеринару
 * @property sourceUrl URL первоисточника (peer-reviewed publication, ветеринарная организация)
 * @property sourceName читаемое имя источника (например, "AAHA 2019 Canine Life Stage Guidelines")
 * @property disclaimer обязательный disclaimer (§3.3), отображается на каждом экране
 */
@Serializable
data class CareRecommendation(
    @SerialName("stage_description")
    val stageDescription: String,
    @SerialName("nutrition")
    val nutrition: String,
    @SerialName("activity")
    val activity: String,
    @SerialName("veterinary_check_frequency")
    val veterinaryCheckFrequency: String,
    @SerialName("dental_care")
    val dentalCare: String? = null,
    @SerialName("warning_signs")
    val warningSigns: String,
    @SerialName("source_url")
    val sourceUrl: String,
    @SerialName("source_name")
    val sourceName: String,
    @SerialName("disclaimer")
    val disclaimer: String,
)
