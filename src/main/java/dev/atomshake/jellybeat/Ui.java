package dev.atomshake.jellybeat;
import net.minecraft.client.Minecraft;
public final class Ui {
 public static String t(String text){return translate(text,Minecraft.getInstance().options.languageCode);}
 public static String translate(String text,String language){if("ru_ru".equals(language))return text;return switch(text){
case "JellyBeat · желейная дискотека" -> "JellyBeat · jelly dance";
case "ВКЛ" -> "ON";
case "ВЫКЛ" -> "OFF";
case "Лимит блоков" -> "Block limit";
case "Выход: " -> "Output: ";
case "Подключить" -> "Connect";
case "Отключить" -> "Disconnect";
case "Тест 120 BPM" -> "Test 120 BPM";
case "Танцы" -> "Dance";
case "Обновить устройства" -> "Refresh devices";
case "Блоки поверхности" -> "Surface blocks";
case "Мобы / сущности" -> "Mobs / entities";
case "Игроки" -> "Players";
case "Лодки / плоты" -> "Boats / rafts";
case "Чувствительность и желе → по объектам" -> "Sensitivity and jelly → per object";
case "Радиус блоков" -> "Block radius";
case "Готово" -> "Done";
case "Слушает выбранный выход ПК · звук не записывается" -> "Listens to selected PC output · no audio recording";
case "J — меню · K — танцы · громкость меняется в плеере" -> "J — menu · K — dance · volume is set in your player";
case "Устройство воспроизведения Windows" -> "Windows playback device";
case "Обновить" -> "Refresh";
case "Назад" -> "Back";
case "JellyBeat · настройки по объектам" -> "JellyBeat · per-object settings";
case "Сила желе" -> "Jelly strength";
case "Чувствительность" -> "Sensitivity";
case "Красный: желе > 1.5 / чувствительность > 3.0" -> "Red: jelly > 1.5 / sensitivity > 3.0";
case "Блоки" -> "Blocks";
case "По умолчанию Windows" -> "Windows default";
case "Выбери аудиовыход и нажми «Подключить»" -> "Choose an audio output and click Connect";
case "Подключение к аудиовыходу..." -> "Connecting to audio output...";
case "Аудиовыход отключён. Подключись снова." -> "Audio output disconnected. Connect again.";
case "Визуальный тест 120 BPM (без звука)" -> "Visual test 120 BPM (silent)";
case "Захват остановлен" -> "Capture stopped";
case "Устройство недоступно — выбери другое" -> "Device unavailable — choose another";
case "Слушаю: " -> "Listening: ";
case "Аудиоустройства: " -> "Audio devices: ";
case "Захват: " -> "Capture: ";
case "Voice Chat не установлен" -> "Voice Chat not installed";
case "Ошибка (см. журнал)" -> "Error (see log)";
default -> text;};}
}
