# ProGuard / R8 rules для :app модуля.
#
# В Plan 1 MVP isMinifyEnabled = false, поэтому эти правила не применяются.
# Файл существует, потому что app/build.gradle.kts ссылается на него в proguardFiles(...)
# — без файла парсинг buildTypes падает при enable minify в Plan 2.
#
# При включении минификации в Plan 2 необходимо проверить keep-rules для:
#  - Hilt (генерируемые компоненты)
#  - Room (entities + DAO)
#  - kotlinx.serialization (@Serializable классы — KSP-плагин обычно сам ставит keep)
#  - Compose runtime (не требует доп. правил при использовании AGP 8+)
#
# Расширения добавлять только после валидации на устройстве с -dontshrink/-dontobfuscate
# отключёнными.
