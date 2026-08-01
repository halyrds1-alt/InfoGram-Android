# InfoGram

InfoGram — это кастомный клиент Telegram для Android, основанный на официальном исходном коде Telegram.

## Возможности

- Полная функциональность Telegram
- Кастомное название приложения
- Поддержка всех функций: чаты, каналы, звонки, стикеры, боты
- Основан на официальном исходном коде

## Сборка

### Требования

- Android Studio (последняя версия)
- JDK 17
- Android SDK 35
- NDK 27.2.12479018
- CMake 3.10.2+

### Инструкция

1. Клонируйте репозиторий:
```bash
git clone https://github.com/halyrds1-alt/InfoGram-Android.git
cd InfoGram-Android
```

2. Откройте проект в Android Studio

3. Дождитесь синхронизации Gradle

4. Соберите APK:
```bash
./gradlew :TMessagesProj_AppStandalone:assembleAfatDebug
```

5. APK будет в `TMessagesProj_AppStandalone/build/outputs/apk/afat/debug/`

### Сборка через командную строку

```bash
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot
set ANDROID_HOME=C:\Android\Sdk
gradlew :TMessagesProj_AppStandalone:assembleAfatDebug --no-daemon
```

## Структура проекта

- `TMessagesProj/` — основная библиотека с кодом Telegram
- `TMessagesProj_AppStandalone/` — standalone приложение
- `TMessagesProj_App/` — Google Play версия
- `TMessagesProj_AppHockeyApp/` — бета версия
- `TMessagesProj_AppHuawei/` — Huawei версия

## Изменения

- Переименовано приложение в "InfoGram"
- Изменён package name на `com.infogram.messenger`
- Заменены ссылки на "Telegram" в UI на "InfoGram"
- Папки хранения файлов: InfoGram Images, InfoGram Video и т.д.

## Лицензия

Telegram for Android лицензирован под GNU GPL v. 2 или позднее.
