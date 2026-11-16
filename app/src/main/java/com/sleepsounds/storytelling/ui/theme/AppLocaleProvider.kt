package com.sleepsounds.storytelling.ui.theme

import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { UserPreferencesRepository.LANGUAGE_EN }
/**
 * Supported in-app languages with English (#1 default), Brazilian Portuguese, Vietnamese,
 * and 10 international languages.
 */
val SUPPORTED_APP_LANGUAGES = listOf(
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_EN,
        flagEmoji = "🇺🇸",
        nativeName = "English",
        englishSubtitle = "Default (United States / UK)",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_PT_BR,
        flagEmoji = "🇧🇷",
        nativeName = "Português (Brasil)",
        englishSubtitle = "Brazilian Portuguese",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_VI,
        flagEmoji = "🇻🇳",
        nativeName = "Tiếng Việt",
        englishSubtitle = "Vietnamese",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_ES,
        flagEmoji = "🇪🇸",
        nativeName = "Español",
        englishSubtitle = "Spanish",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_FR,
        flagEmoji = "🇫🇷",
        nativeName = "Français",
        englishSubtitle = "French",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_DE,
        flagEmoji = "🇩🇪",
        nativeName = "Deutsch",
        englishSubtitle = "German",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_JA,
        flagEmoji = "🇯🇵",
        nativeName = "日本語",
        englishSubtitle = "Japanese",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_KO,
        flagEmoji = "🇰🇷",
        nativeName = "한국어",
        englishSubtitle = "Korean",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_IT,
        flagEmoji = "🇮🇹",
        nativeName = "Italiano",
        englishSubtitle = "Italian",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_RU,
        flagEmoji = "🇷🇺",
        nativeName = "Русский",
        englishSubtitle = "Russian",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_ID,
        flagEmoji = "🇮🇩",
        nativeName = "Bahasa Indonesia",
        englishSubtitle = "Indonesian",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_TR,
        flagEmoji = "🇹🇷",
        nativeName = "Türkçe",
        englishSubtitle = "Turkish",
    ),
    SupportedAppLanguage(
        code = UserPreferencesRepository.LANGUAGE_HI,
        flagEmoji = "🇮🇳",
        nativeName = "हिन्दी",
        englishSubtitle = "Hindi",
    ),
)
private val EXTRA_PHRASE_TRANSLATIONS: Map<String, Map<String, String>> = mapOf(
    "Drift into deep, restful sleep" to mapOf(
        "pt-BR" to "Mergulhe em um sono profundo e tranquilo",
        "vi" to "Chìm vào giấc ngủ sâu và thư thái",
        "es" to "Sumérgete en un sueño profundo y reparador",
        "fr" to "Plongez dans un sommeil profond et paisible",
        "de" to "Gleiten Sie in einen tiefen, erholsamen Schlaf",
        "ja" to "深く心地よい眠りへ",
        "ko" to "깊고 편안한 숙면으로 빠져보세요",
        "it" to "Scivola in un sonno profondo e riposante",
        "ru" to "Погрузитесь в глубокий и спокойный сон",
        "id" to "Tidur lebih nyenyak dan tenang setiap malam",
        "tr" to "Derin ve huzurlu bir uykuya dalın",
        "hi" to "गहरी और शांत नींद में खो जाएं",
    ),
    "Continue" to mapOf(
        "pt-BR" to "Continuar",
        "vi" to "Tiếp tục",
        "es" to "Continuar",
        "fr" to "Continuer",
        "de" to "Weiter",
        "ja" to "次へ",
        "ko" to "계속하기",
        "it" to "Continua",
        "ru" to "Продолжить",
        "id" to "Lanjutkan",
        "tr" to "Devam et",
        "hi" to "जारी रखें",
    ),
    "Close your eyes, and sleep well" to mapOf(
        "pt-BR" to "Feche os olhos e durma bem",
        "vi" to "Nhắm mắt lại và ngủ thật ngon",
        "es" to "Cierra los ojos y duerme bien",
        "fr" to "Fermez les yeux et dormez bien",
        "de" to "Augen zu und tief schlafen",
        "ja" to "目を閉じて、ぐっすり眠ろう",
        "ko" to "눈을 감고 편안하게 숙면하세요",
        "it" to "Chiudi gli occhi e dormi bene",
        "ru" to "Закройте глаза и спите крепко",
        "id" to "Pejamkan mata dan tidur nyenyak",
        "tr" to "Gözlerini kapat ve rahat uyu",
        "hi" to "आँखें बंद करें और गहरी नींद सोएं",
    ),
    "02:30 AM · You Snored & Talked!" to mapOf(
        "pt-BR" to "02:30 · Você Roncou e Falou!",
        "vi" to "02:30 SA · Bạn Đã Ngáy & Nói Mớ!",
        "es" to "02:30 AM · ¡Roncaste y hablaste!",
        "fr" to "02:30 · Vous avez ronflé et parlé !",
        "de" to "02:30 Uhr · Geschnarcht & gesprochen!",
        "ja" to "02:30 AM · いびき＆寝言を検出！",
        "ko" to "02:30 AM · 코골이 및 잠꼬대 감지!",
        "it" to "02:30 · Hai russato e parlato!",
        "ru" to "02:30 · Обнаружен храп и разговоры во сне!",
        "id" to "02:30 · Anda Mendengkur & Mengigau!",
        "tr" to "02:30 · Horladınız ve Uykuda Konuştunuz!",
        "hi" to "02:30 AM · खर्राटे और नींद में बोलना रिकॉर्ड हुआ!",
    ),
    "Automatic nocturnal audio detection & waveform playback" to mapOf(
        "pt-BR" to "Detecção automática de som noturno e reprodução de áudio",
        "vi" to "Tự động phát hiện âm thanh ban đêm & nghe lại sóng âm",
        "es" to "Detección automática de audio nocturno y reproducción de ondas",
        "fr" to "Détection audio nocturne automatique et écoute des enregistrements",
        "de" to "Automatische nächtliche Audioerkennung & Wellenform-Wiedergabe",
        "ja" to "夜間のいびき・寝言を自動検出し、波形付きで再生",
        "ko" to "야간 수면 소리 자동 감지 및 파형 다시 듣기",
        "it" to "Rilevamento audio notturno automatico e riproduzione onde sonore",
        "ru" to "Автоматическая запись ночных звуков и прослушивание аудиоволн",
        "id" to "Deteksi suara malam otomatis & pemutaran gelombang audio",
        "tr" to "Otomatik gece ses algılama ve ses dalgası oynatma",
        "hi" to "रात की ध्वनि की स्वचालित पहचान और ऑडियो वेवफॉर्म प्लेबैक",
    ),
    "Full Sleep Stage Hypnogram" to mapOf(
        "pt-BR" to "Gráfico Completo das Fases do Sono",
        "vi" to "Biểu Đồ Chu Kỳ Giấc Ngủ Chi Tiết",
        "es" to "Hipnograma Completo de Fases del Sueño",
        "fr" to "Hypnogramme Complet des Cycles du Sommeil",
        "de" to "Vollständiges Schlafphasen-Hypnogramm",
        "ja" to "詳細な睡眠ステージ＆サイクル分析",
        "ko" to "상세한 수면 단계 히프노그램 분석",
        "it" to "Ipnogramma Completo delle Fasi del Sonno",
        "ru" to "Полная гипнограмма всех фаз вашего сна",
        "id" to "Grafik Lengkap Siklus & Fase Tidur",
        "tr" to "Tam Uyku Evresi Hipnogram Grafiği",
        "hi" to "नींद के चरणों का संपूर्ण हिप्नोग्राम विश्लेषण",
    ),
    "Deep Sleep · Light Sleep · Awake · Snore Analysis" to mapOf(
        "pt-BR" to "Sono Profundo · Sono Leve · Acordado · Análise de Ronco",
        "vi" to "Ngủ sâu · Ngủ nông · Thức giấc · Phân tích tiếng ngáy",
        "es" to "Sueño Profundo · Sueño Ligero · Despierto · Análisis de Ronquidos",
        "fr" to "Sommeil Profond · Sommeil Léger · Éveillé · Analyse des Ronflements",
        "de" to "Tiefschlaf · Leichtschlaf · Wach · Schnarchanalyse",
        "ja" to "深い睡眠 · 浅い睡眠 · 覚醒 · いびき分析",
        "ko" to "깊은 수면 · 얕은 수면 · 깸 · 코골이 분석",
        "it" to "Sonno Profondo · Sonno Leggero · Sveglio · Analisi Russamento",
        "ru" to "Глубокий сон · Легкий сон · Пробуждение · Анализ храпа",
        "id" to "Tidur Nyenyak · Tidur Ringan · Terjaga · Analisis Dengkur",
        "tr" to "Derin Uyku · Hafif Uyku · Uyanık · Horlama Analizi",
        "hi" to "गहरी नींद · हल्की नींद · जागना · खर्राटे का विश्लेषण",
    ),
    "Offline playback, unlimited custom mixes, sleep stories & smart alarm" to mapOf(
        "pt-BR" to "Modo offline, mixes personalizados ilimitados, histórias e alarme inteligente",
        "vi" to "Nghe offline, trộn âm thanh không giới hạn, truyện ngủ & báo thức thông minh",
        "es" to "Modo sin conexión, mezclas personalizadas, cuentos y alarma inteligente",
        "fr" to "Écoute hors-ligne, mix personnalisés illimités, histoires et réveil intelligent",
        "de" to "Offline-Wiedergabe, unbegrenzte eigene Mixe, Geschichten & Smart-Wecker",
        "ja" to "オフライン再生、無制限のカスタムミックス、睡眠ストーリー＆スマートアラーム",
        "ko" to "오프라인 재생, 무제한 커스텀 믹스, 수면 스토리 및 스마트 알람",
        "it" to "Ascolto offline, mix personalizzati illimitati, storie e sveglia smart",
        "ru" to "Офлайн-режим, безлимитные миксы, истории для сна и умный будильник",
        "id" to "Putar offline, mix kustom tanpa batas, cerita tidur & alarm pintar",
        "tr" to "Çevrimdışı dinleme, sınırsız özel miksler, hikayeler ve akıllı alarm",
        "hi" to "ऑफ़लाइन प्लेबैक, अनलिमिटेड कस्टम मिक्स, स्लीप स्टोरीज़ और स्मार्ट अलार्म",
    ),
    "MONTH" to mapOf(
        "pt-BR" to "MÊS", "vi" to "THÁNG", "es" to "MES", "fr" to "MOIS",
        "de" to "MONAT", "ja" to "ヶ月", "ko" to "개월", "it" to "MESE",
        "ru" to "МЕСЯЦ", "id" to "BULAN", "tr" to "AY", "hi" to "महीना",
    ),
    "MONTHS" to mapOf(
        "pt-BR" to "MESES", "vi" to "THÁNG", "es" to "MESES", "fr" to "MOIS",
        "de" to "MONATE", "ja" to "ヶ月", "ko" to "개월", "it" to "MESI",
        "ru" to "МЕСЯЦЕВ", "id" to "BULAN", "tr" to "AY", "hi" to "महीने",
    ),
    "Most Popular" to mapOf(
        "pt-BR" to "Mais Popular", "vi" to "Phổ biến nhất", "es" to "Más popular", "fr" to "Populaire",
        "de" to "Beliebt", "ja" to "人気No.1", "ko" to "인기 플랜", "it" to "Più scelto",
        "ru" to "Популярный", "id" to "Terpopuler", "tr" to "En Popüler", "hi" to "सबसे लोकप्रिय",
    ),
    "Drift into calm, uninterrupted deep sleep" to mapOf(
        "pt-BR" to "Mergulhe em um sono calmo e profundo",
        "vi" to "Chìm vào giấc ngủ sâu, êm dịu không gián đoạn",
        "es" to "Duerme profundamente sin interrupciones",
        "fr" to "Endormez-vous calmement et profondément",
        "de" to "Sanft in einen ruhigen, tiefen Schlaf gleiten",
        "ja" to "穏やかで途切れない深い眠りへ",
        "ko" to "평온하고 깊은 숙면에 빠져보세요",
        "it" to "Addormentati in un sonno calmo e profondo",
        "ru" to "Спокойный и глубокий сон без пробуждений",
        "id" to "Tidur tenang dan nyenyak sepanjang malam",
        "tr" to "Sakin ve kesintisiz derin bir uykuya dalın",
        "hi" to "शांत और गहरी नींद का आनंद लें",
    ),
    "Sleep Aid" to mapOf(
        "pt-BR" to "Sons", "vi" to "Âm thanh", "es" to "Sonidos", "fr" to "Sons",
        "de" to "Klänge", "ja" to "睡眠音", "ko" to "수면 사운드", "it" to "Suoni",
        "ru" to "Звуки", "id" to "Suara Tidur", "tr" to "Uyku Sesi", "hi" to "स्लीप साउंड",
    ),
    "Dream\nAnalysis" to mapOf(
        "pt-BR" to "Análise de\nSonhos", "vi" to "Giải mã\nGiấc mơ", "es" to "Análisis de\nSueños", "fr" to "Analyse de\nRêves",
        "de" to "Traum-\ndeutung", "ja" to "夢診断", "ko" to "꿈 해몽", "it" to "Analisi\nSogni",
        "ru" to "Толкование\nснов", "id" to "Tafsir\nMimpi", "tr" to "Rüya\nTabiri", "hi" to "सपनों का\nअर्थ",
    ),
    "Breathing" to mapOf(
        "pt-BR" to "Respiração", "vi" to "Nhịp thở", "es" to "Respiración", "fr" to "Respiration",
        "de" to "Atmung", "ja" to "呼吸法", "ko" to "호흡 운동", "it" to "Respiro",
        "ru" to "Дыхание", "id" to "Pernapasan", "tr" to "Nefes", "hi" to "श्वास",
    ),
    "See All" to mapOf(
        "pt-BR" to "Ver tudo", "vi" to "Xem tất cả", "es" to "Ver todo", "fr" to "Tout voir",
        "de" to "Alle", "ja" to "すべて見る", "ko" to "모두 보기", "it" to "Vedi tutti",
        "ru" to "Все", "id" to "Lihat Semua", "tr" to "Tümünü Gör", "hi" to "सभी देखें",
    ),
    "Open" to mapOf(
        "pt-BR" to "Abrir", "vi" to "Mở", "es" to "Abrir", "fr" to "Ouvrir",
        "de" to "Öffnen", "ja" to "開く", "ko" to "열기", "it" to "Apri",
        "ru" to "Открыть", "id" to "Buka", "tr" to "Aç", "hi" to "खोलें",
    ),
    "⭐ Featured" to mapOf(
        "pt-BR" to "⭐ Destaques", "vi" to "⭐ Nổi bật", "es" to "⭐ Destacados", "fr" to "⭐ À la une",
        "de" to "⭐ Empfohlen", "ja" to "⭐ おすすめ", "ko" to "⭐ 추천", "it" to "⭐ In evidenza",
        "ru" to "⭐ Подборки", "id" to "⭐ Unggulan", "tr" to "⭐ Öne Çıkanlar", "hi" to "⭐ विशेष",
    ),
    "Handpicked soundscapes & deep sleep mixes" to mapOf(
        "pt-BR" to "Paisagens sonoras e mixes selecionados para dormir",
        "vi" to "Không gian âm thanh chọn lọc cho giấc ngủ sâu",
        "es" to "Paisajes sonoros y mezclas seleccionadas para dormir profundo",
        "fr" to "Ambiances sonores et mix sélectionnés pour un sommeil profond",
        "de" to "Handverlesene Klangwelten & Tiefschlaf-Mixe",
        "ja" to "厳選された自然音と深い眠りのためのミックス",
        "ko" to "엄선된 자연의 소리와 숙면 믹스",
        "it" to "Paesaggi sonori e mix selezionati per un sonno profondo",
        "ru" to "Избранные звуковые атмосферы и миксы для глубокого сна",
        "id" to "Suasana alam & mix pilihan untuk tidur nyenyak",
        "tr" to "Derin uyku için özenle seçilmiş ses manzaraları ve miksler",
        "hi" to "गहरी नींद के लिए चुनिंदा साउंडस्केप और मिक्स",
    ),
    "💖 Recently Update" to mapOf(
        "pt-BR" to "💖 Atualizados Recentemente", "vi" to "💖 Mới cập nhật", "es" to "💖 Recién Actualizado", "fr" to "💖 Mis à jour récemment",
        "de" to "💖 Kürzlich aktualisiert", "ja" to "💖 最新アップデート", "ko" to "💖 최근 업데이트", "it" to "💖 Aggiornati di recente",
        "ru" to "💖 Недавно обновленные", "id" to "💖 Baru Diperbarui", "tr" to "💖 Son Güncellenenler", "hi" to "💖 हाल ही में अपडेट किया गया",
    ),
    "🌧️ White Noise" to mapOf(
        "pt-BR" to "🌧️ Ruído Branco", "vi" to "🌧️ Tiếng ồn trắng", "es" to "🌧️ Ruido Blanco", "fr" to "🌧️ Bruit Blanc",
        "de" to "🌧️ Weißes Rauschen", "ja" to "🌧️ ホワイトノイズ", "ko" to "🌧️ 백색소음", "it" to "🌧️ Rumore Bianco",
        "ru" to "🌧️ Белый шум", "id" to "🌧️ White Noise", "tr" to "🌧️ Beyaz Gürültü", "hi" to "🌧️ व्हाइट नॉइज़",
    ),
    "🧘 Sleep Meditation" to mapOf(
        "pt-BR" to "🧘 Meditação para Dormir", "vi" to "🧘 Thiền ngủ", "es" to "🧘 Meditación para Dormir", "fr" to "🧘 Méditation du Sommeil",
        "de" to "🧘 Schlafmeditation", "ja" to "🧘 睡眠瞑想", "ko" to "🧘 수면 명상", "it" to "🧘 Meditazione per Dormire",
        "ru" to "🧘 Медитация для сна", "id" to "🧘 Meditasi Tidur", "tr" to "🧘 Uyku Meditasyonu", "hi" to "🧘 नींद ध्यान",
    ),
    "🌲 Soundscape" to mapOf(
        "pt-BR" to "🌲 Paisagem Sonora", "vi" to "🌲 Không gian tự nhiên", "es" to "🌲 Paisaje Sonoro", "fr" to "🌲 Ambiance Nature",
        "de" to "🌲 Klangwelt", "ja" to "🌲 サウンドスケープ", "ko" to "🌲 사운드스케이프", "it" to "🌲 Paesaggio Sonoro",
        "ru" to "🌲 Звуковая атмосфера", "id" to "🌲 Suasana Alam", "tr" to "🌲 Doğa Sesleri", "hi" to "🌲 प्रकृति की ध्वनियाँ",
    ),
    "Relax with the sounds of the great outdoors" to mapOf(
        "pt-BR" to "Relaxe com os sons da natureza ao ar livre",
        "vi" to "Thư giãn với âm thanh thiên nhiên",
        "es" to "Relájate con los sonidos de la naturaleza",
        "fr" to "Détendez-vous avec les sons de la nature",
        "de" to "Entspannen Sie bei den Klängen der Natur",
        "ja" to "大自然の心地よい音でリラックス",
        "ko" to "대자연의 소리와 함께 편안하게 휴식하세요",
        "it" to "Rilassati con i suoni della natura incontaminata",
        "ru" to "Расслабьтесь под живые звуки природы",
        "id" to "Bersantai dengan suara alam terbuka yang menenangkan",
        "tr" to "Doğanın huzur veren sesleriyle rahatlayın",
        "hi" to "प्रकृति की शांत ध्वनियों के साथ विश्राम करें",
    ),
    "🎵 Healing Music" to mapOf(
        "pt-BR" to "🎵 Música Curativa", "vi" to "🎵 Nhạc chữa lành", "es" to "🎵 Música Curativa", "fr" to "🎵 Musique Apaisante",
        "de" to "🎵 Heilende Musik", "ja" to "🎵 ヒーリング音楽", "ko" to "🎵 힐링 음악", "it" to "🎵 Musica Rilassante",
        "ru" to "🎵 Исцеляющая музыка", "id" to "🎵 Musik Penyembuhan", "tr" to "🎵 Şifalı Müzik", "hi" to "🎵 हीलिंग म्यूजिक",
    ),
    "Deep Healing Music for The Body" to mapOf(
        "pt-BR" to "Música de cura profunda para o corpo e a mente",
        "vi" to "Nhạc thư giãn phục hồi năng lượng",
        "es" to "Música de sanación profunda para cuerpo y mente",
        "fr" to "Musique apaisante pour le corps et l'esprit",
        "de" to "Tiefenentspannende Musik für Körper und Geist",
        "ja" to "心と体を癒やすディープヒーリング音楽",
        "ko" to "몸과 마음을 치유하는 깊은 힐링 음악",
        "it" to "Musica rilassante profonda per corpo e mente",
        "ru" to "Глубокая расслабляющая музыка для тела и души",
        "id" to "Musik relaksasi mendalam untuk tubuh dan pikiran",
        "tr" to "Beden ve zihin için derin dinlendirici müzik",
        "hi" to "शरीर और मन के लिए गहरा आरामदायक संगीत",
    ),
    "📖 Sleep Story" to mapOf(
        "pt-BR" to "📖 Histórias para Dormir", "vi" to "📖 Truyện kể ru ngủ", "es" to "📖 Cuentos para Dormir", "fr" to "📖 Histoires pour Dormir",
        "de" to "📖 Schlafgeschichten", "ja" to "📖 スリープストーリー", "ko" to "📖 수면 스토리", "it" to "📖 Storie per Dormire",
        "ru" to "📖 Истории на ночь", "id" to "📖 Cerita Pengantar Tidur", "tr" to "📖 Uyku Hikayeleri", "hi" to "📖 नींद की कहानियाँ",
    ),
    "Say goodbye to sleepless nights with sleep stories" to mapOf(
        "pt-BR" to "Diga adeus às noites em claro com histórias relaxantes",
        "vi" to "Tạm biệt mất ngủ với những câu chuyện êm dịu",
        "es" to "Di adiós al insomnio con cuentos relajantes para dormir",
        "fr" to "Dites adieu aux nuits blanches avec nos histoires apaisantes",
        "de" to "Verabschieden Sie sich von schlaflosen Nächten mit Schlafgeschichten",
        "ja" to "眠れない夜にさよならを告げるベッドタイムストーリー",
        "ko" to "편안한 수면 스토리로 불면증 없는 밤을 만나보세요",
        "it" to "Dì addio alle notti insonni con le storie della buonanotte",
        "ru" to "Забудьте о бессоннице с успокаивающими историями на ночь",
        "id" to "Ucapkan selamat tinggal pada malam sulit tidur dengan cerita pengantar tidur",
        "tr" to "Uyku hikayeleriyle uykusuz gecelere veda edin",
        "hi" to "नींद की कहानियों के साथ अनिद्रा को अलविदा कहें",
    ),
    "Featured Soundscape" to mapOf(
        "pt-BR" to "Ambiente em Destaque", "vi" to "Không Gian Nổi Bật", "es" to "Ambiente Destacado", "fr" to "Ambiance à la Une",
        "de" to "Empfohlene Klangwelt", "ja" to "注目のサウンドスケープ", "ko" to "추천 사운드스케이프", "it" to "Ambiente in Evidenza",
        "ru" to "Избранная атмосфера", "id" to "Suasana Unggulan", "tr" to "Öne Çıkan Atmosfer", "hi" to "विशेष साउंडस्केप",
    ),
    "Drift off with soothing rain, ocean & forest mixes" to mapOf(
        "pt-BR" to "Relaxe com mixes suaves de chuva, oceano e floresta",
        "vi" to "Chìm vào giấc ngủ với tiếng mưa, sóng biển & rừng đêm",
        "es" to "Duerme con suaves mezclas de lluvia, océano y bosque",
        "fr" to "Endormez-vous avec de doux mix de pluie, d'océan et de forêt",
        "de" to "Schlafen Sie ein mit sanften Regen-, Ozean- und Waldmixen",
        "ja" to "心地よい雨音、波の音、森のミックスで深い眠りへ",
        "ko" to "잔잔한 빗소리, 파도 소리, 숲속 사운드 믹스와 함께 잠드세요",
        "it" to "Addormentati con dolci mix di pioggia, oceano e foresta",
        "ru" to "Засыпайте под нежные звуки дождя, океана и ночного леса",
        "id" to "Tertidur lelap dengan perpaduan suara hujan, ombak & hutan",
        "tr" to "Dinlendirici yağmur, okyanus ve orman sesleriyle uykuya dalın",
        "hi" to "शांत बारिश, समुद्र की लहरों और जंगल की ध्वनियों के साथ सो जाएं",
    ),
    "Featured Reader" to mapOf(
        "pt-BR" to "Histórias em Destaque", "vi" to "Truyện Ngủ Chọn Lọc", "es" to "Cuentos Destacados", "fr" to "Histoires à la Une",
        "de" to "Empfohlene Geschichten", "ja" to "注目のスリープストーリー", "ko" to "추천 수면 스토리", "it" to "Storie in Evidenza",
        "ru" to "Избранные истории", "id" to "Cerita Pilihan", "tr" to "Seçkin Hikayeler", "hi" to "चुनिंदा कहानियाँ",
    ),
    "Calm bedtime stories & guided sleep meditations" to mapOf(
        "pt-BR" to "Histórias calmas para dormir e meditações guiadas",
        "vi" to "Truyện kể êm dịu & thiền dẫn giấc ngủ sâu",
        "es" to "Cuentos tranquilos para dormir y meditaciones guiadas",
        "fr" to "Histoires calmes pour dormir et méditations guidées",
        "de" to "Ruhige Gute-Nacht-Geschichten & geführte Schlafmeditationen",
        "ja" to "穏やかなベッドタイムストーリーとガイド付き睡眠瞑想",
        "ko" to "차분한 베드타임 스토리와 가이드 수면 명상",
        "it" to "Storie rilassanti della buonanotte e meditazioni guidate",
        "ru" to "Спокойные истории на ночь и медитации для глубокого сна",
        "id" to "Cerita pengantar tidur yang tenang & meditasi tidur terpandu",
        "tr" to "Sakin uyku hikayeleri ve rehberli uyku meditasyonları",
        "hi" to "शांत नींद की कहानियाँ और निर्देशित नींद ध्यान",
    ),
    "Search dream symbol (e.g. Fire, Falling, Flying)..." to mapOf(
        "pt-BR" to "Buscar símbolo do sonho (ex.: Fogo, Cair, Voar)...",
        "vi" to "Tìm kiếm giấc mơ (VD: Ngọn lửa, Rụng răng, Bay lượn)...",
        "es" to "Buscar símbolo del sueño (ej.: Fuego, Caer, Volar)...",
        "fr" to "Rechercher un rêve (ex. : Feu, Tomber, Voler)...",
        "de" to "Traumsymbol suchen (z. B. Feuer, Fallen, Fliegen)...",
        "ja" to "夢のキーワードを検索（例：火、落ちる、空を飛ぶ）...",
        "ko" to "꿈 키워드 검색 (예: 불, 떨어지는 꿈, 하늘을 나는 꿈)...",
        "it" to "Cerca simbolo del sogno (es. Fuoco, Cadere, Volare)...",
        "ru" to "Поиск символа сна (например: Огонь, Падение, Полет)...",
        "id" to "Cari simbol mimpi (mis. Api, Jatuh, Terbang)...",
        "tr" to "Rüya sembolü ara (ör. Ateş, Düşmek, Uçmak)...",
        "hi" to "सपने का प्रतीक खोजें (जैसे: आग, गिरना, उड़ना)...",
    ),
    "Dream Meaning" to mapOf(
        "pt-BR" to "Significado do Sonho", "vi" to "Ý nghĩa giấc mơ", "es" to "Significado del Sueño", "fr" to "Signification du Rêve",
        "de" to "Traumbedeutung", "ja" to "夢の意味", "ko" to "꿈의 의미", "it" to "Significato del Sogno",
        "ru" to "Значение сна", "id" to "Arti Mimpi", "tr" to "Rüyanın Anlamı", "hi" to "सपने का अर्थ",
    ),
    "Psychological Insight" to mapOf(
        "pt-BR" to "Visão Psicológica", "vi" to "Góc nhìn tâm lý học", "es" to "Perspectiva Psicológica", "fr" to "Analyse Psychologique",
        "de" to "Psychologischer Einblick", "ja" to "心理学的インサイト", "ko" to "심리학적 통찰", "it" to "Approfondimento Psicologico",
        "ru" to "Психологический смысл", "id" to "Wawasan Psikologis", "tr" to "Psikolojik Analiz", "hi" to "मनोवैज्ञानिक दृष्टिकोण",
    ),
    "Bedtime Reflection" to mapOf(
        "pt-BR" to "Reflexão antes de Dormir", "vi" to "Lời khuyên trước khi ngủ", "es" to "Reflexión antes de Dormir", "fr" to "Conseil avant de Dormir",
        "de" to "Abendgedanke zum Einschlafen", "ja" to "おやすみ前のアドバイス", "ko" to "취침 전 성찰 및 팁", "it" to "Riflessione della Buonanotte",
        "ru" to "Совет перед сном", "id" to "Refleksi Sebelum Tidur", "tr" to "Uyku Öncesi Tavsiye", "hi" to "सोने से पहले का विचार",
    ),
    "Done Reading" to mapOf(
        "pt-BR" to "Concluir Leitura", "vi" to "Đã hiểu", "es" to "Entendido", "fr" to "Terminé",
        "de" to "Verstanden", "ja" to "閉じる", "ko" to "확인", "it" to "Ho capito",
        "ru" to "Понятно", "id" to "Selesai Membaca", "tr" to "Anladım", "hi" to "समझ गया",
    ),
    "Sleep Tracker" to mapOf(
        "pt-BR" to "Monitor de Sono", "vi" to "Theo dõi giấc ngủ", "es" to "Monitor de Sueño", "fr" to "Suivi du Sommeil",
        "de" to "Schlaftracker", "ja" to "睡眠トラッカー", "ko" to "수면 추적기", "it" to "Monitor del Sonno",
        "ru" to "Трекер сна", "id" to "Pelacak Tidur", "tr" to "Uyku Takipçisi", "hi" to "स्लीप ट्रैकर",
    ),
    "Ready to analyze your sleep cycles" to mapOf(
        "pt-BR" to "Pronto para analisar seus ciclos de sono",
        "vi" to "Sẵn sàng phân tích chu kỳ giấc ngủ của bạn",
        "es" to "Listo para analizar tus ciclos de sueño",
        "fr" to "Prêt à analyser vos cycles de sommeil",
        "de" to "Bereit, deine Schlafzyklen zu analysieren",
        "ja" to "睡眠サイクルの分析準備ができました",
        "ko" to "수면 주기 분석 준비 완료",
        "it" to "Pronto ad analizzare i tuoi cicli del sonno",
        "ru" to "Готов к анализу ваших фаз сна",
        "id" to "Siap menganalisis siklus tidur Anda",
        "tr" to "Uyku döngülerinizi analiz etmeye hazır",
        "hi" to "आपके नींद चक्र का विश्लेषण करने के लिए तैयार",
    ),
    "Tracking sleep phases & ambient sound…" to mapOf(
        "pt-BR" to "Monitorando fases do sono e som ambiente…",
        "vi" to "Đang theo dõi chu kỳ ngủ & âm thanh…",
        "es" to "Monitoreando fases del sueño y sonido ambiente…",
        "fr" to "Suivi des cycles du sommeil et des sons…",
        "de" to "Schlafphasen & Umgebungsgeräusche werden erfasst…",
        "ja" to "睡眠ステージと周囲の音を計測中…",
        "ko" to "수면 단계 및 주변 소리 측정 중…",
        "it" to "Monitoraggio fasi del sonno e suoni ambientali…",
        "ru" to "Отслеживание фаз сна и фоновых звуков…",
        "id" to "Melacak fase tidur & suara sekitar…",
        "tr" to "Uyku evreleri ve ortam sesi takip ediliyor…",
        "hi" to "नींद के चरणों और आसपास की आवाज़ का विश्लेषण हो रहा है…",
    ),
    "Sleep Journal" to mapOf(
        "pt-BR" to "Diário de Sono", "vi" to "Nhật ký giấc ngủ", "es" to "Diario de Sueño", "fr" to "Journal de Sommeil",
        "de" to "Schlaftagebuch", "ja" to "睡眠日誌", "ko" to "수면 일지", "it" to "Diario del Sonno",
        "ru" to "Дневник сна", "id" to "Jurnal Tidur", "tr" to "Uyku Günlüğü", "hi" to "स्लीप जर्नल",
    ),
    "Sample Report" to mapOf(
        "pt-BR" to "Relatório Exemplo", "vi" to "Báo cáo mẫu", "es" to "Informe de Muestra", "fr" to "Rapport Exemple",
        "de" to "Beispielbericht", "ja" to "サンプルレポート", "ko" to "샘플 리포트", "it" to "Report di Esempio",
        "ru" to "Пример отчета", "id" to "Contoh Laporan", "tr" to "Örnek Rapor", "hi" to "नमूना रिपोर्ट",
    ),
    "This is demo report data" to mapOf(
        "pt-BR" to "Estes são dados de demonstração",
        "vi" to "Đây là dữ liệu báo cáo mẫu",
        "es" to "Estos son datos de informe de muestra",
        "fr" to "Ceci est un rapport de démonstration",
        "de" to "Dies sind Beispiel-Berichtsdaten",
        "ja" to "これはサンプルレポートのデータです",
        "ko" to "이 데이터는 샘플 리포트입니다",
        "it" to "Questi sono dati dimostrativi",
        "ru" to "Это демонстрационные данные отчета",
        "id" to "Ini adalah data laporan contoh",
        "tr" to "Bu örnek bir rapor verisidir",
        "hi" to "यह डेमो रिपोर्ट डेटा है",
    ),
    "Start the Sleep Tracker tonight to generate your personal sleep stages & score." to mapOf(
        "pt-BR" to "Inicie o Monitor de Sono hoje à noite para gerar suas fases e pontuação de sono.",
        "vi" to "Hãy bật Theo dõi giấc ngủ tối nay để tạo biểu đồ và điểm số giấc ngủ của riêng bạn.",
        "es" to "Inicia el Monitor de Sueño esta noche para generar tus fases y puntuación personal.",
        "fr" to "Lancez le suivi du sommeil ce soir pour générer vos propres phases et votre score.",
        "de" to "Starten Sie heute Nacht den Schlaftracker für Ihre persönliche Schlafanalyse.",
        "ja" to "今夜睡眠トラッカーを開始して、あなた専用の睡眠スコアとグラフを作成しましょう。",
        "ko" to "오늘 밤 수면 추적기를 켜고 나만의 수면 단계와 점수를 확인해 보세요.",
        "it" to "Avvia il Monitor del Sonno stanotte per generare le tue fasi e il tuo punteggio.",
        "ru" to "Запустите трекер сна сегодня ночью, чтобы получить персональный график и оценку сна.",
        "id" to "Mulai Pelacak Tidur malam ini untuk melihat grafik fase & skor tidur pribadi Anda.",
        "tr" to "Kişisel uyku evrelerinizi ve puanınızı görmek için bu gece Uyku Takipçisini başlatın.",
        "hi" to "अपने व्यक्तिगत नींद चरण और स्कोर जानने के लिए आज रात स्लीप ट्रैकर शुरू करें।",
    ),
    "My Sleep Profile" to mapOf(
        "pt-BR" to "Meu Perfil de Sono", "vi" to "Hồ sơ giấc ngủ của tôi", "es" to "Mi Perfil de Sueño", "fr" to "Mon Profil Sommeil",
        "de" to "Mein Schlafprofil", "ja" to "マイ睡眠プロフィール", "ko" to "내 수면 프로필", "it" to "Il Mio Profilo Sonno",
        "ru" to "Мой профиль сна", "id" to "Profil Tidur Saya", "tr" to "Uyku Profilim", "hi" to "मेरी स्लीप प्रोफ़ाइल",
    ),
    "All" to mapOf(
        "pt-BR" to "Todos", "vi" to "Tất cả", "es" to "Todos", "fr" to "Tous",
        "de" to "Alle", "ja" to "すべて", "ko" to "전체", "it" to "Tutti",
        "ru" to "Все", "id" to "Semua", "tr" to "Tümü", "hi" to "सभी",
    ),
    "Mixes" to mapOf(
        "pt-BR" to "Mixes", "vi" to "Bản phối", "es" to "Mezclas", "fr" to "Mix",
        "de" to "Mixe", "ja" to "ミックス", "ko" to "믹스", "it" to "Mix",
        "ru" to "Миксы", "id" to "Mix", "tr" to "Miksler", "hi" to "मिक्स",
    ),
    "Rain" to mapOf(
        "pt-BR" to "Chuva", "vi" to "Tiếng mưa", "es" to "Lluvia", "fr" to "Pluie",
        "de" to "Regen", "ja" to "雨音", "ko" to "빗소리", "it" to "Pioggia",
        "ru" to "Дождь", "id" to "Hujan", "tr" to "Yağmur", "hi" to "बारिश",
    ),
    "ASMR" to mapOf(
        "pt-BR" to "ASMR", "vi" to "ASMR", "es" to "ASMR", "fr" to "ASMR",
        "de" to "ASMR", "ja" to "ASMR", "ko" to "ASMR", "it" to "ASMR",
        "ru" to "ASMR", "id" to "ASMR", "tr" to "ASMR", "hi" to "ASMR",
    ),
    "Nature" to mapOf(
        "pt-BR" to "Natureza", "vi" to "Thiên nhiên", "es" to "Naturaleza", "fr" to "Nature",
        "de" to "Natur", "ja" to "自然", "ko" to "자연", "it" to "Natura",
        "ru" to "Природа", "id" to "Alam", "tr" to "Doğa", "hi" to "प्रकृति",
    ),
    "Animal" to mapOf(
        "pt-BR" to "Animais", "vi" to "Động vật", "es" to "Animales", "fr" to "Animaux",
        "de" to "Tiere", "ja" to "動物", "ko" to "동물", "it" to "Animali",
        "ru" to "Животные", "id" to "Hewan", "tr" to "Hayvanlar", "hi" to "पशु-पक्षी",
    ),
    "City" to mapOf(
        "pt-BR" to "Cidade", "vi" to "Thành phố", "es" to "Ciudad", "fr" to "Ville",
        "de" to "Stadt", "ja" to "都市", "ko" to "도시", "it" to "Città",
        "ru" to "Город", "id" to "Kota", "tr" to "Şehir", "hi" to "शहर",
    ),
    "Fan" to mapOf(
        "pt-BR" to "Ventilador", "vi" to "Tiếng quạt", "es" to "Ventilador", "fr" to "Ventilateur",
        "de" to "Ventilator", "ja" to "扇風機", "ko" to "선풍기", "it" to "Ventilatore",
        "ru" to "Вентилятор", "id" to "Kipas", "tr" to "Vantilatör", "hi" to "पंखा",
    ),
    "Melody" to mapOf(
        "pt-BR" to "Melodia", "vi" to "Giai điệu", "es" to "Melodía", "fr" to "Mélodie",
        "de" to "Melodie", "ja" to "メロディ", "ko" to "멜로디", "it" to "Melodia",
        "ru" to "Мелодия", "id" to "Melodi", "tr" to "Melodi", "hi" to "धुन",
    ),
    "Baby" to mapOf(
        "pt-BR" to "Bebê", "vi" to "Ru bé ngủ", "es" to "Bebé", "fr" to "Bébé",
        "de" to "Baby", "ja" to "ベビー", "ko" to "아기", "it" to "Bambino",
        "ru" to "Малыш", "id" to "Bayi", "tr" to "Bebek", "hi" to "शिशु",
    ),
    "Noise" to mapOf(
        "pt-BR" to "Ruído", "vi" to "Tiếng ồn", "es" to "Ruido", "fr" to "Bruit",
        "de" to "Rauschen", "ja" to "ノイズ", "ko" to "노이즈", "it" to "Rumore",
        "ru" to "Шум", "id" to "Derau", "tr" to "Gürültü", "hi" to "नॉइज़",
    ),
    "Special" to mapOf(
        "pt-BR" to "Especial", "vi" to "Đặc biệt", "es" to "Especial", "fr" to "Spécial",
        "de" to "Spezial", "ja" to "特別", "ko" to "스페셜", "it" to "Speciale",
        "ru" to "Особые", "id" to "Spesial", "tr" to "Özel", "hi" to "विशेष",
    ),
    "Healing Music" to mapOf(
        "pt-BR" to "Música Curativa", "vi" to "Nhạc chữa lành", "es" to "Música Curativa", "fr" to "Musique Apaisante",
        "de" to "Heilende Musik", "ja" to "ヒーリング音楽", "ko" to "힐링 음악", "it" to "Musica Rilassante",
        "ru" to "Исцеляющая музыка", "id" to "Musik Penyembuhan", "tr" to "Şifalı Müzik", "hi" to "हीलिंग म्यूजिक",
    ),
    "Nature Melodies" to mapOf(
        "pt-BR" to "Melodias da Natureza", "vi" to "Giai điệu thiên nhiên", "es" to "Melodías Naturales", "fr" to "Mélodies Nature",
        "de" to "Naturmelodien", "ja" to "自然のメロディ", "ko" to "자연의 멜로디", "it" to "Melodie della Natura",
        "ru" to "Мелодии природы", "id" to "Melodi Alam", "tr" to "Doğa Melodileri", "hi" to "प्रकृति की धुनें",
    ),
    "Lullaby" to mapOf(
        "pt-BR" to "Canção de Ninar", "vi" to "Nhạc hát ru", "es" to "Canción de Cuna", "fr" to "Berceuse",
        "de" to "Schlaflied", "ja" to "子守唄", "ko" to "자장가", "it" to "Ninna Nanna",
        "ru" to "Колыбельная", "id" to "Lagu Pengantar Tidur", "tr" to "Ninni", "hi" to "लोरी",
    ),
    "Binaural Beats" to mapOf(
        "pt-BR" to "Ondas Binaurais", "vi" to "Sóng não Binaural", "es" to "Ondas Binaurales", "fr" to "Ondes Binaurales",
        "de" to "Binaurale Beats", "ja" to "バイノーラル", "ko" to "바이노럴 비트", "it" to "Toni Binaurali",
        "ru" to "Бинауральные ритмы", "id" to "Gelombang Binaural", "tr" to "Binaural Frekans", "hi" to "बाइनॉरल बीट्स",
    ),
    "Meditation Music" to mapOf(
        "pt-BR" to "Música de Meditação", "vi" to "Nhạc thiền", "es" to "Música de Meditación", "fr" to "Musique Méditation",
        "de" to "Meditationsmusik", "ja" to "瞑想音楽", "ko" to "명상 음악", "it" to "Musica da Meditazione",
        "ru" to "Музыка для медитации", "id" to "Musik Meditasi", "tr" to "Meditasyon Müziği", "hi" to "ध्यान संगीत",
    ),
    "Caffeine" to mapOf(
        "pt-BR" to "Cafeína", "vi" to "Cà phê / Trà", "es" to "Cafeína", "fr" to "Caféine",
        "de" to "Koffein", "ja" to "カフェイン", "ko" to "카페인", "it" to "Caffeina",
        "ru" to "Кофеин", "id" to "Kafein", "tr" to "Kafein", "hi" to "कैफीन",
    ),
    "Workout" to mapOf(
        "pt-BR" to "Exercício", "vi" to "Tập thể dục", "es" to "Ejercicio", "fr" to "Sport",
        "de" to "Training", "ja" to "運動", "ko" to "운동", "it" to "Allenamento",
        "ru" to "Тренировка", "id" to "Olahraga", "tr" to "Egzersiz", "hi" to "व्यायाम",
    ),
    "Late Meal" to mapOf(
        "pt-BR" to "Refeição Tardia", "vi" to "Ăn khuya", "es" to "Cena Tardía", "fr" to "Repas Tardif",
        "de" to "Spätes Essen", "ja" to "夜食", "ko" to "야식", "it" to "Cena Tardi",
        "ru" to "Поздний ужин", "id" to "Makan Malam Larut", "tr" to "Geç Yemek", "hi" to "देर रात भोजन",
    ),
    "Screen Time" to mapOf(
        "pt-BR" to "Uso de Tela", "vi" to "Dùng điện thoại", "es" to "Uso de Pantalla", "fr" to "Écrans",
        "de" to "Bildschirmzeit", "ja" to "スマホ・画面", "ko" to "스마트폰 사용", "it" to "Schermi",
        "ru" to "Экран перед сном", "id" to "Layar Ponsel", "tr" to "Ekran Süresi", "hi" to "स्क्रीन टाइम",
    ),
    "Stressful Day" to mapOf(
        "pt-BR" to "Dia Estressante", "vi" to "Ngày căng thẳng", "es" to "Día Estresante", "fr" to "Journée Stressante",
        "de" to "Stressiger Tag", "ja" to "ストレス", "ko" to "스트레스", "it" to "Giornata Stressante",
        "ru" to "Стрессовый день", "id" to "Hari Penuh Stres", "tr" to "Stresli Gün", "hi" to "तनावपूर्ण दिन",
    ),
    "Reading" to mapOf(
        "pt-BR" to "Leitura", "vi" to "Đọc sách", "es" to "Lectura", "fr" to "Lecture",
        "de" to "Lesen", "ja" to "読書", "ko" to "독서", "it" to "Lettura",
        "ru" to "Чтение", "id" to "Membaca", "tr" to "Kitap Okuma", "hi" to "पढ़ना",
    ),
    "Meditation" to mapOf(
        "pt-BR" to "Meditação", "vi" to "Thiền định", "es" to "Meditación", "fr" to "Méditation",
        "de" to "Meditation", "ja" to "瞑想", "ko" to "명상", "it" to "Meditazione",
        "ru" to "Медитация", "id" to "Meditasi", "tr" to "Meditasyon", "hi" to "ध्यान",
    ),
    "Warm Shower" to mapOf(
        "pt-BR" to "Banho Morno", "vi" to "Tắm nước ấm", "es" to "Ducha Tibia", "fr" to "Douche Chaude",
        "de" to "Warme Dusche", "ja" to "入浴・シャワー", "ko" to "따뜻한 샤워", "it" to "Doccia Calda",
        "ru" to "Теплый душ", "id" to "Mandi Air Hangat", "tr" to "Ilık Duş", "hi" to "गुनगुना स्नान",
    ),
    "Refreshed" to mapOf(
        "pt-BR" to "Revigorado", "vi" to "Sảng khoái", "es" to "Renovado", "fr" to "En pleine forme",
        "de" to "Erfrischt", "ja" to "すっきり", "ko" to "상쾌함", "it" to "Rigenerato",
        "ru" to "Бодрый", "id" to "Segar", "tr" to "Dinç", "hi" to "तरोताज़ा",
    ),
    "Good" to mapOf(
        "pt-BR" to "Bem", "vi" to "Tốt", "es" to "Bien", "fr" to "Bien",
        "de" to "Gut", "ja" to "良い", "ko" to "좋음", "it" to "Bene",
        "ru" to "Хорошо", "id" to "Baik", "tr" to "İyi", "hi" to "अच्छा",
    ),
    "Okay" to mapOf(
        "pt-BR" to "Normal", "vi" to "Bình thường", "es" to "Normal", "fr" to "Correct",
        "de" to "Okay", "ja" to "普通", "ko" to "보통", "it" to "Normale",
        "ru" to "Нормально", "id" to "Biasa", "tr" to "Normal", "hi" to "ठीक",
    ),
    "Tired" to mapOf(
        "pt-BR" to "Cansado", "vi" to "Hơi mệt", "es" to "Cansado", "fr" to "Fatigué",
        "de" to "Müde", "ja" to "眠い", "ko" to "피곤함", "it" to "Stanco",
        "ru" to "Устал", "id" to "Lelah", "tr" to "Yorgun", "hi" to "थका हुआ",
    ),
    "Ready for a good night's sleep?\nPick up right where you left off." to mapOf(
        "pt-BR" to "Pronto para uma boa noite de sono?\nContinue exatamente de onde parou.",
        "vi" to "Sẵn sàng cho một giấc ngủ ngon?\nTiếp tục ngay nơi bạn đã dừng lại.",
        "es" to "¿Listo para dormir bien esta noche?\nContinúa justo donde lo dejaste.",
        "fr" to "Prêt pour une bonne nuit de sommeil ?\nReprenez là où vous vous êtes arrêté.",
        "de" to "Bereit für eine erholsame Nacht?\nMachen Sie genau dort weiter, wo Sie aufgehört haben.",
        "ja" to "ぐっすり眠る準備はできましたか？\n前回の続きからすぐに始められます。",
        "ko" to "편안한 숙면을 취할 준비가 되셨나요?\n마지막으로 듣던 곳에서 바로 이어서 시작하세요.",
        "it" to "Pronto per una buona notte di sonno?\nRiprendi esattamente da dove avevi lasciato.",
        "ru" to "Готовы к крепкому сну?\nПродолжите с того места, где остановились.",
        "id" to "Siap untuk tidur nyenyak malam ini?\nLanjutkan tepat dari terakhir kali Anda berhenti.",
        "tr" to "İyi bir gece uykusuna hazır mısınız?\nKaldığınız yerden devam edin.",
        "hi" to "क्या आप अच्छी नींद के लिए तैयार हैं?\nवहीं से शुरू करें जहाँ आपने छोड़ा था।",
    ),
    "10 Million\nuser's choices" to mapOf(
        "es" to "Elección de\n10 Millones", "fr" to "Le choix de\n10 Millions", "de" to "Wahl von\n10 Millionen",
        "ja" to "1,000万人が\n選んだアプリ", "ko" to "1,000만 사용자의\n선택", "it" to "Scelto da\n10 Milioni",
        "ru" to "Выбор\n10 миллионов", "id" to "Pilihan\n10 Juta Pengguna", "tr" to "10 Milyon\nKullanıcı Seçimi", "hi" to "1 करोड़+\nलोगों की पसंद",
    ),
    "1 Billion+" to mapOf(
        "es" to "1.000 Millones+", "fr" to "1 Milliard+", "de" to "1 Milliarde+",
        "ja" to "10億回以上", "ko" to "10억 회 이상", "it" to "1 Miliardo+",
        "ru" to "Более 1 млрд", "id" to "1 Miliar+", "tr" to "1 Milyar+", "hi" to "1 अरब+",
    ),
    "nights tracked worldwide" to mapOf(
        "es" to "noches registradas en el mundo", "fr" to "nuits suivies dans le monde", "de" to "erfasste Nächte weltweit",
        "ja" to "世界中で記録された睡眠データ", "ko" to "전 세계 수면 분석 기록", "it" to "notti monitorate nel mondo",
        "ru" to "ночей сна проанализировано", "id" to "malam tidur terlacak di dunia", "tr" to "dünya çapında takip edilen gece", "hi" to "दुनिया भर में ट्रैक की गई रातें",
    ),
    "It's time to\nupgrade your sleep!" to mapOf(
        "es" to "¡Es hora de mejorar\ntu descanso!", "fr" to "Il est temps d'améliorer\nvotre sommeil !", "de" to "Zeit, deinen Schlaf\nzu verbessern!",
        "ja" to "今こそ、あなたの睡眠を\nアップグレードしましょう！", "ko" to "이제 당신의 수면을\n업그레이드할 시간입니다!", "it" to "È ora di migliorare\nil tuo sonno!",
        "ru" to "Пора улучшить\nкачество вашего сна!", "id" to "Saatnya meningkatkan\nkualitas tidur Anda!", "tr" to "Uykunuzu iyileştirme\nzamanı geldi!", "hi" to "अपनी नींद को बेहतर\nबनाने का समय आ गया है!",
    ),
    "Helped 9,200,000+ users sleep better every night" to mapOf(
        "es" to "Ayudó a más de 9.200.000 usuarios a dormir mejor cada noche",
        "fr" to "A aidé plus de 9 200 000 utilisateurs à mieux dormir chaque nuit",
        "de" to "Hat über 9.200.000 Nutzern geholfen, jede Nacht besser zu schlafen",
        "ja" to "920万人以上のユーザーが毎晩ぐっすり眠れるようになりました",
        "ko" to "920만 명 이상의 사용자가 매일 밤 더 깊게 잠들고 있습니다",
        "it" to "Ha aiutato oltre 9.200.000 utenti a dormire meglio ogni notte",
        "ru" to "Более 9 200 000 человек уже спят крепче каждую ночь",
        "id" to "Telah membantu 9.200.000+ pengguna tidur lebih nyenyak setiap malam",
        "tr" to "9.200.000+ kullanıcının her gece daha iyi uyumasına yardımcı oldu",
        "hi" to "92 लाख+ उपयोगकर्ताओं को हर रात बेहतर नींद पाने में मदद की",
    ),
    "Creating your sleep report" to mapOf(
        "es" to "Creando tu informe de sueño", "fr" to "Création de votre bilan sommeil", "de" to "Dein Schlafbericht wird erstellt",
        "ja" to "睡眠レポートを作成中", "ko" to "맞춤 수면 리포트 생성 중", "it" to "Creazione del tuo report del sonno",
        "ru" to "Создание вашего отчета о сне", "id" to "Membuat laporan tidur Anda", "tr" to "Uyku raporunuz oluşturuluyor", "hi" to "आपकी नींद रिपोर्ट तैयार की जा रही है",
    ),
    "10+ Million Users" to mapOf(
        "es" to "10+ Millones de Usuarios", "fr" to "10+ Millions d'Utilisateurs", "de" to "10+ Millionen Nutzer",
        "ja" to "1,000万人以上のユーザー", "ko" to "1,000만 명 이상의 사용자", "it" to "Oltre 10 Milioni di Utenti",
        "ru" to "Более 10 млн пользователей", "id" to "10+ Juta Pengguna", "tr" to "10+ Milyon Kullanıcı", "hi" to "1 करोड़+ उपयोगकर्ता",
    ),
    "have chosen our sleep soundscapes & tracker" to mapOf(
        "es" to "han elegido nuestros sonidos y monitor de sueño", "fr" to "ont choisi nos sons et notre suivi de sommeil", "de" to "vertrauen unseren Schlafklängen & dem Tracker",
        "ja" to "が私たちの睡眠サウンドとトラッカーを選んでいます", "ko" to "가 우리의 수면 사운드와 추적기를 선택했습니다", "it" to "hanno scelto i nostri suoni e il monitor del sonno",
        "ru" to "выбрали наши звуки для сна и умный трекер", "id" to "telah memilih suara tidur & pelacak kami", "tr" to "uyku seslerimizi ve takipçimizi tercih etti", "hi" to "ने हमारे स्लीप साउंड्स और ट्रैकर को चुना है",
    ),
    "#1 Sleep App" to mapOf(
        "es" to "App de Sueño N.º 1", "fr" to "Application Sommeil N°1", "de" to "Nr. 1 Schlaf-App",
        "ja" to "満足度 No.1 睡眠アプリ", "ko" to "만족도 1위 수면 앱", "it" to "App del Sonno N. 1",
        "ru" to "Приложение №1 для сна", "id" to "Aplikasi Tidur #1", "tr" to "1 Numaralı Uyku Uygulaması", "hi" to "#1 स्लीप ऐप",
    ),
    "by revenue & sleep satisfaction worldwide" to mapOf(
        "es" to "en satisfacción de descanso a nivel mundial", "fr" to "en satisfaction de sommeil dans le monde", "de" to "bei Schlafzufriedenheit weltweit",
        "ja" to "世界中のユーザー満足度でトップ評価", "ko" to "전 세계 수면 만족도 기준", "it" to "per soddisfazione del sonno nel mondo",
        "ru" to "по уровню удовлетворенности сном в мире", "id" to "dalam kepuasan kualitas tidur di seluruh dunia", "tr" to "dünya çapında uyku memnuniyetinde lider", "hi" to "दुनिया भर में नींद की संतुष्टि में अग्रणी",
    ),
    "50 million" to mapOf(
        "es" to "50 millones", "fr" to "50 millions", "de" to "50 Millionen", "ja" to "5,000万",
        "ko" to "5,000만", "it" to "50 milioni", "ru" to "50 млн", "id" to "50 juta", "tr" to "50 milyon", "hi" to "5 करोड़",
    ),
    "downloads" to mapOf(
        "es" to "descargas", "fr" to "téléchargements", "de" to "Downloads", "ja" to "ダウンロード",
        "ko" to "다운로드", "it" to "download", "ru" to "скачиваний", "id" to "unduhan", "tr" to "indirme", "hi" to "डाउनलोड",
    ),
    "2 billion" to mapOf(
        "es" to "2 mil millones", "fr" to "2 milliards", "de" to "2 Milliarden", "ja" to "20億回",
        "ko" to "20억 회", "it" to "2 miliardi", "ru" to "2 млрд", "id" to "2 miliar", "tr" to "2 milyar", "hi" to "2 अरब",
    ),
    "nights analyzed" to mapOf(
        "es" to "noches analizadas", "fr" to "nuits analysées", "de" to "analysierte Nächte", "ja" to "睡眠分析実績",
        "ko" to "수면 분석 완료", "it" to "notti analizzate", "ru" to "ночей изучено", "id" to "malam dianalisis", "tr" to "analiz edilen gece", "hi" to "रातों का विश्लेषण",
    ),
    "High-Rated Sleep App" to mapOf(
        "es" to "App Mejor Valorada", "fr" to "App Très Bien Notée", "de" to "Top-bewertete App", "ja" to "高評価の睡眠アプリ",
        "ko" to "최고 평점 수면 앱", "it" to "App Più Apprezzata", "ru" to "Высокий рейтинг", "id" to "Aplikasi Rating Tinggi", "tr" to "En Yüksek Puanlı", "hi" to "उच्च रेटिंग वाला ऐप",
    ),
    "Accurate Sleep Recorder" to mapOf(
        "es" to "Grabadora de Sueño Precisa", "fr" to "Enregistreur de Sommeil Précis", "de" to "Präziser Schlafrekorder", "ja" to "高精度スリープレコーダー",
        "ko" to "정밀 수면 녹음기", "it" to "Registratore del Sonno Preciso", "ru" to "Точный рекордер сна", "id" to "Perekam Tidur Akurat", "tr" to "Hassas Uyku Kaydedici", "hi" to "सटीक स्लीप रिकॉर्डर",
    ),
    "Find out what you did in\nyour sleep" to mapOf(
        "es" to "Descubre lo que hiciste\nmientras dormías", "fr" to "Découvrez ce que vous faites\npendant votre sommeil", "de" to "Finde heraus, was du\nim Schlaf getan hast", "ja" to "寝ている間のいびきや寝言を\nしっかりチェック",
        "ko" to "잠든 사이에 있었던 일을\n확인해 보세요", "it" to "Scopri cosa hai fatto\ndurante il sonno", "ru" to "Узнайте, что происходило,\nпока вы спали", "id" to "Ketahui apa yang terjadi\nsaat Anda tertidur", "tr" to "Uykunuzda neler yaptığınızı\nkeşfedin", "hi" to "जानें कि नींद के दौरान\nक्या हुआ",
    ),
    "You Snored" to mapOf(
        "es" to "Roncaste", "fr" to "Ronflement", "de" to "Geschnarcht", "ja" to "いびきを検出",
        "ko" to "코골이 감지", "it" to "Hai russato", "ru" to "Храп", "id" to "Mendengkur", "tr" to "Horlama", "hi" to "खर्राटे",
    ),
    "You Talked" to mapOf(
        "es" to "Hablaste dormido", "fr" to "Parole nocturne", "de" to "Im Schlaf geredet", "ja" to "寝言を検出",
        "ko" to "잠꼬대 감지", "it" to "Hai parlato", "ru" to "Разговор во сне", "id" to "Mengigau", "tr" to "Uykuda Konuşma", "hi" to "नींद में बोलना",
    ),
    "Deep Breathing" to mapOf(
        "es" to "Respiración profunda", "fr" to "Respiration profonde", "de" to "Tiefe Atmung", "ja" to "深い呼吸",
        "ko" to "깊은 호흡", "it" to "Respiro profondo", "ru" to "Глубокое дыхание", "id" to "Napas Dalam", "tr" to "Derin Nefes", "hi" to "गहरी सांस",
    ),
    "Powerful Sleep Tracker" to mapOf(
        "es" to "Monitor de Sueño Avanzado", "fr" to "Suivi du Sommeil Avancé", "de" to "Leistungsstarker Schlaftracker", "ja" to "高機能睡眠トラッカー",
        "ko" to "강력한 수면 추적기", "it" to "Monitor del Sonno Avanzato", "ru" to "Умный трекер сна", "id" to "Pelacak Tidur Canggih", "tr" to "Gelişmiş Uyku Takipçisi", "hi" to "शक्तिशाली स्लीप ट्रैकर",
    ),
    "Understand your sleep\nbetter than ever" to mapOf(
        "es" to "Comprende tu sueño\nmejor que nunca", "fr" to "Comprenez votre sommeil\nmieux que jamais", "de" to "Verstehe deinen Schlaf\nbesser als je zuvor", "ja" to "あなたの睡眠サイクルを\nこれまで以上に深く理解",
        "ko" to "당신의 수면 패턴을\n더 깊이 이해하세요", "it" to "Comprendi il tuo sonno\nmeglio che mai", "ru" to "Понимайте свой сон\nлучше, чем когда-либо", "id" to "Pahami kualitas tidur Anda\nlebih baik dari sebelumnya", "tr" to "Uykunuzu her zamankinden\ndaha iyi anlayın", "hi" to "अपनी नींद को पहले से\nबेहतर समझें",
    ),
    "Apnea Risk" to mapOf(
        "es" to "Riesgo de Apnea", "fr" to "Respiration", "de" to "Atemanalyse", "ja" to "呼吸の乱れ",
        "ko" to "수면 호흡", "it" to "Respiro Notturno", "ru" to "Дыхание", "id" to "Napas Malam", "tr" to "Gece Nefesi", "hi" to "श्वास विश्लेषण",
    ),
    "Track breathing & nighttime disturbances" to mapOf(
        "es" to "Monitorea la respiración e interrupciones", "fr" to "Suivez la respiration et les réveils", "de" to "Atmung & nächtliche Störungen erfassen", "ja" to "夜間の呼吸と睡眠の乱れを記録",
        "ko" to "호흡 패턴 및 야간 각성 기록", "it" to "Monitora il respiro e i risvegli", "ru" to "Отслеживание дыхания и пробуждений", "id" to "Pantau napas & gangguan malam", "tr" to "Nefes ve gece uyanmalarını izleyin", "hi" to "रात की सांस और बाधाओं को ट्रैक करें",
    ),
    "Sleep Stages" to mapOf(
        "es" to "Fases del Sueño", "fr" to "Cycles du Sommeil", "de" to "Schlafphasen", "ja" to "睡眠ステージ",
        "ko" to "수면 단계", "it" to "Fasi del Sonno", "ru" to "Фазы сна", "id" to "Fase Tidur", "tr" to "Uyku Evreleri", "hi" to "नींद के चरण",
    ),
    "Deep 32% · Light 54% · REM 14%" to mapOf(
        "es" to "Profundo 32% · Ligero 54% · REM 14%", "fr" to "Profond 32% · Léger 54% · REM 14%", "de" to "Tief 32% · Leicht 54% · REM 14%", "ja" to "深い 32% · 浅い 54% · レム 14%",
        "ko" to "깊은잠 32% · 얕은잠 54% · REM 14%", "it" to "Profondo 32% · Leggero 54% · REM 14%", "ru" to "Глубокий 32% · Легкий 54% · REM 14%", "id" to "Nyenyak 32% · Ringan 54% · REM 14%", "tr" to "Derin %32 · Hafif %54 · REM %14", "hi" to "गहरी 32% · हल्की 54% · REM 14%",
    ),
    "Snoring" to mapOf(
        "es" to "Ronquidos", "fr" to "Ronflements", "de" to "Schnarchen", "ja" to "いびき",
        "ko" to "코골이", "it" to "Russamento", "ru" to "Храп", "id" to "Dengkuran", "tr" to "Horlama", "hi" to "खर्राटे",
    ),
    "Max 76 dB · Duration & audio clips" to mapOf(
        "es" to "Máx 76 dB · Duración y clips de audio", "fr" to "Max 76 dB · Durée et extraits audio", "de" to "Max. 76 dB · Dauer & Audioclips", "ja" to "最大76 dB · 時間と録音クリップ",
        "ko" to "최대 76 dB · 지속 시간 및 오디오", "it" to "Max 76 dB · Durata e clip audio", "ru" to "Макс. 76 дБ · Длительность и записи", "id" to "Maks 76 dB · Durasi & klip audio", "tr" to "Maks 76 dB · Süre ve ses kayıtları", "hi" to "अधिकतम 76 dB · अवधि और ऑडियो",
    ),
    "Trends" to mapOf(
        "es" to "Tendencias", "fr" to "Tendances", "de" to "Trends", "ja" to "トレンド",
        "ko" to "수면 추세", "it" to "Tendenze", "ru" to "Динамика", "id" to "Tren Tidur", "tr" to "Trendler", "hi" to "रुझान",
    ),
    "Weekly & monthly sleep quality charts" to mapOf(
        "es" to "Gráficos semanales y mensuales", "fr" to "Graphiques hebdomadaires et mensuels", "de" to "Wochen- & Monatsstatistiken", "ja" to "週間・月間の睡眠スコア推移",
        "ko" to "주간 및 월간 수면 품질 차트", "it" to "Grafici settimanali e mensili", "ru" to "Графики за неделю и месяц", "id" to "Grafik tidur mingguan & bulanan", "tr" to "Haftalık ve aylık uyku grafikleri", "hi" to "साप्ताहिक और मासिक नींद चार्ट",
    ),
    "Smart Alarm" to mapOf(
        "es" to "Alarma Inteligente", "fr" to "Réveil Intelligent", "de" to "Smart-Wecker", "ja" to "スマートアラーム",
        "ko" to "스마트 알람", "it" to "Sveglia Smart", "ru" to "Умный будильник", "id" to "Alarm Pintar", "tr" to "Akıllı Alarm", "hi" to "स्मार्ट अलार्म",
    ),
    "Wake up gently in light sleep phase" to mapOf(
        "es" to "Despierta suavemente en sueño ligero", "fr" to "Réveil doux en phase de sommeil léger", "de" to "Sanft im Leichtschlaf aufwachen", "ja" to "浅い眠りのタイミングで優しく起床",
        "ko" to "얕은 수면 단계에서 상쾌하게 기상", "it" to "Risveglio dolce nella fase leggera", "ru" to "Мягкое пробуждение в легкой фазе", "id" to "Bangun segar di fase tidur ringan", "tr" to "Hafif uyku evresinde nazikçe uyanın", "hi" to "हल्की नींद के चरण में आराम से जागें",
    ),
    "Sleep Quality" to mapOf(
        "es" to "Calidad del Sueño", "fr" to "Qualité du Sommeil", "de" to "Schlafqualität", "ja" to "睡眠スコア",
        "ko" to "수면 점수", "it" to "Qualità del Sonno", "ru" to "Качество сна", "id" to "Kualitas Tidur", "tr" to "Uyku Kalitesi", "hi" to "नींद की गुणवत्ता",
    ),
    "Score 87 · Optimal recovery analysis" to mapOf(
        "es" to "Nota 87 · Análisis de recuperación", "fr" to "Score 87 · Analyse de récupération", "de" to "Wert 87 · Optimale Erholung", "ja" to "スコア87 · 最適な回復分析",
        "ko" to "87점 · 최적의 수면 회복 분석", "it" to "Punti 87 · Analisi recupero ottimale", "ru" to "Оценка 87 · Анализ восстановления", "id" to "Skor 87 · Analisis pemulihan optimal", "tr" to "Puan 87 · İdeal dinlenme analizi", "hi" to "स्कोर 87 · सर्वोत्तम रिकवरी विश्लेषण",
    ),
    "Best Sound Machine" to mapOf(
        "es" to "La Mejor Máquina de Sonidos", "fr" to "Meilleure Machine à Sons", "de" to "Beste Klangmaschine", "ja" to "最高のサウンドマシン",
        "ko" to "최고의 수면 사운드 머신", "it" to "Miglior Macchina del Suono", "ru" to "Лучшие звуки для сна", "id" to "Mesin Suara Tidur Terbaik", "tr" to "En İyi Uyku Ses Makinesi", "hi" to "सर्वश्रेष्ठ साउंड मशीन",
    ),
    "Customized white noise\nhelps you fall asleep faster" to mapOf(
        "es" to "El ruido blanco personalizado\nte ayuda a dormir más rápido", "fr" to "Le bruit blanc personnalisé\nvous aide à dormir plus vite", "de" to "Individuelles weißes Rauschen\nhilft beim schnellen Einschlafen", "ja" to "カスタマイズ可能な自然音で\nもっと早く深い眠りへ",
        "ko" to "맞춤형 백색소음으로\n더 빠르게 잠들어 보세요", "it" to "Il rumore bianco personalizzato\nti aiuta ad addormentarti prima", "ru" to "Персональный белый шум\nпомогает уснуть быстрее", "id" to "White noise kustom membantu\nAnda cepat tertidur", "tr" to "Özelleştirilmiş beyaz gürültü\ndaha hızlı uyumanıza yardımcı olur", "hi" to "कस्टमाइज़्ड व्हाइट नॉइज़ से\nजल्दी और गहरी नींद पाएं",
    ),
    "Get unlimited access to all 150+ sleep sounds, sleep analysis, snore recording, offline downloads & smart alarm." to mapOf(
        "es" to "Acceso ilimitado a los 150+ sonidos de sueño, análisis de sueño, grabación de ronquidos, descargas offline y alarma inteligente.",
        "fr" to "Accès illimité aux 150+ sons de sommeil, analyse du sommeil, détection des ronflements, mode hors-ligne et réveil intelligent.",
        "de" to "Unbegrenzter Zugriff auf alle 150+ Schlafklänge, Schlafanalyse, Schnarchrekorder, Offline-Downloads & Smart-Wecker.",
        "ja" to "150以上の全睡眠サウンド、睡眠分析、いびき録音、オフライン保存＆スマートアラームが無制限。",
        "ko" to "150개 이상의 모든 수면 사운드, 수면 분석, 코골이 녹음, 오프라인 다운로드 및 스마트 알람 무제한 이용.",
        "it" to "Accesso illimitato a tutti i 150+ suoni, analisi del sonno, registrazione russamento, download offline e sveglia smart.",
        "ru" to "Полный доступ ко всем 150+ звукам, анализу сна, записи храпа, офлайн-загрузкам и умному будильнику.",
        "id" to "Akses penuh ke semua 150+ suara tidur, analisis tidur, perekam dengkur, unduhan offline & alarm pintar.",
        "tr" to "Tüm 150+ uyku sesine, uyku analizine, horlama kaydına, çevrimdışı indirmelere ve akıllı alarma sınırsız erişim.",
        "hi" to "सभी 150+ स्लीप साउंड्स, नींद विश्लेषण, खर्राटे रिकॉर्डिंग, ऑफ़लाइन डाउनलोड और स्मार्ट अलार्म तक असीमित पहुंच।",
    ),
    "LUCKY DRAW" to mapOf(
        "es" to "SORTEO DE LA SUERTE", "fr" to "ROUE DE LA CHANCE", "de" to "GLÜCKSRAD", "ja" to "ラッキードロー",
        "ko" to "행운의 룰렛", "it" to "RUOTA DELLA FORTUNA", "ru" to "КОЛЕСО УДАЧИ", "id" to "RODA KEBERUNTUNGAN", "tr" to "ŞANS ÇARKI", "hi" to "लकी ड्रॉ",
    ),
    "Check how lucky you are!" to mapOf(
        "es" to "¡Descubre cuánta suerte tienes!", "fr" to "Tentez votre chance !", "de" to "Teste dein Glück!", "ja" to "運試しをして特別割引をゲット！",
        "ko" to "당신의 행운을 확인해 보세요!", "it" to "Scopri quanto sei fortunato!", "ru" to "Испытайте свою удачу!", "id" to "Coba keberuntungan Anda sekarang!", "tr" to "Ne kadar şanslı olduğunuzu görün!", "hi" to "आज अपनी किस्मत आज़माएं!",
    ),
    "SUPER PRIZE!" to mapOf(
        "es" to "¡SÚPER PREMIO!", "fr" to "SUPER PRIX !", "de" to "HAUPTGEWINN!", "ja" to "大当たり！",
        "ko" to "특별 당첨!", "it" to "SUPER PREMIO!", "ru" to "СУПЕРПРИЗ!", "id" to "HADIAH UTAMA!", "tr" to "SÜPER ÖDÜL!", "hi" to "सुपर प्राइज़!",
    ),
    "Congrats! You are so lucky!" to mapOf(
        "es" to "¡Felicidades! ¡Tienes mucha suerte!", "fr" to "Félicitations ! Quelle chance !", "de" to "Glückwunsch! Du hast gewonnen!", "ja" to "おめでとうございます！特別割引が当たりました！",
        "ko" to "축하합니다! 행운의 주인공이 되셨네요!", "it" to "Complimenti! Sei davvero fortunato!", "ru" to "Поздравляем! Вам улыбнулась удача!", "id" to "Selamat! Anda sangat beruntung!", "tr" to "Tebrikler! Çok şanslısınız!", "hi" to "बधाई हो! आप बहुत भाग्यशाली हैं!",
    ),
    "EXCLUSIVE NEW-USER OFFER UNLOCKED" to mapOf(
        "es" to "OFERTA EXCLUSIVA DESBLOQUEADA", "fr" to "OFFRE EXCLUSIVE DÉBLOQUÉE", "de" to "EXKLUSIVES ANGEBOT FREIGESCHALTET", "ja" to "初回限定オファーが解放されました",
        "ko" to "신규 회원 전용 특별 혜택 오픈", "it" to "OFFERTA ESCLUSIVA SBLOCCATA", "ru" to "ЭКСКЛЮЗИВНАЯ СКИДКА ОТКРЫТА", "id" to "PENAWARAN EKSKLUSIF TERBUKA", "tr" to "ÖZEL İNDİRİM TEKLİFİ AÇILDI", "hi" to "विशेष ऑफ़र अनलॉक किया गया",
    ),
    "Seize Now" to mapOf(
        "es" to "Aprovechar Ahora", "fr" to "Profiter de l'Offre", "de" to "Jetzt sichern", "ja" to "今すぐ受け取る",
        "ko" to "지금 혜택 받기", "it" to "Approfitta Ora", "ru" to "Забрать скидку", "id" to "Ambil Promo Sekarang", "tr" to "Fırsatı Yakala", "hi" to "अभी लाभ उठाएं",
    ),
    "How much sleep do you usually get at night?" to mapOf(
        "es" to "¿Cuántas horas sueles dormir por la noche?", "fr" to "Combien d'heures dormez-vous habituellement la nuit ?",
        "de" to "Wie viele Stunden schläfst du normalerweise nachts?", "ja" to "普段、夜の睡眠時間はどのくらいですか？",
        "ko" to "보통 밤에 몇 시간 정도 주무시나요?", "it" to "Quante ore dormi di solito la notte?",
        "ru" to "Сколько часов вы обычно спите ночью?", "id" to "Berapa jam biasanya Anda tidur di malam hari?",
        "tr" to "Geceleri genellikle kaç saat uyuyorsunuz?", "hi" to "आप आमतौर पर रात में कितने घंटे सोते हैं?",
    ),
    "6 hours or less" to mapOf(
        "es" to "6 horas o menos", "fr" to "6 heures ou moins", "de" to "6 Stunden oder weniger", "ja" to "6時間以下",
        "ko" to "6시간 이하", "it" to "6 ore o meno", "ru" to "6 часов или меньше", "id" to "6 jam atau kurang", "tr" to "6 saat veya daha az", "hi" to "6 घंटे या उससे कम",
    ),
    "6–8 hours" to mapOf(
        "es" to "6–8 horas", "fr" to "6–8 heures", "de" to "6–8 Stunden", "ja" to "6〜8時間",
        "ko" to "6~8시간", "it" to "6–8 ore", "ru" to "6–8 часов", "id" to "6–8 jam", "tr" to "6–8 saat", "hi" to "6–8 घंटे",
    ),
    "8–10 hours" to mapOf(
        "es" to "8–10 horas", "fr" to "8–10 heures", "de" to "8–10 Stunden", "ja" to "8〜10時間",
        "ko" to "8~10시간", "it" to "8–10 ore", "ru" to "8–10 часов", "id" to "8–10 jam", "tr" to "8–10 saat", "hi" to "8–10 घंटे",
    ),
    "More than 10 hours" to mapOf(
        "es" to "Más de 10 horas", "fr" to "Plus de 10 heures", "de" to "Mehr als 10 Stunden", "ja" to "10時間以上",
        "ko" to "10시간 이상", "it" to "Più di 10 ore", "ru" to "Более 10 часов", "id" to "Lebih dari 10 jam", "tr" to "10 saatten fazla", "hi" to "10 घंटे से अधिक",
    ),
    "How satisfied are you with your sleep?" to mapOf(
        "es" to "¿Qué tan satisfecho estás con tu sueño?", "fr" to "Êtes-vous satisfait de la qualité de votre sommeil ?",
        "de" to "Wie zufrieden bist du mit deinem Schlaf?", "ja" to "現在の睡眠の質にどのくらい満足していますか？",
        "ko" to "현재 수면의 질에 얼마나 만족하시나요?", "it" to "Quanto sei soddisfatto del tuo sonno?",
        "ru" to "Насколько вы довольны качеством своего сна?", "id" to "Seberapa puas Anda dengan kualitas tidur Anda?",
        "tr" to "Uyku kalitenizden ne kadar memnunsunuz?", "hi" to "आप अपनी नींद से कितने संतुष्ट हैं?",
    ),
    "Very satisfied" to mapOf(
        "es" to "Muy satisfecho", "fr" to "Très satisfait", "de" to "Sehr zufrieden", "ja" to "とても満足",
        "ko" to "매우 만족", "it" to "Molto soddisfatto", "ru" to "Очень доволен", "id" to "Sangat puas", "tr" to "Çok memnunum", "hi" to "बहुत संतुष्ट",
    ),
    "Neutral" to mapOf(
        "es" to "Normal", "fr" to "Neutre", "de" to "Neutral", "ja" to "普通",
        "ko" to "보통", "it" to "Neutrale", "ru" to "Нормально", "id" to "Biasa saja", "tr" to "Normal", "hi" to "सामान्य",
    ),
    "Unsatisfied" to mapOf(
        "es" to "Insatisfecho", "fr" to "Insatisfait", "de" to "Unzufrieden", "ja" to "やや不満",
        "ko" to "불만족", "it" to "Insoddisfatto", "ru" to "Недоволен", "id" to "Kurang puas", "tr" to "Memnun değilim", "hi" to "असंतुष्ट",
    ),
    "Very Unsatisfied" to mapOf(
        "es" to "Muy insatisfecho", "fr" to "Très insatisfait", "de" to "Sehr unzufrieden", "ja" to "とても不満",
        "ko" to "매우 불만족", "it" to "Molto insoddisfatto", "ru" to "Совсем недоволен", "id" to "Sangat tidak puas", "tr" to "Hiç memnun değilim", "hi" to "बहुत असंतुष्ट",
    ),
    "How long after getting in bed do you usually fall asleep?" to mapOf(
        "es" to "¿Cuánto tardas normalmente en dormirte al acostarte?", "fr" to "Combien de temps mettez-vous à vous endormir ?",
        "de" to "Wie lange brauchst du abends zum Einschlafen?", "ja" to "ベッドに入ってから眠りにつくまでどのくらいかかりますか？",
        "ko" to "침대에 누운 후 잠들기까지 보통 얼마나 걸리나요?", "it" to "Quanto tempo impieghi di solito per addormentarti?",
        "ru" to "Как быстро вы обычно засыпаете после того, как легли?", "id" to "Berapa lama waktu yang Anda butuhkan untuk tertidur?",
        "tr" to "Yatağa girdikten sonra uyumanız ne kadar sürüyor?", "hi" to "बिस्तर पर जाने के बाद आपको सोने में कितना समय लगता है?",
    ),
    "Immediately" to mapOf(
        "es" to "Inmediatamente", "fr" to "Immédiatement", "de" to "Sofort", "ja" to "すぐに眠れる",
        "ko" to "바로 잠듦", "it" to "Subito", "ru" to "Сразу", "id" to "Langsung tertidur", "tr" to "Hemen", "hi" to "तुरंत",
    ),
    "10–15 minutes" to mapOf(
        "es" to "10–15 minutos", "fr" to "10–15 minutes", "de" to "10–15 Minuten", "ja" to "10〜15分",
        "ko" to "10~15분", "it" to "10–15 minuti", "ru" to "10–15 минут", "id" to "10–15 menit", "tr" to "10–15 dakika", "hi" to "10–15 मिनट",
    ),
    "20–40 minutes" to mapOf(
        "es" to "20–40 minutos", "fr" to "20–40 minutes", "de" to "20–40 Minuten", "ja" to "20〜40分",
        "ko" to "20~40분", "it" to "20–40 minuti", "ru" to "20–40 минут", "id" to "20–40 menit", "tr" to "20–40 dakika", "hi" to "20–40 मिनट",
    ),
    "Hard to fall asleep" to mapOf(
        "es" to "Me cuesta mucho dormir", "fr" to "Difficile de m'endormir", "de" to "Schwer einzuschlafen", "ja" to "なかなか眠れない",
        "ko" to "잠들기 매우 힘듦", "it" to "Fatico ad addormentarmi", "ru" to "Трудно уснуть", "id" to "Sulit tertidur", "tr" to "Uykuya dalmakta zorlanıyorum", "hi" to "सोने में बहुत कठिनाई होती है",
    ),
    "Do you snore or talk in your sleep at night?" to mapOf(
        "es" to "¿Roncas o hablas dormido por la noche?", "fr" to "Ronflez-vous ou parlez-vous en dormant la nuit ?",
        "de" to "Schnarchst oder sprichst du nachts im Schlaf?", "ja" to "夜中にいびきをかいたり寝言を言ったりしますか？",
        "ko" to "밤에 코를 골거나 잠꼬대를 하시나요?", "it" to "Russi o parli nel sonno durante la notte?",
        "ru" to "Вы храпите или разговариваете во сне?", "id" to "Apakah Anda mendengkur atau mengigau saat tidur?",
        "tr" to "Gece uykunuzda horluyor veya konuşuyor musunuz?", "hi" to "क्या आप रात में सोते समय खर्राटे लेते हैं या बोलते हैं?",
    ),
    "I don't know" to mapOf(
        "es" to "No lo sé", "fr" to "Je ne sais pas", "de" to "Ich weiß nicht", "ja" to "わからない",
        "ko" to "잘 모르겠음", "it" to "Non lo so", "ru" to "Не знаю", "id" to "Tidak tahu", "tr" to "Bilmiyorum", "hi" to "मुझे नहीं पता",
    ),
    "No, I don't snore" to mapOf(
        "es" to "No, no ronco", "fr" to "Non, je ne ronfle pas", "de" to "Nein, ich schnarche nicht", "ja" to "いいえ、かきません",
        "ko" to "아니요, 안 합니다", "it" to "No, non russo", "ru" to "Нет, не храплю", "id" to "Tidak mendengkur", "tr" to "Hayır, horlamıyorum", "hi" to "नहीं",
    ),
    "Yes, sometimes" to mapOf(
        "es" to "Sí, a veces", "fr" to "Oui, parfois", "de" to "Ja, manchmal", "ja" to "はい、時々あります",
        "ko" to "네, 가끔 합니다", "it" to "Sì, a volte", "ru" to "Да, иногда", "id" to "Ya, kadang-kadang", "tr" to "Evet, bazen", "hi" to "हाँ, कभी-कभी",
    ),
    "Often / told by partner" to mapOf(
        "es" to "Con frecuencia", "fr" to "Souvent", "de" to "Häufig", "ja" to "よくある・指摘される",
        "ko" to "자주 함 / 주변에서 들음", "it" to "Spesso", "ru" to "Часто", "id" to "Sering sekali", "tr" to "Sık sık", "hi" to "अक्सर",
    ),
    "Does lack of sleep affect your daily life?" to mapOf(
        "es" to "¿La falta de sueño afecta tu vida diaria?", "fr" to "Le manque de sommeil affecte-t-il votre quotidien ?",
        "de" to "Beeinträchtigt Schlafmangel deinen Alltag?", "ja" to "睡眠不足は日常生活に影響していますか？",
        "ko" to "수면 부족이 일상생활에 영향을 미치나요?", "it" to "La mancanza di sonno influisce sulla tua giornata?",
        "ru" to "Влияет ли недосып на вашу повседневную жизнь?", "id" to "Apakah kurang tidur memengaruhi aktivitas harian Anda?",
        "tr" to "Uykusuzluk günlük hayatınızı etkiliyor mu?", "hi" to "क्या नींद की कमी आपके दैनिक जीवन को प्रभावित करती है?",
    ),
    "Not at all" to mapOf(
        "es" to "Para nada", "fr" to "Pas du tout", "de" to "Überhaupt nicht", "ja" to "全くない",
        "ko" to "전혀 없음", "it" to "Per niente", "ru" to "Совсем нет", "id" to "Tidak sama sekali", "tr" to "Hiç etkilemiyor", "hi" to "बिल्कुल नहीं",
    ),
    "A little" to mapOf(
        "es" to "Un poco", "fr" to "Un peu", "de" to "Ein wenig", "ja" to "少しある",
        "ko" to "조금 있음", "it" to "Un po'", "ru" to "Немного", "id" to "Sedikit", "tr" to "Biraz", "hi" to "थोड़ा बहुत",
    ),
    "Somewhat" to mapOf(
        "es" to "Bastante", "fr" to "Moyennement", "de" to "Merklich", "ja" to "ある程度ある",
        "ko" to "어느 정도 있음", "it" to "Abbastanza", "ru" to "Заметно", "id" to "Cukup terasa", "tr" to "Oldukça", "hi" to "काफी हद तक",
    ),
    "Very much" to mapOf(
        "es" to "Muchísimo", "fr" to "Beaucoup", "de" to "Sehr stark", "ja" to "大きく影響している",
        "ko" to "매우 많이 영향받음", "it" to "Moltissimo", "ru" to "Очень сильно", "id" to "Sangat berpengaruh", "tr" to "Çok fazla", "hi" to "बहुत ज़्यादा",
    ),
    "Nightstand guide" to mapOf(
        "es" to "Guía de mesita", "fr" to "Guide table de nuit", "de" to "Nachttisch-Info", "ja" to "配置ガイド",
        "ko" to "배치 가이드", "it" to "Guida comodino", "ru" to "Гид по размещению", "id" to "Panduan posisi", "tr" to "Yerleşim rehberi", "hi" to "स्थान गाइड",
    ),
    "selected" to mapOf(
        "es" to "seleccionados", "fr" to "sélectionnés", "de" to "ausgewählt", "ja" to "件選択中",
        "ko" to "개 선택됨", "it" to "selezionati", "ru" to "выбрано", "id" to "dipilih", "tr" to "seçildi", "hi" to "चयनित",
    ),
    "Wake Up & View Sleep Report" to mapOf(
        "es" to "Despertar y Ver Informe de Sueño", "fr" to "Se Réveiller & Voir le Rapport", "de" to "Aufwachen & Schlafbericht ansehen", "ja" to "起床して睡眠レポートを見る",
        "ko" to "기상 및 수면 리포트 보기", "it" to "Svegliati e Vedi Report del Sonno", "ru" to "Проснуться и открыть отчет", "id" to "Bangun & Lihat Laporan Tidur", "tr" to "Uyan ve Uyku Raporunu Gör", "hi" to "जागें और नींद रिपोर्ट देखें",
    ),
    "Wake-up Alarm · Gentle phase wake" to mapOf(
        "es" to "Alarma Inteligente · Despertar suave", "fr" to "Réveil Intelligent · Phase légère", "de" to "Smart-Wecker · Sanftes Aufwachen", "ja" to "スマートアラーム · 浅い眠りで起床",
        "ko" to "스마트 알람 · 얕은 수면 기상", "it" to "Sveglia Smart · Risveglio dolce", "ru" to "Умный будильник · Мягкий подъем", "id" to "Alarm Pintar · Bangun lembut", "tr" to "Akıllı Alarm · Nazik uyandırma", "hi" to "स्मार्ट अलार्म · कोमल जागरण",
    ),
    "Smart Wake-up Alarm" to mapOf(
        "es" to "Alarma Inteligente", "fr" to "Réveil Intelligent", "de" to "Smart-Wecker", "ja" to "スマートアラーム",
        "ko" to "스마트 기상 알람", "it" to "Sveglia Intelligente", "ru" to "Умный будильник", "id" to "Alarm Bangun Pintar", "tr" to "Akıllı Uyandırma Alarmı", "hi" to "स्मार्ट वेक-अप अलार्म",
    ),
    "Hour" to mapOf(
        "es" to "Hora", "fr" to "Heure", "de" to "Stunde", "ja" to "時",
        "ko" to "시", "it" to "Ora", "ru" to "Часы", "id" to "Jam", "tr" to "Saat", "hi" to "घंटा",
    ),
    "Minute" to mapOf(
        "es" to "Minuto", "fr" to "Minute", "de" to "Minute", "ja" to "分",
        "ko" to "분", "it" to "Minuto", "ru" to "Минуты", "id" to "Menit", "tr" to "Dakika", "hi" to "मिनट",
    ),
    "Smart Wake Window (wakes you in light sleep)" to mapOf(
        "es" to "Ventana inteligente (te despierta en sueño ligero)", "fr" to "Plage intelligente (réveil en sommeil léger)", "de" to "Smart-Zeitfenster (weckt im Leichtschlaf)", "ja" to "スマート起床枠（浅い眠りで起こします）",
        "ko" to "스마트 기상 구간 (얕은 수면에서 깨움)", "it" to "Finestra smart (risveglio nel sonno leggero)", "ru" to "Умное окно (будит в фазе легкого сна)", "id" to "Jendela pintar (membangunkan di tidur ringan)", "tr" to "Akıllı pencere (hafif uykuda uyandırır)", "hi" to "स्मार्ट वेक विंडो (हल्की नींद में जगाता है)",
    ),
    "Exact" to mapOf(
        "es" to "Exacto", "fr" to "Exact", "de" to "Genau", "ja" to "定刻",
        "ko" to "정각", "it" to "Esatto", "ru" to "Точно", "id" to "Tepat", "tr" to "Tam", "hi" to "सटीक",
    ),
    "Gentle Vibration" to mapOf(
        "es" to "Vibración suave", "fr" to "Vibration douce", "de" to "Sanfte Vibration", "ja" to "優しいバイブレーション",
        "ko" to "부드러운 진동", "it" to "Vibrazione delicata", "ru" to "Мягкая вибрация", "id" to "Getaran Lembut", "tr" to "Nazik Titreşim", "hi" to "हल्का कंपन",
    ),
    "Save Alarm" to mapOf(
        "es" to "Guardar Alarma", "fr" to "Enregistrer l'Alarme", "de" to "Wecker speichern", "ja" to "アラームを保存",
        "ko" to "알람 저장", "it" to "Salva Sveglia", "ru" to "Сохранить будильник", "id" to "Simpan Alarm", "tr" to "Alarmı Kaydet", "hi" to "अलार्म सहेजें",
    ),
    "Phone Placement & Battery Guide" to mapOf(
        "es" to "Guía de Colocación y Batería", "fr" to "Guide de Placement et Batterie", "de" to "Platzierungs- & Akku-Hinweis", "ja" to "スマートフォンの配置とバッテリー案内",
        "ko" to "휴대폰 배치 및 배터리 가이드", "it" to "Guida Posizione e Batteria", "ru" to "Размещение телефона и заряд", "id" to "Panduan Posisi Ponsel & Baterai", "tr" to "Telefon Yerleşimi ve Pil Rehberi", "hi" to "फ़ोन स्थान और बैटरी गाइड",
    ),
    "Place your phone on your nightstand or near your pillow with the microphone facing toward your bed (30–50 cm away)." to mapOf(
        "es" to "Coloca tu teléfono en la mesita de noche o cerca de la almohada con el micrófono hacia la cama (a 30–50 cm).",
        "fr" to "Placez votre téléphone sur la table de chevet ou près de l'oreiller avec le micro tourné vers le lit (30–50 cm).",
        "de" to "Legen Sie Ihr Smartphone auf den Nachttisch oder neben das Kissen, mit dem Mikrofon zum Bett gerichtet (30–50 cm).",
        "ja" to "マイクをベッド側に向けて、ナイトテーブルや枕元（30〜50cm以内）にスマートフォンを置いてください。",
        "ko" to "마이크가 침대 쪽을 향하도록 협탁이나 베개 근처(30~50cm 거리)에 휴대폰을 두세요.",
        "it" to "Posiziona il telefono sul comodino o vicino al cuscino con il microfono rivolto verso il letto (30–50 cm).",
        "ru" to "Положите телефон на тумбочку или рядом с подушкой микрофоном к кровати (на расстоянии 30–50 см).",
        "id" to "Letakkan ponsel di meja samping tempat tidur atau dekat bantal dengan mikrofon menghadap kasur (jarak 30–50 cm).",
        "tr" to "Telefonunuzu mikrofonu yatağa bakacak şekilde komodine veya yastığınızın yanına (30–50 cm uzağa) yerleştirin.",
        "hi" to "अपने फ़ोन को तकिए के पास या साइड टेबल पर रखें और माइक्रोफ़ोन बिस्तर की ओर रखें (30–50 सेमी दूर)।",
    ),
    "Keep your device connected to a charger overnight or ensure at least 40% battery before starting the sleep tracker." to mapOf(
        "es" to "Mantén el dispositivo conectado al cargador durante la noche o asegúrate de tener al menos 40% de batería.",
        "fr" to "Laissez votre appareil branché toute la nuit ou assurez-vous d'avoir au moins 40% de batterie.",
        "de" to "Lassen Sie das Gerät über Nacht am Ladegerät oder stellen Sie mindestens 40% Akkuladung sicher.",
        "ja" to "一晩中充電器に接続しておくか、40%以上のバッテリー残量がある状態で計測を開始してください。",
        "ko" to "밤새 충전기를 연결해 두거나 최소 40% 이상의 배터리를 확보한 후 수면 추적을 시작하세요.",
        "it" to "Tieni il dispositivo collegato al caricabatterie durante la notte o assicurati almeno il 40% di carica.",
        "ru" to "Оставьте телефон на зарядке на ночь или убедитесь, что заряд батареи не менее 40%.",
        "id" to "Hubungkan perangkat ke pengisi daya semalaman atau pastikan baterai minimal 40% sebelum memulai.",
        "tr" to "Gece boyunca cihazınızı şarjda bırakın veya başlatmadan önce en az %40 şarj olduğundan emin olun.",
        "hi" to "रात भर फ़ोन को चार्जर से जोड़े रखें या ट्रैकर शुरू करने से पहले कम से कम 40% बैटरी सुनिश्चित करें।",
    ),
    "Got It" to mapOf(
        "es" to "Entendido", "fr" to "Compris", "de" to "Verstanden", "ja" to "了解",
        "ko" to "확인", "it" to "Ho capito", "ru" to "Понятно", "id" to "Mengerti", "tr" to "Anladım", "hi" to "समझ गया",
    ),
    "Pre-Sleep Notes & Mood" to mapOf(
        "es" to "Notas de Sueño y Ánimo", "fr" to "Notes de Sommeil & Humeur", "de" to "Schlafnotizen & Stimmung", "ja" to "就寝前メモ＆目覚めの気分",
        "ko" to "취침 전 메모 및 기분", "it" to "Note sul Sonno e Umore", "ru" to "Заметки перед сном и настроение", "id" to "Catatan Sebelum Tidur & Mood", "tr" to "Uyku Öncesi Notlar ve Mod", "hi" to "सोने से पहले के नोट्स और मूड",
    ),
    "Tag activities before bed to see how they affect your deep sleep." to mapOf(
        "es" to "Marca tus actividades antes de dormir para ver cómo afectan tu sueño profundo.",
        "fr" to "Notez vos activités avant de dormir pour voir leur impact sur votre sommeil profond.",
        "de" to "Markiere Aktivitäten vor dem Schlafen, um ihren Einfluss auf deinen Tiefschlaf zu sehen.",
        "ja" to "就寝前の習慣を記録して、深い睡眠への影響を確認しましょう。",
        "ko" to "잠들기 전 활동을 기록하여 깊은 수면에 미치는 영향을 확인하세요.",
        "it" to "Seleziona le attività prima di dormire per vedere come influenzano il sonno profondo.",
        "ru" to "Отмечайте дневные привычки, чтобы видеть их влияние на глубокий сон.",
        "id" to "Tandai aktivitas sebelum tidur untuk melihat pengaruhnya pada tidur nyenyak Anda.",
        "tr" to "Derin uykunuzu nasıl etkilediğini görmek için uyku öncesi aktiviteleri işaretleyin.",
        "hi" to "यह देखने के लिए कि वे आपकी गहरी नींद को कैसे प्रभावित करते हैं, सोने से पहले की गतिविधियाँ चुनें।",
    ),
    "Expected Wake-up Mood" to mapOf(
        "es" to "Ánimo al Despertar", "fr" to "Humeur au Réveil", "de" to "Aufwachstimmung", "ja" to "目覚めの気分",
        "ko" to "기상 시 기분", "it" to "Umore al Risveglio", "ru" to "Настроение при пробуждении", "id" to "Mood Saat Bangun", "tr" to "Uyanma Modu", "hi" to "जागने पर मूड",
    ),
    "Track" to mapOf(
        "es" to "Iniciar", "fr" to "Suivre", "de" to "Starten", "ja" to "計測",
        "ko" to "측정", "it" to "Avvia", "ru" to "Старт", "id" to "Lacak", "tr" to "Başlat", "hi" to "ट्रैक करें",
    ),
    "Excellent" to mapOf(
        "es" to "Excelente", "fr" to "Excellent", "de" to "Ausgezeichnet", "ja" to "最高",
        "ko" to "매우 좋음", "it" to "Eccellente", "ru" to "Отлично", "id" to "Sangat Baik", "tr" to "Mükemmel", "hi" to "उत्कृष्ट",
    ),
    "Fair" to mapOf(
        "es" to "Regular", "fr" to "Moyen", "de" to "Mittel", "ja" to "やや不足",
        "ko" to "보통", "it" to "Discreto", "ru" to "Средне", "id" to "Cukup", "tr" to "Orta", "hi" to "सामान्य",
    ),
    "Needs Rest" to mapOf(
        "es" to "Necesita Descanso", "fr" to "Repos Nécessaire", "de" to "Mehr Ruhe nötig", "ja" to "休息が必要",
        "ko" to "휴식 필요", "it" to "Serve Riposo", "ru" to "Нужен отдых", "id" to "Butuh Istirahat", "tr" to "Dinlenme Gerekli", "hi" to "आराम की ज़रूरत",
    ),
    "Time in Bed" to mapOf(
        "es" to "Tiempo en Cama", "fr" to "Temps au Lit", "de" to "Zeit im Bett", "ja" to "就床時間",
        "ko" to "수면 시간", "it" to "Tempo a Letto", "ru" to "Время в постели", "id" to "Waktu Tidur", "tr" to "Yatakta Kalma Süresi", "hi" to "बिस्तर पर समय",
    ),
    "Wake-up mood" to mapOf(
        "es" to "Ánimo al despertar", "fr" to "Humeur au réveil", "de" to "Aufwachstimmung", "ja" to "目覚めの気分",
        "ko" to "기상 기분", "it" to "Umore al risveglio", "ru" to "Настроение утром", "id" to "Mood bangun", "tr" to "Uyanma modu", "hi" to "जागने का मूड",
    ),
    "Awake · Light · Deep" to mapOf(
        "es" to "Despierto · Ligero · Profundo", "fr" to "Éveillé · Léger · Profond", "de" to "Wach · Leicht · Tief", "ja" to "覚醒 · 浅い · 深い",
        "ko" to "깸 · 얕은잠 · 깊은잠", "it" to "Sveglio · Leggero · Profondo", "ru" to "Бодрств. · Легкий · Глубокий", "id" to "Terjaga · Ringan · Nyenyak", "tr" to "Uyanık · Hafif · Derin", "hi" to "जागना · हल्की · गहरी",
    ),
    "Awake" to mapOf(
        "es" to "Despierto", "fr" to "Éveillé", "de" to "Wach", "ja" to "覚醒",
        "ko" to "깨어남", "it" to "Sveglio", "ru" to "Пробуждение", "id" to "Terjaga", "tr" to "Uyanık", "hi" to "जागना",
    ),
    "Light Sleep" to mapOf(
        "es" to "Sueño Ligero", "fr" to "Sommeil Léger", "de" to "Leichtschlaf", "ja" to "浅い睡眠",
        "ko" to "얕은 수면", "it" to "Sonno Leggero", "ru" to "Легкий сон", "id" to "Tidur Ringan", "tr" to "Hafif Uyku", "hi" to "हल्की नींद",
    ),
    "Deep Sleep" to mapOf(
        "es" to "Sueño Profundo", "fr" to "Sommeil Profond", "de" to "Tiefschlaf", "ja" to "深い睡眠",
        "ko" to "깊은 수면", "it" to "Sonno Profondo", "ru" to "Глубокий сон", "id" to "Tidur Nyenyak", "tr" to "Derin Uyku", "hi" to "गहरी नींद",
    ),
    "Fell Asleep" to mapOf(
        "es" to "Se durmió en", "fr" to "Endormi en", "de" to "Eingeschlafen in", "ja" to "入眠時間",
        "ko" to "입면 시간", "it" to "Addormentato in", "ru" to "Заснул за", "id" to "Tertidur dalam", "tr" to "Uykuya Dalma", "hi" to "नींद आने का समय",
    ),
    "Ambient Noise & Sleep Factors" to mapOf(
        "es" to "Ruido Ambiente y Factores de Sueño", "fr" to "Bruit Ambiant & Facteurs de Sommeil", "de" to "Umgebungsgeräusche & Schlaffaktoren", "ja" to "周囲の物音＆睡眠ファクター",
        "ko" to "주변 소음 및 수면 요인", "it" to "Rumore Ambientale e Fattori del Sonno", "ru" to "Фоновый шум и факторы сна", "id" to "Suara Sekitar & Faktor Tidur", "tr" to "Ortam Gürültüsü ve Uyku Faktörleri", "hi" to "आसपास का शोर और नींद के कारक",
    ),
    "Quiet environment · 0 loud snore or talk disruptions detected" to mapOf(
        "es" to "Ambiente silencioso · 0 interrupciones por ronquidos detectadas",
        "fr" to "Environnement calme · 0 perturbation sonore détectée",
        "de" to "Ruhige Umgebung · 0 laute Schnarch- oder Sprachstörungen erkannt",
        "ja" to "静かな睡眠環境 · 大音量のいびきや寝言は検出されませんでした",
        "ko" to "조용한 수면 환경 · 큰 코골이나 잠꼬대가 감지되지 않았습니다",
        "it" to "Ambiente silenzioso · 0 interruzioni sonore rilevate",
        "ru" to "Тихая обстановка · Громкого храпа или шума не обнаружено",
        "id" to "Lingkungan tenang · 0 gangguan dengkur keras terdeteksi",
        "tr" to "Sessiz ortam · Yüksek sesli horlama veya konuşma algılanmadı",
        "hi" to "शांत वातावरण · कोई तेज़ खर्राटे या आवाज़ की बाधा नहीं मिली",
    ),
    "Smart Alarm & Sleep Tracking" to mapOf(
        "es" to "Alarma Inteligente y Monitor", "fr" to "Réveil Intelligent & Suivi", "de" to "Smart-Wecker & Schlaftracking", "ja" to "スマートアラーム＆睡眠計測",
        "ko" to "스마트 알람 및 수면 추적", "it" to "Sveglia Smart e Monitoraggio", "ru" to "Умный будильник и трекер", "id" to "Alarm Pintar & Pelacak Tidur", "tr" to "Akıllı Alarm ve Uyku Takibi", "hi" to "स्मार्ट अलार्म और स्लीप ट्रैकिंग",
    ),
    "Smart wake window" to mapOf(
        "es" to "Ventana inteligente", "fr" to "Réveil intelligent", "de" to "Smart-Weckfenster", "ja" to "スマート起床枠",
        "ko" to "스마트 기상 구간", "it" to "Finestra smart", "ru" to "Умное окно пробуждения", "id" to "Jendela bangun pintar", "tr" to "Akıllı uyandırma aralığı", "hi" to "स्मार्ट वेक विंडो",
    ),
    "Exact wake-up alarm" to mapOf(
        "es" to "Alarma a la hora exacta", "fr" to "Réveil à l'heure exacte", "de" to "Wecker zur genauen Zeit", "ja" to "定刻アラーム",
        "ko" to "정각 기상 알람", "it" to "Sveglia all'ora esatta", "ru" to "Будильник точно по времени", "id" to "Alarm tepat waktu", "tr" to "Tam zamanında alarm", "hi" to "सटीक समय अलार्म",
    ),
    "Sleep Journal & Reports" to mapOf(
        "es" to "Diario e Informes de Sueño", "fr" to "Journal & Rapports de Sommeil", "de" to "Schlaftagebuch & Berichte", "ja" to "睡眠日誌＆詳細レポート",
        "ko" to "수면 일지 및 리포트", "it" to "Diario e Report del Sonno", "ru" to "Дневник и отчеты о сне", "id" to "Jurnal & Laporan Tidur", "tr" to "Uyku Günlüğü ve Raporlar", "hi" to "स्लीप जर्नल और रिपोर्ट",
    ),
    "View hypnogram stages, sleep score & snore logs" to mapOf(
        "es" to "Ver fases del sueño, puntuación y registros", "fr" to "Voir les cycles, le score de sommeil et les sons", "de" to "Hypnogramm, Schlaf-Score & Schnarchprotokolle", "ja" to "睡眠ステージ、スコア、いびき記録を確認",
        "ko" to "수면 단계 차트, 점수 및 코골이 기록 확인", "it" to "Vedi fasi del sonno, punteggio e registrazioni", "ru" to "Гипнограмма фаз, оценка сна и записи звуков", "id" to "Lihat grafik fase tidur, skor & catatan suara", "tr" to "Uyku evrelerini, puanı ve ses kayıtlarını görün", "hi" to "नींद के चरण, स्कोर और खर्राटे के लॉग देखें",
    ),
    "Sleep Quiz & Personalized Report" to mapOf(
        "es" to "Evaluación de Sueño e Informe", "fr" to "Test de Sommeil & Bilan", "de" to "Schlaf-Test & Persönlicher Bericht", "ja" to "睡眠診断テスト＆個別レポート",
        "ko" to "수면 진단 테스트 및 맞춤 리포트", "it" to "Test del Sonno e Report", "ru" to "Тест сна и персональный отчет", "id" to "Kuis Tidur & Laporan Pribadi", "tr" to "Uyku Testi ve Kişisel Rapor", "hi" to "स्लीप क्विज़ और व्यक्तिगत रिपोर्ट",
    ),
    "Retake the sleep assessment & view special offers" to mapOf(
        "es" to "Repetir la evaluación de sueño y ver ofertas", "fr" to "Refaire le test de sommeil et voir les offres", "de" to "Schlafanalyse wiederholen & Angebote ansehen", "ja" to "睡眠診断をやり直して特別オファーを確認",
        "ko" to "수면 진단을 다시 받고 특별 혜택을 확인하세요", "it" to "Ripeti il test del sonno e scopri le offerte", "ru" to "Пройти тест заново и посмотреть спецпредложения", "id" to "Ulangi kuis tidur & lihat penawaran spesial", "tr" to "Uyku testini tekrarlayın ve özel teklifleri görün", "hi" to "नींद मूल्यांकन फिर से करें और ऑफ़र देखें",
    ),
    "Curated pillows, sleep masks & relaxation essentials" to mapOf(
        "es" to "Almohadas, antifaces para dormir y accesorios de relax", "fr" to "Oreillers, masques de nuit et accessoires détente", "de" to "Ausgewählte Kissen, Schlafmasken & Entspannung", "ja" to "厳選された快眠枕・アイマスク・リラックスグッズ",
        "ko" to "엄선된 숙면 베개, 수면 안대 및 릴랙싱 용품", "it" to "Cuscini, mascherine per dormire e accessori relax", "ru" to "Подушки, маски для сна и товары для релакса", "id" to "Bantal tidur, penutup mata & perlengkapan relaksasi", "tr" to "Seçkin yastıklar, uyku maskeleri ve rahatlama ürünleri", "hi" to "चुनिंदा तकिए, स्लीप मास्क और विश्राम सामग्री",
    ),
    "Calibrating" to mapOf(
        "es" to "Calibrando", "fr" to "Calibrage", "de" to "Kalibrierung", "ja" to "計測待ち",
        "ko" to "측정 대기", "it" to "Calibrazione", "ru" to "Калибровка", "id" to "Kalibrasi", "tr" to "Ölçülüyor", "hi" to "आकलन जारी",
    ),
    "Avg Quality" to mapOf(
        "es" to "Calidad Media", "fr" to "Qualité Moy.", "de" to "Ø Qualität", "ja" to "平均品質",
        "ko" to "평균 품질", "it" to "Qualità Media", "ru" to "Ср. качество", "id" to "Kualitas Rata", "tr" to "Ort. Kalite", "hi" to "औसत गुणवत्ता",
    ),
    "Nights" to mapOf(
        "es" to "Noches", "fr" to "Nuits", "de" to "Nächte", "ja" to "夜",
        "ko" to "박", "it" to "Notti", "ru" to "ночей", "id" to "Malam", "tr" to "Gece", "hi" to "रातें",
    ),
    "Tracked" to mapOf(
        "es" to "Registradas", "fr" to "Suivies", "de" to "Erfasst", "ja" to "計測済み",
        "ko" to "측정 완료", "it" to "Monitorate", "ru" to "Отслежено", "id" to "Terlacak", "tr" to "Takip Edilen", "hi" to "ट्रैक किया",
    ),
    "Avg Duration" to mapOf(
        "es" to "Duración Media", "fr" to "Durée Moy.", "de" to "Ø Dauer", "ja" to "平均睡眠時間",
        "ko" to "평균 수면 시간", "it" to "Durata Media", "ru" to "Ср. время", "id" to "Durasi Rata", "tr" to "Ort. Süre", "hi" to "औसत अवधि",
    ),
    "Clear" to mapOf(
        "es" to "Borrar", "fr" to "Effacer", "de" to "Löschen", "ja" to "クリア",
        "ko" to "지우기", "it" to "Cancella", "ru" to "Очистить", "id" to "Hapus", "tr" to "Temizle", "hi" to "साफ़ करें",
    ),
    "No Favorites Yet" to mapOf(
        "es" to "Aún no hay favoritos", "fr" to "Aucun favori pour le moment", "de" to "Noch keine Favoriten", "ja" to "お気に入りはまだありません",
        "ko" to "아직 즐겨찾기가 없습니다", "it" to "Nessun preferito", "ru" to "Пока нет избранного", "id" to "Belum Ada Favorit", "tr" to "Henüz Favori Yok", "hi" to "अभी कोई पसंदीदा नहीं",
    ),
    "Save custom mixes or tap the heart icon on any soundscape or story to find them here." to mapOf(
        "es" to "Guarda tus mezclas o toca el corazón en cualquier sonido o cuento para verlos aquí.",
        "fr" to "Enregistrez vos mix ou appuyez sur le cœur d'un son ou d'une histoire pour les retrouver ici.",
        "de" to "Speichere eigene Mixe oder tippe auf das Herz bei Klängen & Geschichten, um sie hier zu finden.",
        "ja" to "カスタムミックスを保存するか、サウンドやストーリーのハートをタップするとここに表示されます。",
        "ko" to "나만의 믹스를 저장하거나 사운드 및 스토리의 하트 아이콘을 누르면 여기에 표시됩니다.",
        "it" to "Salva i tuoi mix o tocca il cuore su qualsiasi suono o storia per trovarli qui.",
        "ru" to "Сохраняйте свои миксы или нажимайте на сердечко на звуках и историях, чтобы видеть их здесь.",
        "id" to "Simpan mix kustom atau ketuk ikon hati pada suasana alam atau cerita untuk melihatnya di sini.",
        "tr" to "Özel karışımları kaydedin veya herhangi bir ses ya da hikayede kalp simgesine dokunun.",
        "hi" to "अपने कस्टम मिक्स सहेजें या किसी भी साउंड या कहानी पर दिल के आइकन पर टैप करें।",
    ),
    "No Listening History" to mapOf(
        "es" to "Sin historial de reproducción", "fr" to "Aucun historique d'écoute", "de" to "Kein Hörverlauf", "ja" to "再生履歴はまだありません",
        "ko" to "재생 기록이 없습니다", "it" to "Nessuna cronologia di ascolto", "ru" to "История прослушивания пуста", "id" to "Belum Ada Riwayat Dengar", "tr" to "Dinleme Geçmişi Yok", "hi" to "कोई सुनने का इतिहास नहीं",
    ),
    "Sounds, mixes, and bedtime stories you play will automatically appear here." to mapOf(
        "es" to "Los sonidos, mezclas y cuentos que reproduzcas aparecerán aquí automáticamente.",
        "fr" to "Les sons, mix et histoires que vous écoutez apparaîtront automatiquement ici.",
        "de" to "Klänge, Mixe und Schlafgeschichten, die du abspielst, erscheinen automatisch hier.",
        "ja" to "再生したサウンド、ミックス、スリープストーリーの履歴がここに自動で表示されます。",
        "ko" to "재생한 사운드, 믹스 및 수면 스토리가 여기에 자동으로 표시됩니다.",
        "it" to "I suoni, i mix e le storie che ascolti appariranno automaticamente qui.",
        "ru" to "Звуки, миксы и истории на ночь, которые вы включаете, появятся здесь автоматически.",
        "id" to "Suara, mix, dan cerita pengantar tidur yang Anda putar akan otomatis muncul di sini.",
        "tr" to "Dinlediğiniz sesler, karışımlar ve uyku hikayeleri otomatik olarak burada görünür.",
        "hi" to "आपके द्वारा चलाए गए साउंड्स, मिक्स और कहानियाँ अपने आप यहाँ दिखाई دیںगी।",
    ),
    "No Stories Found" to mapOf(
        "es" to "No se encontraron cuentos", "fr" to "Aucune histoire trouvée", "de" to "Keine Geschichten gefunden", "ja" to "ストーリーが見つかりません",
        "ko" to "스토리를 찾을 수 없습니다", "it" to "Nessuna storia trovata", "ru" to "Истории не найдены", "id" to "Cerita Tidak Ditemukan", "tr" to "Hikaye Bulunamadı", "hi" to "कोई कहानी नहीं मिली",
    ),
    "Try selecting another category filter above." to mapOf(
        "es" to "Prueba seleccionando otra categoría arriba.", "fr" to "Essayez de sélectionner une autre catégorie ci-dessus.", "de" to "Bitte wähle oben eine andere Kategorie aus.", "ja" to "上の別のカテゴリフィルターを選択してみてください。",
        "ko" to "위에서 다른 카테고리 필터를 선택해 보세요.", "it" to "Prova a selezionare un'altra categoria qui sopra.", "ru" to "Попробуйте выбрать другую категорию выше.", "id" to "Coba pilih filter kategori lain di atas.", "tr" to "Yukarıdan başka bir kategori seçmeyi deneyin.", "hi" to "ऊपर से कोई अन्य श्रेणी चुनने का प्रयास करें।",
    ),
    "Sleep Meditation" to mapOf(
        "es" to "Meditación de Sueño", "fr" to "Méditation Sommeil", "de" to "Schlafmeditation", "ja" to "睡眠瞑想",
        "ko" to "수면 명상", "it" to "Meditazione Sonno", "ru" to "Медитация сна", "id" to "Meditasi Tidur", "tr" to "Uyku Meditasyonu", "hi" to "नींद ध्यान",
    ),
    "Guided Meditation" to mapOf(
        "es" to "Meditación Guiada", "fr" to "Méditation Guidée", "de" to "Geführte Meditation", "ja" to "ガイド付き瞑想",
        "ko" to "가이드 명상", "it" to "Meditazione Guidata", "ru" to "С гидом", "id" to "Meditasi Terpandu", "tr" to "Rehberli Meditasyon", "hi" to "निर्देशित ध्यान",
    ),
    "Soft Fan" to mapOf(
        "pt-BR" to "Ventilador", "vi" to "Quạt êm", "es" to "Ventilador", "fr" to "Ventilateur",
        "de" to "Ventilator", "ja" to "扇風機", "ko" to "선풍기", "it" to "Ventilatore",
        "ru" to "Вентилятор", "id" to "Kipas Lembut", "tr" to "Vantilatör", "hi" to "पंखा",
    ),
    "Campfire" to mapOf(
        "pt-BR" to "Fogueira", "vi" to "Lửa trại", "es" to "Fogata", "fr" to "Feu de camp",
        "de" to "Lagerfeuer", "ja" to "焚き火", "ko" to "모닥불", "it" to "Falò",
        "ru" to "Костер", "id" to "Api Unggun", "tr" to "Kamp Ateşi", "hi" to "अलाव",
    ),
    "Ocean Waves" to mapOf(
        "pt-BR" to "Ondas do Mar", "vi" to "Sóng biển", "es" to "Olas del Mar", "fr" to "Vagues",
        "de" to "Meereswellen", "ja" to "波の音", "ko" to "파도 소리", "it" to "Onde del Mare",
        "ru" to "Волны океана", "id" to "Ombak Laut", "tr" to "Okyanus Dalgaları", "hi" to "समुद्र की लहरें",
    ),
    "Leaves" to mapOf(
        "pt-BR" to "Folhas", "vi" to "Lá xào xạc", "es" to "Hojas", "fr" to "Feuilles",
        "de" to "Blätter", "ja" to "木の葉", "ko" to "나뭇잎", "it" to "Foglie",
        "ru" to "Листва", "id" to "Dedaunan", "tr" to "Yapraklar", "hi" to "पत्तियाँ",
    ),
    "Rain on Tent" to mapOf(
        "pt-BR" to "Chuva na Barraca", "vi" to "Mưa trên lều", "es" to "Lluvia en Tienda", "fr" to "Pluie sur Tente",
        "de" to "Regen am Zelt", "ja" to "テントの雨", "ko" to "텐트 위 빗소리", "it" to "Pioggia su Tenda",
        "ru" to "Дождь по палатке", "id" to "Hujan di Tenda", "tr" to "Çadırda Yağmur", "hi" to "तंबू पर बारिश",
    ),
    "Crickets" to mapOf(
        "pt-BR" to "Grilos", "vi" to "Tiếng dế đêm", "es" to "Grillos", "fr" to "Grillons",
        "de" to "Grillen", "ja" to "コオロギ", "ko" to "귀뚜라미", "it" to "Grilli",
        "ru" to "Сверчки", "id" to "Jangkrik", "tr" to "Cırcır Böceği", "hi" to "झींगुर",
    ),
    "Soft Piano" to mapOf(
        "pt-BR" to "Piano Suave", "vi" to "Piano êm dịu", "es" to "Piano Suave", "fr" to "Piano Doux",
        "de" to "Sanftes Klavier", "ja" to "優しいピアノ", "ko" to "부드러운 피아노", "it" to "Piano Dolce",
        "ru" to "Нежное пианино", "id" to "Piano Lembut", "tr" to "Yumuşak Piyano", "hi" to "मधुर पियानो",
    ),
    "Background Music & Stories" to mapOf(
        "es" to "Música de Fondo y Cuentos", "fr" to "Musique de Fond & Histoires", "de" to "Hintergrundmusik & Geschichten", "ja" to "BGM＆スリープストーリー",
        "ko" to "배경 음악 및 수면 스토리", "it" to "Musica di Sottofondo e Storie", "ru" to "Фоновая музыка и истории", "id" to "Musik Latar & Cerita", "tr" to "Arka Plan Müziği ve Hikayeler", "hi" to "पृष्ठभूमि संगीत और कहानियाँ",
    ),
    "Ambient Background Sound" to mapOf(
        "es" to "Sonido Ambiente de Fondo", "fr" to "Son d'Ambiance", "de" to "Hintergrundklang", "ja" to "環境音BGM",
        "ko" to "배경 자연의 소리", "it" to "Suono Ambientale", "ru" to "Фоновый звук природы", "id" to "Suara Latar Belakang", "tr" to "Arka Plan Ortam Sesi", "hi" to "पृष्ठभूमि ध्वनि",
    ),
    "Background Volume" to mapOf(
        "es" to "Volumen de Fondo", "fr" to "Volume du Fond", "de" to "Hintergrundlautstärke", "ja" to "BGM音量",
        "ko" to "배경음 볼륨", "it" to "Volume Sottofondo", "ru" to "Громкость фона", "id" to "Volume Latar", "tr" to "Arka Plan Sesi", "hi" to "पृष्ठभूमि वॉल्यूम",
    ),
    "Switch Bedtime Story" to mapOf(
        "es" to "Cambiar Cuento", "fr" to "Changer d'Histoire", "de" to "Geschichte wechseln", "ja" to "ストーリーを切り替え",
        "ko" to "스토리 변경", "it" to "Cambia Storia", "ru" to "Сменить историю", "id" to "Ganti Cerita Tidur", "tr" to "Hikayeyi Değiştir", "hi" to "कहानी बदलें",
    ),
    "Benefits" to mapOf(
        "pt-BR" to "Benefícios",
        "vi" to "Quyền lợi",
        "es" to "Beneficios",
        "fr" to "Avantages",
        "de" to "Vorteile",
        "ja" to "特典詳細",
        "ko" to "혜택 안내",
        "it" to "Vantaggi",
        "ru" to "Бонусы",
        "id" to "Manfaat",
        "tr" to "Avantajlar",
        "hi" to "लाभ",
    ),
    "Free Gift" to mapOf(
        "pt-BR" to "Prêmio", "vi" to "Quà tặng", "es" to "Regalo", "fr" to "Cadeau",
        "de" to "Geschenk", "ja" to "無料特典", "ko" to "무료 선물", "it" to "Regalo",
        "ru" to "Подарок", "id" to "Hadiah", "tr" to "Hediye", "hi" to "मुफ़्त उपहार",
    ),
    "Cancel" to mapOf(
        "pt-BR" to "Cancelar", "vi" to "Đóng", "es" to "Cancelar", "fr" to "Fermer",
        "de" to "Schließen", "ja" to "閉じる", "ko" to "닫기", "it" to "Chiudi",
        "ru" to "Закрыть", "id" to "Tutup", "tr" to "Kapat", "hi" to "बंद करें",
    ),
    "Best Value" to mapOf(
        "pt-BR" to "Melhor Valor", "vi" to "Tiết kiệm nhất", "es" to "Mejor Valor", "fr" to "Meilleure Offre",
        "de" to "Bester Preis", "ja" to "一番お得", "ko" to "최고 가성비", "it" to "Miglior Valore",
        "ru" to "Выгодно", "id" to "Paling Hemat", "tr" to "En Avantajlı", "hi" to "सबसे किफायती",
    ),
    "Connection to Google Play was interrupted. Please try again." to mapOf(
        "pt-BR" to "A conexão com o Google Play foi interrompida. Tente novamente.",
        "vi" to "Kết nối tới Google Play bị gián đoạn. Vui lòng thử lại.",
        "es" to "Se interrumpió la conexión con Google Play. Inténtalo de nuevo.",
        "fr" to "La connexion à Google Play a été interrompue. Veuillez réessayer.",
        "de" to "Die Verbindung zu Google Play wurde unterbrochen. Bitte erneut versuchen.",
        "ja" to "Google Playとの接続が中断されました。もう一度お試しください。",
        "ko" to "Google Play 연결이 중단되었습니다. 다시 시도해 주세요.",
        "it" to "La connessione a Google Play è stata interrotta. Riprova.",
        "ru" to "Соединение с Google Play прервано. Повторите попытку.",
        "id" to "Koneksi ke Google Play terputus. Silakan coba lagi.",
        "tr" to "Google Play bağlantısı kesildi. Lütfen tekrar deneyin.",
        "hi" to "Google Play से कनेक्शन टूट गया। कृपया पुनः प्रयास करें।",
    ),
    "Daily Wisdom" to mapOf(
        "pt-BR" to "Sabedoria do Dia",
        "vi" to "Châm ngôn sống",
        "es" to "Sabiduría del Día",
        "fr" to "Sagesse du Jour",
        "de" to "Lebensweisheit",
        "ja" to "心の格言",
        "ko" to "오늘의 명언",
        "it" to "Saggezza del Giorno",
        "ru" to "Мудрость дня",
        "id" to "Kata Bijak",
        "tr" to "Günün Sözü",
        "hi" to "जीवन सूत्र",
    ),
    "Exhausted" to mapOf(
        "pt-BR" to "Exausto", "vi" to "Mệt lả", "es" to "Agotado", "fr" to "Épuisé",
        "de" to "Erschöpft", "ja" to "ぐったり", "ko" to "매우 지침", "it" to "Esausto",
        "ru" to "Измотан", "id" to "Sangat Lelah", "tr" to "Bitkin", "hi" to "बहुत थका हुआ",
    ),
    "Off (Voice Only)" to mapOf(
        "pt-BR" to "Desativado (Apenas Voz)", "vi" to "Tắt (Chỉ giọng đọc)", "es" to "Apagado (Solo voz)", "fr" to "Désactivé (Voix seule)",
        "de" to "Aus (Nur Stimme)", "ja" to "オフ（音声のみ）", "ko" to "끔 (음성만)", "it" to "Disattivato (Solo voce)",
        "ru" to "Выкл. (только голос)", "id" to "Mati (Hanya Suara)", "tr" to "Kapalı (Yalnızca Ses)", "hi" to "बंद (केवल आवाज़)",
    ),
    "🌧️ Light Rain" to mapOf(
        "pt-BR" to "🌧️ Chuva Leve", "vi" to "🌧️ Mưa nhỏ", "es" to "🌧️ Lluvia suave", "fr" to "🌧️ Pluie légère",
        "de" to "🌧️ Leichter Regen", "ja" to "🌧️ 小雨", "ko" to "🌧️ 가랑비", "it" to "🌧️ Pioggia leggera",
        "ru" to "🌧️ Легкий дождь", "id" to "🌧️ Hujan Ringan", "tr" to "🌧️ Hafif Yağmur", "hi" to "🌧️ हल्की बारिश",
    ),
    "🌊 Calm Sea" to mapOf(
        "pt-BR" to "🌊 Mar Calmo", "vi" to "🌊 Biển êm", "es" to "🌊 Mar en calma", "fr" to "🌊 Mer calme",
        "de" to "🌊 Ruhiges Meer", "ja" to "🌊 穏やかな海", "ko" to "🌊 잔잔한 바다", "it" to "🌊 Mare calmo",
        "ru" to "🌊 Спокойное море", "id" to "🌊 Laut Tenang", "tr" to "🌊 Sakin Deniz", "hi" to "🌊 शांत समुद्र",
    ),
    "🔥 Campfire" to mapOf(
        "pt-BR" to "🔥 Fogueira", "vi" to "🔥 Lửa trại", "es" to "🔥 Fogata", "fr" to "🔥 Feu de camp",
        "de" to "🔥 Lagerfeuer", "ja" to "🔥 焚き火", "ko" to "🔥 모닥불", "it" to "🔥 Falò",
        "ru" to "🔥 Костер", "id" to "🔥 Api Unggun", "tr" to "🔥 Kamp Ateşi", "hi" to "🔥 अलाव",
    ),
    "🏠 Rain on Roof" to mapOf(
        "pt-BR" to "🏠 Chuva no Telhado", "vi" to "🏠 Mưa trên mái nhà", "es" to "🏠 Lluvia en el tejado", "fr" to "🏠 Pluie sur le toit",
        "de" to "🏠 Regen auf dem Dach", "ja" to "🏠 屋根を打つ雨", "ko" to "🏠 지붕 위 빗소리", "it" to "🏠 Pioggia sul tetto",
        "ru" to "🏠 Дождь по крыше", "id" to "🏠 Hujan di Atap", "tr" to "🏠 Çatıda Yağmur", "hi" to "🏠 छत पर बारिश",
    ),
    "✨ Soft White Noise" to mapOf(
        "pt-BR" to "✨ Ruído Branco Suave", "vi" to "✨ Tiếng ồn trắng dịu nhẹ", "es" to "✨ Ruido blanco suave", "fr" to "✨ Bruit blanc doux",
        "de" to "✨ Sanftes weißes Rauschen", "ja" to "✨ ソフトホワイトノイズ", "ko" to "✨ 부드러운 백색소음", "it" to "✨ Rumore bianco delicato",
        "ru" to "✨ Мягкий белый шум", "id" to "✨ Derau Putih Lembut", "tr" to "✨ Yumuşak Beyaz Gürültü", "hi" to "✨ कोमल व्हाइट नॉइज़",
    ),
    "Saved" to mapOf(
        "pt-BR" to "Salvo", "vi" to "Đã lưu", "es" to "Guardado", "fr" to "Enregistré",
        "de" to "Gespeichert", "ja" to "保存済み", "ko" to "저장됨", "it" to "Salvato",
        "ru" to "Сохранено", "id" to "Tersimpan", "tr" to "Kaydedildi", "hi" to "सहेजा गया",
    ),
    "Mix Save" to mapOf(
        "pt-BR" to "Salvar Mix", "vi" to "Lưu phối âm", "es" to "Guardar mezcla", "fr" to "Sauver mix",
        "de" to "Mix speichern", "ja" to "ミックス保存", "ko" to "믹스 저장", "it" to "Salva mix",
        "ru" to "Сохранить микс", "id" to "Simpan Mix", "tr" to "Miksi Kaydet", "hi" to "मिक्स सहेजें",
    ),
    "Sleep Story" to mapOf(
        "pt-BR" to "História de Dormir", "vi" to "Truyện kể ru ngủ", "es" to "Historia para dormir", "fr" to "Histoire pour dormir",
        "de" to "Schlafgeschichte", "ja" to "スリープストーリー", "ko" to "수면 스토리", "it" to "Storia della buonanotte",
        "ru" to "Сказка на ночь", "id" to "Cerita Pengantar Tidur", "tr" to "Uyku Hikayesi", "hi" to "नींद की कहानी",
    ),
    "MIN" to mapOf(
        "pt-BR" to "MIN", "vi" to "PHÚT", "es" to "MIN", "fr" to "MIN",
        "de" to "MIN", "ja" to "分", "ko" to "분", "it" to "MIN",
        "ru" to "МИН", "id" to "MNT", "tr" to "DK", "hi" to "मिनट",
    ),
    "Mix" to mapOf(
        "pt-BR" to "Mix", "vi" to "Bản phối", "es" to "Mezcla", "fr" to "Mix",
        "de" to "Mix", "ja" to "ミックス", "ko" to "믹스", "it" to "Mix",
        "ru" to "Микс", "id" to "Mix", "tr" to "Miks", "hi" to "मिक्स",
    ),
    "White Noise" to mapOf(
        "pt-BR" to "Ruído Branco", "vi" to "Tiếng ồn trắng", "es" to "Ruido blanco", "fr" to "Bruit blanc",
        "de" to "Weißes Rauschen", "ja" to "ホワイトノイズ", "ko" to "백색소음", "it" to "Rumore bianco",
        "ru" to "Белый шум", "id" to "Derau Putih", "tr" to "Beyaz Gürültü", "hi" to "व्हाइट नॉइज़",
    ),
    "Nature" to mapOf(
        "pt-BR" to "Natureza", "vi" to "Thiên nhiên", "es" to "Naturaleza", "fr" to "Nature",
        "de" to "Natur", "ja" to "自然", "ko" to "자연", "it" to "Natura",
        "ru" to "Природа", "id" to "Alam", "tr" to "Doğa", "hi" to "प्रकृति",
    ),
    "Rain" to mapOf(
        "pt-BR" to "Chuva", "vi" to "Tiếng mưa", "es" to "Lluvia", "fr" to "Pluie",
        "de" to "Regen", "ja" to "雨", "ko" to "비", "it" to "Pioggia",
        "ru" to "Дождь", "id" to "Hujan", "tr" to "Yağmur", "hi" to "बारिश",
    ),
    "Water" to mapOf(
        "pt-BR" to "Água", "vi" to "Mặt nước", "es" to "Agua", "fr" to "Eau",
        "de" to "Wasser", "ja" to "水", "ko" to "물", "it" to "Acqua",
        "ru" to "Вода", "id" to "Air", "tr" to "Su", "hi" to "पानी",
    ),
    "Animals" to mapOf(
        "pt-BR" to "Animais", "vi" to "Động vật", "es" to "Animales", "fr" to "Animaux",
        "de" to "Tiere", "ja" to "動物", "ko" to "동물", "it" to "Animali",
        "ru" to "Животные", "id" to "Hewan", "tr" to "Hayvanlar", "hi" to "जीव-जंतु",
    ),
    "City" to mapOf(
        "pt-BR" to "Cidade", "vi" to "Phố thị", "es" to "Ciudad", "fr" to "Ville",
        "de" to "Stadt", "ja" to "都市", "ko" to "도시", "it" to "Città",
        "ru" to "Город", "id" to "Kota", "tr" to "Şehir", "hi" to "शहर",
    ),
    "Instruments" to mapOf(
        "pt-BR" to "Instrumentos", "vi" to "Nhạc cụ", "es" to "Instrumentos", "fr" to "Instruments",
        "de" to "Instrumente", "ja" to "楽器", "ko" to "악기", "it" to "Strumenti",
        "ru" to "Инструменты", "id" to "Instrumen", "tr" to "Enstrümanlar", "hi" to "वाद्ययंत्र",
    ),
    "Meditate" to mapOf(
        "pt-BR" to "Meditar", "vi" to "Thiền", "es" to "Meditar", "fr" to "Méditer",
        "de" to "Meditieren", "ja" to "瞑想", "ko" to "명상", "it" to "Meditare",
        "ru" to "Медитация", "id" to "Meditasi", "tr" to "Meditasyon", "hi" to "ध्यान",
    ),
    "Custom" to mapOf(
        "pt-BR" to "Personalizado", "vi" to "Tùy chỉnh", "es" to "Personalizado", "fr" to "Personnalisé",
        "de" to "Eigene", "ja" to "カスタム", "ko" to "사용자 지정", "it" to "Personalizzato",
        "ru" to "Свой микс", "id" to "Kustom", "tr" to "Özel", "hi" to "कस्टम",
    ),
    "Custom Mix" to mapOf(
        "pt-BR" to "Mix Personalizado", "vi" to "Bản phối tùy chỉnh", "es" to "Mezcla personalizada", "fr" to "Mix personnalisé",
        "de" to "Eigener Mix", "ja" to "カスタムミックス", "ko" to "나만의 믹스", "it" to "Mix personalizzato",
        "ru" to "Свой микс", "id" to "Mix Kustom", "tr" to "Özel Miks", "hi" to "कस्टम मिक्स",
    ),
    "Music" to mapOf(
        "pt-BR" to "Música", "vi" to "Âm nhạc", "es" to "Música", "fr" to "Musique",
        "de" to "Musik", "ja" to "音楽", "ko" to "음악", "it" to "Musica",
        "ru" to "Музыка", "id" to "Musik", "tr" to "Müzik", "hi" to "संगीत",
    ),
    "Story" to mapOf(
        "pt-BR" to "Histórias", "vi" to "Truyện ngủ", "es" to "Historias", "fr" to "Histoires",
        "de" to "Geschichten", "ja" to "ストーリー", "ko" to "스토리", "it" to "Storie",
        "ru" to "Истории", "id" to "Cerita", "tr" to "Hikayeler", "hi" to "कहानियाँ",
    ),
    "Stories" to mapOf(
        "pt-BR" to "Histórias", "vi" to "Truyện ngủ", "es" to "Historias", "fr" to "Histoires",
        "de" to "Geschichten", "ja" to "ストーリー", "ko" to "스토리", "it" to "Storie",
        "ru" to "Истории", "id" to "Cerita", "tr" to "Hikayeler", "hi" to "कहानियाँ",
    ),
    "Soundscape" to mapOf(
        "pt-BR" to "Paisagem Sonora", "vi" to "Không gian âm thanh", "es" to "Paisaje sonoro", "fr" to "Paysage sonore",
        "de" to "Klangwelt", "ja" to "サウンドスケープ", "ko" to "사운드스케이프", "it" to "Paesaggio sonoro",
        "ru" to "Звуковая сцена", "id" to "Suasana Suara", "tr" to "Ses Manzarası", "hi" to "साउंडस्केप",
    ),
    "Featured" to mapOf(
        "pt-BR" to "Destaques", "vi" to "Nổi bật", "es" to "Destacados", "fr" to "À la une",
        "de" to "Empfohlen", "ja" to "おすすめ", "ko" to "추천", "it" to "In evidenza",
        "ru" to "Рекомендуемое", "id" to "Unggulan", "tr" to "Öne Çıkanlar", "hi" to "विशेष",
    ),
    "Relax" to mapOf(
        "pt-BR" to "Relaxar", "vi" to "Thư giãn", "es" to "Relajación", "fr" to "Relaxation",
        "de" to "Entspannung", "ja" to "リラックス", "ko" to "릴랙스", "it" to "Relax",
        "ru" to "Релакс", "id" to "Relaksasi", "tr" to "Rahatlama", "hi" to "आराम",
    ),
    "Bedtime" to mapOf(
        "pt-BR" to "Hora de Dormir", "vi" to "Trước giờ ngủ", "es" to "Hora de dormir", "fr" to "Coucher",
        "de" to "Schlafenszeit", "ja" to "おやすみ前", "ko" to "취침 시간", "it" to "Ora di dormire",
        "ru" to "Перед сном", "id" to "Waktu Tidur", "tr" to "Uyku Vakti", "hi" to "सोने का समय",
    ),
    "SOUND" to mapOf(
        "pt-BR" to "Som", "vi" to "Âm thanh", "es" to "Sonido", "fr" to "Son",
        "de" to "Klang", "ja" to "サウンド", "ko" to "사운드", "it" to "Suono",
        "ru" to "Звук", "id" to "Suara", "tr" to "Ses", "hi" to "ध्वनि",
    ),
    "PRESET" to mapOf(
        "pt-BR" to "Ambiente", "vi" to "Không gian mẫu", "es" to "Ambiente", "fr" to "Ambiance",
        "de" to "Szene", "ja" to "シーン", "ko" to "장면", "it" to "Ambiente",
        "ru" to "Сцена", "id" to "Suasana", "tr" to "Sahne", "hi" to "दृश्य",
    ),
    "PODCAST" to mapOf(
        "pt-BR" to "História de Dormir", "vi" to "Truyện kể ru ngủ", "es" to "Historia para dormir", "fr" to "Histoire pour dormir",
        "de" to "Schlafgeschichte", "ja" to "スリープストーリー", "ko" to "수면 스토리", "it" to "Storia della buonanotte",
        "ru" to "Сказка на ночь", "id" to "Cerita Pengantar Tidur", "tr" to "Uyku Hikayesi", "hi" to "नींद की कहानी",
    ),
    "USER_MIX" to mapOf(
        "pt-BR" to "Mix Personalizado", "vi" to "Bản phối đã lưu", "es" to "Mezcla guardada", "fr" to "Mix enregistré",
        "de" to "Gespeicherter Mix", "ja" to "保存済みミックス", "ko" to "저장된 믹스", "it" to "Mix salvato",
        "ru" to "Сохранённый микс", "id" to "Mix Tersimpan", "tr" to "Kayıtlı Miks", "hi" to "सहेजा गया मिक्स",
    ),
    "3-Hour Ad-Free & VIP Active" to mapOf(
        "es" to "3 Horas Sin Anuncios y VIP Activo", "fr" to "3h Sans Pub & VIP Actif", "de" to "3 Std. Werbefrei & VIP Aktiv", "ja" to "3時間広告なし＆VIP有効中",
        "ko" to "3시간 광고 제거 & VIP 활성화됨", "it" to "3 Ore Senza Pubblicità & VIP Attivo", "ru" to "3 часа без рекламы и VIP активны", "id" to "3 Jam Bebas Iklan & VIP Aktif", "tr" to "3 Saat Reklamsız & VIP Aktif", "hi" to "3 घंटे विज्ञापन-मुक्त और VIP सक्रिय",
    ),
    "Free 3-Hour Ad-Free & VIP Pass" to mapOf(
        "es" to "Pase Gratis de 3h Sin Anuncios y VIP", "fr" to "Pass Gratuit 3h Sans Pub & VIP", "de" to "Gratis 3-Stunden Werbefrei & VIP-Pass", "ja" to "無料3時間広告なし＆VIPパス",
        "ko" to "무료 3시간 광고 제거 & VIP 패스", "it" to "Pass Gratuito 3h Senza Pubblicità & VIP", "ru" to "Бесплатный VIP и 3 часа без рекламы", "id" to "Pass Gratis 3 Jam Bebas Iklan & VIP", "tr" to "Ücretsiz 3 Saat Reklamsız & VIP Paketi", "hi" to "मुफ़्त 3-घंटे विज्ञापन-मुक्त और VIP पास",
    ),
    "Remaining time" to mapOf(
        "es" to "Tiempo restante", "fr" to "Temps restant", "de" to "Verbleibende Zeit", "ja" to "残り時間",
        "ko" to "남은 시간", "it" to "Tempo rimanente", "ru" to "Осталось времени", "id" to "Sisa waktu", "tr" to "Kalan süre", "hi" to "शेष समय",
    ),
    "Watch 20 rewarded ads to remove all ads & unlock VIP for 3 hours" to mapOf(
        "es" to "Mira 20 anuncios para eliminar anuncios y desbloquear VIP por 3 horas",
        "fr" to "Regardez 20 publicités pour supprimer les pubs et débloquer le VIP pendant 3 heures",
        "de" to "Sieh dir 20 Werbevideos an, um alle Anzeigen zu entfernen & VIP für 3 Stunden freizuschalten",
        "ja" to "リワード広告を20回視聴すると、3時間すべての広告が消えVIPが解放されます",
        "ko" to "보상형 광고 20개를 시청하면 3시간 동안 모든 광고 제거 및 VIP가 잠금 해제됩니다",
        "it" to "Guarda 20 annunci per rimuovere tutte le pubblicità e sbloccare il VIP per 3 ore",
        "ru" to "Посмотрите 20 роликов, чтобы отключить рекламу и открыть VIP на 3 часа",
        "id" to "Tonton 20 iklan berhadiah untuk menghapus iklan & membuka VIP selama 3 jam",
        "tr" to "Tüm reklamları kaldırmak ve 3 saat VIP açmak için 20 ödüllü reklam izleyin",
        "hi" to "3 घंटे के लिए सभी विज्ञापन हटाने और VIP अनलॉक करने के लिए 20 विज्ञापन देखें",
    ),
    "Watch Ad" to mapOf(
        "es" to "Ver Anuncio", "fr" to "Voir Pub", "de" to "Video ansehen", "ja" to "広告を見る",
        "ko" to "광고 보기", "it" to "Guarda Spot", "ru" to "Смотреть", "id" to "Tonton Iklan", "tr" to "Reklam İzle", "hi" to "विज्ञापन देखें",
    ),
    "Extend +3h" to mapOf(
        "es" to "Extender +3h", "fr" to "Prolonger +3h", "de" to "+3 Std. verlängern", "ja" to "+3時間延長",
        "ko" to "+3시간 연장", "it" to "Estendi +3h", "ru" to "Продлить +3ч", "id" to "Perpanjang +3j", "tr" to "+3 Saat Uzat", "hi" to "+3 घंटे बढ़ाएं",
    ),
    "Unlocked 3 Hours of Ad-Free & Full VIP Access!" to mapOf(
        "es" to "¡Desbloqueaste 3 Horas Sin Anuncios y Acceso VIP Total!",
        "fr" to "3 Heures Sans Pub et Accès VIP Complet débloqués !",
        "de" to "3 Stunden Werbefrei & Vollständiger VIP-Zugang freigeschaltet!",
        "ja" to "3時間の広告なし＆全VIPアクセスが解放されました！",
        "ko" to "3시간 광고 제거 및 전체 VIP 액세스가 잠금 해제되었습니다!",
        "it" to "Sbloccate 3 Ore Senza Pubblicità e Accesso VIP Completo!",
        "ru" to "Открыто 3 часа без рекламы и полный VIP-доступ!",
        "id" to "Berhasil membuka 3 Jam Bebas Iklan & Akses VIP Penuh!",
        "tr" to "3 Saat Reklamsız ve Tam VIP Erişimi Açıldı!",
        "hi" to "3 घंटे का विज्ञापन-मुक्त और पूर्ण VIP एक्सेस अनलॉक हो गया!",
    ),
    "Reward earned! Progress: %d/%d toward 3h Ad-Free & VIP" to mapOf(
        "es" to "¡Recompensa obtenida! Progreso: %d/%d para 3h Sin Anuncios y VIP",
        "fr" to "Récompense obtenue ! Progression : %d/%d vers 3h Sans Pub & VIP",
        "de" to "Belohnung erhalten! Fortschritt: %d/%d für 3 Std. Werbefrei & VIP",
        "ja" to "報酬を獲得しました！ 進捗: %d/%d（3時間広告なし＆VIPまで）",
        "ko" to "보상 획득! 진행률: %d/%d (3시간 광고 제거 & VIP)",
        "it" to "Ricompensa ottenuta! Progresso: %d/%d per 3h Senza Pubblicità & VIP",
        "ru" to "Награда получена! Прогресс: %d/%d до 3ч без рекламы и VIP",
        "id" to "Hadiah diterima! Progres: %d/%d menuju 3 jam Bebas Iklan & VIP",
        "tr" to "Ödül kazanıldı! İlerleme: %d/%d (3 Saat Reklamsız & VIP)",
        "hi" to "इनाम मिला! प्रगति: %d/%d (3 घंटे विज्ञापन-मुक्त और VIP)",
    ),
    "Unlocked \"%s\" for 12h! (%d/%d toward 3h Ad-Free)" to mapOf(
        "es" to "¡\"%s\" desbloqueado por 12h! (%d/%d para 3h Sin Anuncios)",
        "fr" to "« %s » débloqué pour 12h ! (%d/%d vers 3h Sans Pub)",
        "de" to "„%s“ für 12 Std. freigeschaltet! (%d/%d für 3 Std. Werbefrei)",
        "ja" to "「%s」を12時間解放しました！（3時間広告なしまで %d/%d）",
        "ko" to "\"%s\" 12시간 잠금 해제! (3시간 광고 제거까지 %d/%d)",
        "it" to "\"%s\" sbloccato per 12h! (%d/%d per 3h Senza Pubblicità)",
        "ru" to "«%s» открыто на 12 ч! (%d/%d до 3 ч без рекламы)",
        "id" to "\"%s\" dibuka selama 12 jam! (%d/%d menuju 3 jam Bebas Iklan)",
        "tr" to "\"%s\" 12 saatliğine açıldı! (3 Saat Reklamsız için %d/%d)",
        "hi" to "\"%s\" 12 घंटे के लिए अनलॉक! (3 घंटे विज्ञापन-मुक्त के लिए %d/%d)",
    ),
    "Ad is loading, please try again in a few seconds." to mapOf(
        "es" to "El anuncio se está cargando, inténtalo de nuevo en unos segundos.",
        "fr" to "La publicité charge, veuillez réessayer dans quelques secondes.",
        "de" to "Anzeige wird geladen, bitte versuche es in wenigen Sekunden erneut.",
        "ja" to "広告を読み込んでいます。数秒後にもう一度お試しください。",
        "ko" to "광고를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.",
        "it" to "Caricamento annuncio in corso, riprova tra qualche secondo.",
        "ru" to "Реклама загружается, попробуйте снова через несколько секунд.",
        "id" to "Iklan sedang dimuat, silakan coba lagi dalam beberapa detik.",
        "tr" to "Reklam yükleniyor, lütfen birkaç saniye sonra tekrar deneyin.",
        "hi" to "विज्ञापन लोड हो रहा है, कृपया कुछ सेकंड बाद पुनः प्रयास करें।",
    ),
    "Watch 1 Ad to Unlock Now" to mapOf(
        "es" to "Ver 1 Anuncio para Desbloquear", "fr" to "Voir 1 Pub pour Débloquer", "de" to "1 Video ansehen & freischalten", "ja" to "広告を1回見て今すぐ解放",
        "ko" to "광고 1개 보고 지금 잠금 해제", "it" to "Guarda 1 Annuncio per Sbloccare", "ru" to "Посмотреть 1 рекламу и открыть", "id" to "Tonton 1 Iklan untuk Membuka", "tr" to "Şimdi Açmak İçin 1 Reklam İzle", "hi" to "अभी अनलॉक करने के लिए 1 विज्ञापन देखें",
    ),
    "Upgrade to Somni VIP (No Ads Forever)" to mapOf(
        "es" to "Mejorar a Somni VIP (Sin Anuncios)", "fr" to "Passer à Somni VIP (Sans Pub)", "de" to "Upgrade auf Somni VIP (Ohne Werbung)", "ja" to "Somni VIPにアップグレード（広告なし）",
        "ko" to "Somni VIP 업그레이드 (영구 광고 제거)", "it" to "Passa a Somni VIP (Senza Pubblicità)", "ru" to "Перейти на Somni VIP (без рекламы)", "id" to "Upgrade ke Somni VIP (Tanpa Iklan)", "tr" to "Somni VIP'ye Yükselt (Reklamsız)", "hi" to "Somni VIP में अपग्रेड करें (विज्ञापन-मुक्त)",
    ),
    "3-Hour Ad-Free & VIP Progress" to mapOf(
        "es" to "Progreso para 3h Sin Anuncios y VIP", "fr" to "Progression 3h Sans Pub & VIP", "de" to "Fortschritt für 3 Std. Werbefrei & VIP", "ja" to "3時間広告なし＆VIPの進捗",
        "ko" to "3시간 광고 제거 & VIP 진행률", "it" to "Progresso 3h Senza Pubblicità & VIP", "ru" to "Прогресс 3ч без рекламы и VIP", "id" to "Progres 3 Jam Bebas Iklan & VIP", "tr" to "3 Saat Reklamsız & VIP İlerlemesi", "hi" to "3 घंटे विज्ञापन-मुक्त और VIP प्रगति",
    ),
    "Watch 1 short video to unlock this item for 12 hours, or collect 20 rewarded ads to unlock 3 hours of 100% Ad-Free & Full VIP access." to mapOf(
        "es" to "Mira 1 video corto para desbloquear este elemento por 12 horas, o acumula 20 anuncios para ganar 3 horas Sin Anuncios y VIP Total.",
        "fr" to "Regardez 1 courte vidéo pour débloquer cet élément pendant 12h, ou cumulez 20 pubs pour 3h Sans Pub et VIP Complet.",
        "de" to "Sieh dir 1 kurzes Video an, um diesen Inhalt für 12 Std. freizuschalten, oder sammle 20 Videos für 3 Std. Werbefrei & VIP.",
        "ja" to "短い動画を1回見てこのアイテムを12時間解放するか、20回視聴して3時間の広告なし＆全VIPを解放しましょう。",
        "ko" to "짧은 동영상 1개를 시청하여 이 항목을 12시간 동안 잠금 해제하거나, 20개를 모아 3시간 광고 제거 및 전체 VIP를 활성화하세요.",
        "it" to "Guarda 1 breve video per sbloccare questo elemento per 12 ore, o accumula 20 annunci per 3 ore Senza Pubblicità e VIP.",
        "ru" to "Посмотрите 1 короткое видео, чтобы открыть этот контент на 12 часов, или соберите 20 просмотров для 3 часов без рекламы и VIP.",
        "id" to "Tonton 1 video singkat untuk membuka item ini selama 12 jam, atau kumpulkan 20 iklan untuk membuka 3 jam Bebas Iklan & VIP Penuh.",
        "tr" to "Bu içeriği 12 saatliğine açmak için 1 kısa video izleyin veya 3 saat Reklamsız & Tam VIP için 20 reklam tamamlayın.",
        "hi" to "इस आइटम को 12 घंटे के लिए अनलॉक करने हेतु 1 छोटा वीडियो देखें, या 3 घंटे विज्ञापन-मुक्त और पूर्ण VIP के लिए 20 विज्ञापन पूरे करें।",
    ),
    "Somni VIP Active" to mapOf(
        "es" to "Somni VIP Activo", "fr" to "Somni VIP Actif", "de" to "Somni VIP Aktiv", "ja" to "Somni VIP 有効",
        "ko" to "Somni VIP 사용 중", "it" to "Somni VIP Attivo", "ru" to "Somni VIP Активен", "id" to "Somni VIP Aktif", "tr" to "Somni VIP Aktif", "hi" to "Somni VIP सक्रिय",
    ),
    "Somni VIP Membership" to mapOf(
        "es" to "Membresía Somni VIP", "fr" to "Abonnement Somni VIP", "de" to "Somni VIP-Mitgliedschaft", "ja" to "Somni VIP メンバーシップ",
        "ko" to "Somni VIP 멤버십", "it" to "Abbonamento Somni VIP", "ru" to "Подписка Somni VIP", "id" to "Keanggotaan Somni VIP", "tr" to "Somni VIP Üyeliği", "hi" to "Somni VIP सदस्यता",
    ),
    "All sounds, stories & ad-free mode unlocked" to mapOf(
        "es" to "Todos los sonidos, historias y modo sin anuncios activos", "fr" to "Tous les sons, histoires et mode sans pub débloqués",
        "de" to "Alle Klänge, Geschichten & Werbefreiheit freigeschaltet", "ja" to "全サウンド・ストーリー＆広告なしモード解放済み",
        "ko" to "모든 사운드, 스토리 및 광고 제거 활성화됨", "it" to "Tutti i suoni, storie e modalità senza pubblicità attivi",
        "ru" to "Все звуки, сказки и режим без рекламы открыты", "id" to "Semua suara, cerita & mode bebas iklan aktif",
        "tr" to "Tüm sesler, hikayeler ve reklamsız mod açık", "hi" to "सभी ध्वनियाँ, कहानियाँ और विज्ञापन-मुक्त मोड अनलॉक",
    ),
    "Unlock all sounds, stories & remove ads forever" to mapOf(
        "es" to "Desbloquea todo y elimina los anuncios para siempre", "fr" to "Débloquez tout et supprimez les publicités",
        "de" to "Alle Inhalte freischalten & Werbung dauerhaft entfernen", "ja" to "全コンテンツを解放＆広告を永久に非表示",
        "ko" to "모든 콘텐츠 잠금 해제 및 영구 광고 제거", "it" to "Sblocca tutto e rimuovi la pubblicità per sempre",
        "ru" to "Откройте весь контент и отключите рекламу навсегда", "id" to "Buka semua konten & hapus iklan selamanya",
        "tr" to "Tüm içerikleri açın ve reklamları kalıcı olarak kaldırın", "hi" to "सभी सामग्री अनलॉक करें और विज्ञापन हमेशा के लिए हटाएं",
    ),
    "Sleep Deeper Every Night with Somni VIP" to mapOf(
        "es" to "Duerme Más Profundamente Cada Noche con Somni VIP", "fr" to "Dormez Plus Profondément Chaque Nuit avec Somni VIP",
        "de" to "Schlafe Jede Nacht Tiefer mit Somni VIP", "ja" to "Somni VIPで毎晩もっと深い眠りへ",
        "ko" to "Somni VIP와 함께 매일 밤 더 깊은 잠에 빠져보세요", "it" to "Dormi Più Profondamente Ogni Notte con Somni VIP",
        "ru" to "Спите глубже каждую ночь с Somni VIP", "id" to "Tidur Lebih Nyenyak Setiap Malam dengan Somni VIP",
        "tr" to "Somni VIP ile Her Gece Daha Derin Uyuyun", "hi" to "Somni VIP के साथ हर रात गहरी नींद सोएं",
    ),
    "Choose a VIP plan below or watch 20 rewarded ads to unlock 3 hours of Ad-Free & Full VIP for free." to mapOf(
        "es" to "Elige un plan VIP abajo o mira 20 anuncios para desbloquear 3h Sin Anuncios y VIP gratis.",
        "fr" to "Choisissez un forfait VIP ci-dessous ou regardez 20 pubs pour débloquer 3h Sans Pub & VIP gratuitement.",
        "de" to "Wähle unten ein VIP-Abo oder sieh dir 20 Videos an, um 3 Std. Werbefrei & VIP kostenlos zu aktivieren.",
        "ja" to "以下のVIPプランを選択するか、広告を20回見て3時間の広告なし＆VIPを無料で解放しましょう。",
        "ko" to "아래에서 VIP 플랜을 선택하거나 보상형 광고 20개를 시청하여 3시간 무료 VIP를 잠금 해제하세요.",
        "it" to "Scegli un piano VIP qui sotto o guarda 20 annunci per sbloccare gratis 3h Senza Pubblicità e VIP.",
        "ru" to "Выберите тариф VIP ниже или посмотрите 20 роликов для 3 часов бесплатного VIP без рекламы.",
        "id" to "Pilih paket VIP di bawah atau tonton 20 iklan untuk membuka 3 jam Bebas Iklan & VIP gratis.",
        "tr" to "Aşağıdan bir VIP planı seçin veya ücretsiz 3 saat Reklamsız & VIP için 20 reklam izleyin.",
        "hi" to "नीचे एक VIP प्लान चुनें या मुफ़्त में 3 घंटे विज्ञापन-मुक्त और VIP अनलॉक करने के लिए 20 विज्ञापन देखें।",
    ),
    "Yearly VIP" to mapOf(
        "es" to "VIP Anual", "fr" to "VIP Annuel", "de" to "Jahres-VIP", "ja" to "年額VIP",
        "ko" to "연간 VIP", "it" to "VIP Annuale", "ru" to "VIP на год", "id" to "VIP Tahunan", "tr" to "Yıllık VIP", "hi" to "वार्षिक VIP",
    ),
    "Monthly VIP" to mapOf(
        "es" to "VIP Mensual", "fr" to "VIP Mensuel", "de" to "Monats-VIP", "ja" to "月額VIP",
        "ko" to "월간 VIP", "it" to "VIP Mensile", "ru" to "VIP на месяц", "id" to "VIP Bulanan", "tr" to "Aylık VIP", "hi" to "मासिक VIP",
    ),
    "Lifetime VIP" to mapOf(
        "es" to "VIP Vitalicio", "fr" to "VIP à Vie", "de" to "Lebenslanges VIP", "ja" to "永久VIP（買い切り）",
        "ko" to "평생 VIP 소장", "it" to "VIP a Vita", "ru" to "VIP Навсегда", "id" to "VIP Seumur Hidup", "tr" to "Ömür Boyu VIP", "hi" to "लाइफटाइम VIP",
    ),
    "Subscribe Yearly VIP" to mapOf(
        "es" to "Suscribirse a VIP Anual", "fr" to "S'abonner à VIP Annuel", "de" to "Jahres-VIP Abonnieren", "ja" to "年額VIPに登録する",
        "ko" to "연간 VIP 구독하기", "it" to "Abbonati a VIP Annuale", "ru" to "Оформить VIP на год", "id" to "Langganan VIP Tahunan", "tr" to "Yıllık VIP Abonesi Ol", "hi" to "वार्षिक VIP सब्सक्राइब करें",
    ),
    "Subscribe Monthly VIP" to mapOf(
        "es" to "Suscribirse a VIP Mensual", "fr" to "S'abonner à VIP Mensuel", "de" to "Monats-VIP Abonnieren", "ja" to "月額VIPに登録する",
        "ko" to "월간 VIP 구독하기", "it" to "Abbonati a VIP Mensile", "ru" to "Оформить VIP на месяц", "id" to "Langganan VIP Bulanan", "tr" to "Aylık VIP Abonesi Ol", "hi" to "मासिक VIP सब्सक्राइब करें",
    ),
    "Unlock Lifetime VIP" to mapOf(
        "es" to "Desbloquear VIP Vitalicio", "fr" to "Débloquer VIP à Vie", "de" to "Lebenslanges VIP Freischalten", "ja" to "永久VIPをアンロック",
        "ko" to "평생 VIP 잠금 해제", "it" to "Sblocca VIP a Vita", "ru" to "Открыть VIP Навсегда", "id" to "Buka VIP Seumur Hidup", "tr" to "Ömür Boyu VIP'yi Aç", "hi" to "लाइफटाइम VIP अनलॉक करें",
    ),
    "Restore Purchases" to mapOf(
        "es" to "Restaurar Compras", "fr" to "Restaurer les Achats", "de" to "Käufe Wiederherstellen", "ja" to "購入を復元",
        "ko" to "구매 내역 복원", "it" to "Ripristina Acquisti", "ru" to "Восстановить покупки", "id" to "Pulihkan Pembelian", "tr" to "Satın Alımları Geri Yükle", "hi" to "खरीदारी बहाल करें",
    ),
    "Terms & Privacy" to mapOf(
        "es" to "Términos y Privacidad", "fr" to "Conditions & Confidentialité", "de" to "AGB & Datenschutz", "ja" to "利用規約とプライバシー",
        "ko" to "이용약관 및 개인정보", "it" to "Termini e Privacy", "ru" to "Условия и конфиденциальность", "id" to "Ketentuan & Privasi", "tr" to "Koşullar ve Gizlilik", "hi" to "शर्तें और गोपनीयता",
    ),
    "Select your preferred display language" to mapOf(
        "es" to "Selecciona tu idioma de visualización preferido", "fr" to "Sélectionnez votre langue d'affichage préférée",
        "de" to "Wähle deine bevorzugte Anzeigesprache", "ja" to "表示する言語を選択してください",
        "ko" to "원하는 앱 표시 언어를 선택하세요", "it" to "Seleziona la lingua di visualizzazione preferita",
        "ru" to "Выберите предпочитаемый язык интерфейса", "id" to "Pilih bahasa tampilan pilihan Anda",
        "tr" to "Tercih ettiğiniz görünüm dilini seçin", "hi" to "अपनी पसंदीदा प्रदर्शन भाषा चुनें",
    ),
    "CHOOSE YOUR VIP PLAN (CLEAR & TRANSPARENT)" to mapOf(
        "es" to "ELIGE TU PLAN VIP (CLARO Y TRANSPARENTE)", "fr" to "CHOISISSEZ VOTRE FORFAIT VIP (CLAIR & TRANSPARENT)",
        "de" to "WÄHLE DEIN VIP-PAKET (KLAR & TRANSPARENT)", "ja" to "VIPプランを選択（明確・安心の料金体系）",
        "ko" to "VIP 플랜 선택 (투명하고 명확한 안내)", "it" to "SCEGLI IL TUO PIANO VIP (CHIARO E TRASPARENTE)",
        "ru" to "ВЫБЕРИТЕ ТАРИФ VIP (ПРОЗРАЧНЫЕ УСЛОВИЯ)", "id" to "PILIH PAKET VIP ANDA (JELAS & TRANSPARAN)",
        "tr" to "VIP PLANINIZI SEÇİN (AÇIK VE ŞEFFAF)", "hi" to "अपना VIP प्लान चुनें (स्पष्ट और पारदर्शी)",
    ),
    "Billed once a year · Best savings for 365 nights of deep sleep" to mapOf(
        "es" to "Facturado una vez al año · Mayor ahorro para 365 noches de sueño profundo",
        "fr" to "Facturé une fois par an · Meilleure économie pour 365 nuits de sommeil",
        "de" to "Jährliche Abrechnung · Beste Ersparnis für 365 Nächte Tiefschlaf",
        "ja" to "年1回のお支払い · 365日の快眠に一番お得なプラン",
        "ko" to "연 1회 결제 · 365일 숙면을 위한 최고의 가성비 플랜",
        "it" to "Fatturato una volta all'anno · Massimo risparmio per 365 notti",
        "ru" to "Оплата раз в год · Максимальная выгода на 365 ночей",
        "id" to "Ditagih sekali setahun · Penghematan terbaik untuk 365 malam",
        "tr" to "Yılda bir kez faturalandırılır · 365 gece için en yüksek tasarruf",
        "hi" to "साल में एक बार बिल · 365 रातों की गहरी नींद के लिए सबसे किफायती",
    ),
    "✓ Cancel anytime on Google Play · No commitment" to mapOf(
        "es" to "✓ Cancela cuando quieras en Google Play · Sin compromiso",
        "fr" to "✓ Annulez à tout moment sur Google Play · Sans engagement",
        "de" to "✓ Jederzeit bei Google Play kündbar · Keine Bindung",
        "ja" to "✓ Google Playでいつでも解約可能 · 縛りなし",
        "ko" to "✓ Google Play에서 언제든 해지 가능 · 약정 없음",
        "it" to "✓ Disdici quando vuoi su Google Play · Nessun vincolo",
        "ru" to "✓ Отмена в любой момент в Google Play · Без обязательств",
        "id" to "✓ Batalkan kapan saja di Google Play · Tanpa ikatan",
        "tr" to "✓ Google Play'den istediğiniz zaman iptal edin · Taahhüt yok",
        "hi" to "✓ Google Play पर कभी भी रद्द करें · कोई बाध्यता नहीं",
    ),
    "MOST POPULAR · SAVE 58%" to mapOf(
        "es" to "MÁS POPULAR · AHORRA 58%", "fr" to "PLUS POPULAIRE · -58%", "de" to "BELIEBT · 58% SPAREN", "ja" to "一番人気 · 58%お得",
        "ko" to "인기 1위 · 58% 할인", "it" to "PIÙ POPOLARE · -58%", "ru" to "ПОПУЛЯРНЫЙ · ВЫГОДА 58%", "id" to "TERPOPULER · HEMAT 58%",
        "tr" to "EN POPÜLER · %58 TASARRUF", "hi" to "सबसे लोकप्रिय · 58% बचाएं",
    ),
    "Pay month-by-month · Great for short-term flexibility" to mapOf(
        "es" to "Pago mes a mes · Ideal para flexibilidad a corto plazo",
        "fr" to "Paiement mensuel · Idéal pour une flexibilité à court terme",
        "de" to "Monatliche Zahlung · Ideal für kurzfristige Flexibilität",
        "ja" to "月々のお支払い · 短期間から気軽に始められるプラン",
        "ko" to "매월 결제 · 단기 이용에 적합한 유연한 플랜",
        "it" to "Pagamento mensile · Ottimo per la massima flessibilità",
        "ru" to "Помесячная оплата · Удобный вариант на короткий срок",
        "id" to "Bayar bulanan · Cocok untuk fleksibilitas jangka pendek",
        "tr" to "Aydan aya ödeme · Kısa vadeli esneklik için ideal",
        "hi" to "महीने-दर-महीने भुगतान · लचीले उपयोग के लिए उत्तम",
    ),
    "✓ Cancel anytime with 1 tap · No lock-in contract" to mapOf(
        "es" to "✓ Cancela con 1 toque · Sin contrato de permanencia",
        "fr" to "✓ Résiliation en 1 clic · Aucun contrat d'engagement",
        "de" to "✓ Mit 1 Tipp kündbar · Keine Vertragsbindung",
        "ja" to "✓ ワンタップでいつでも解約 · 契約期間の縛りなし",
        "ko" to "✓ 터치 한 번으로 언제든 해지 · 위약금 없음",
        "it" to "✓ Disdici con 1 tocco · Nessun contratto vincolante",
        "ru" to "✓ Отмена в 1 касание · Никаких скрытых условий",
        "id" to "✓ Batalkan dengan 1 ketukan · Tanpa kontrak mengikat",
        "tr" to "✓ Tek dokunuşla istediğiniz an iptal · Sözleşme yok",
        "hi" to "✓ 1 टैप में कभी भी रद्द करें · कोई अनुबंध नहीं",
    ),
    "FLEXIBLE PLAN" to mapOf(
        "es" to "PLANO FLEXIBLE", "fr" to "FORFAIT FLEXIBLE", "de" to "FLEXIBEL", "ja" to "柔軟なプラン",
        "ko" to "유연한 플랜", "it" to "PIANO FLESSIBILE", "ru" to "ГИБКИЙ ТАРИФ", "id" to "PAKET FLEKSIBEL",
        "tr" to "ESNEK PLAN", "hi" to "लचीला प्लान",
    ),
    "One-time payment · Own full VIP access permanently forever" to mapOf(
        "es" to "Pago único · Acceso VIP completo para siempre",
        "fr" to "Paiement unique · Accès VIP complet à vie",
        "de" to "Einmalige Zahlung · Lebenslanger Vollzugriff auf VIP",
        "ja" to "1回限りの買い切り · 全VIP機能を永久にご利用可能",
        "ko" to "1회 결제 · 모든 VIP 기능을 평생 영구 소장",
        "it" to "Pagamento unico · Accesso VIP completo per sempre",
        "ru" to "Разовый платеж · Полный VIP-доступ навсегда",
        "id" to "Sekali bayar · Miliki akses VIP penuh selamanya",
        "tr" to "Tek seferlik ödeme · Ömür boyu kalıcı VIP erişimi",
        "hi" to "एक बार भुगतान · हमेशा के लिए पूर्ण VIP एक्सेस पाएं",
    ),
    "✓ Never billed again · Zero recurring subscription fees" to mapOf(
        "es" to "✓ Nunca más se te cobrará · Cero cargos recurrentes",
        "fr" to "✓ Plus jamais facturé · Zéro frais d'abonnement récurrent",
        "de" to "✓ Nie wieder zahlen · Keine laufenden Abo-Gebühren",
        "ja" to "✓ 追加料金は一切なし · 定期購読の自動更新もありません",
        "ko" to "✓ 추가 결제 없음 · 정기 구독료 평생 0원",
        "it" to "✓ Nessun altro addebito · Zero costi ricorrenti",
        "ru" to "✓ Никаких повторных списаний · Без абонентской платы",
        "id" to "✓ Tidak pernah ditagih lagi · Tanpa biaya langganan bulanan",
        "tr" to "✓ Bir daha ücret alınmaz · Yinelenen abonelik ücreti yok",
        "hi" to "✓ फिर कभी बिल नहीं · कोई आवर्ती सदस्यता शुल्क नहीं",
    ),
    "BEST DEAL · PAY ONCE" to mapOf(
        "es" to "MEJOR OFERTA · PAGO ÚNICO", "fr" to "MEILLEURE OFFRE · 1 SEUL PAIEMENT", "de" to "BESTES ANGEBOT · 1× ZAHLEN", "ja" to "最高のお得度 · 買い切り",
        "ko" to "최고 혜택 · 1회 결제", "it" to "MIGLIOR OFFERTA · PAGA 1 VOLTA", "ru" to "ЛУЧШАЯ ЦЕНА · РАЗОВЫЙ ПЛАТЕЖ", "id" to "PENAWARAN TERBAIK · SEKALI BAYAR",
        "tr" to "EN İYİ FIRSAT · TEK ÖDEME", "hi" to "सर्वश्रेष्ठ डील · एक बार भुगतान",
    ),
    "100% Secure Google Play Checkout · Cancel Anytime · No Commitment" to mapOf(
        "es" to "Pago 100% Seguro en Google Play · Cancela Cuando Quieras · Sin Compromiso",
        "fr" to "Paiement 100% Sécurisé via Google Play · Annulable à Tout Moment · Sans Engagement",
        "de" to "100% Sicher über Google Play · Jederzeit kündbar · Keine Verpflichtung",
        "ja" to "Google Playによる100%安全な決済 · いつでも解約可能 · 契約の縛りなし",
        "ko" to "Google Play 100% 안전 결제 · 언제든 해지 가능 · 약정 없음",
        "it" to "Pagamento Sicuro al 100% con Google Play · Disdici Quando Vuoi · Nessun Vincolo",
        "ru" to "100% безопасная оплата через Google Play · Отмена в любой момент · Без обязательств",
        "id" to "Pembayaran 100% Aman via Google Play · Batalkan Kapan Saja · Tanpa Ikatan",
        "tr" to "Google Play ile %100 Güvenli Ödeme · İstediğiniz An İptal · Taahhüt Yok",
        "hi" to "Google Play द्वारा 100% सुरक्षित भुगतान · कभी भी रद्द करें · कोई बाध्यता नहीं",
    ),
    "Subscription & Payment Security Guarantee" to mapOf(
        "es" to "Garantía de Suscripción y Seguridad de Pago",
        "fr" to "Garantie d'Abonnement et de Sécurité de Paiement",
        "de" to "Abo-Transparenz & Zahlungssicherheits-Garantie",
        "ja" to "安心のサブスクリプション＆決済セキュリティ保証",
        "ko" to "구독 투명성 및 결제 보안 보증 안내",
        "it" to "Garanzia di Abbonamento e Sicurezza dei Pagamenti",
        "ru" to "Гарантия прозрачности подписки и безопасности оплаты",
        "id" to "Jaminan Langganan & Keamanan Pembayaran",
        "tr" to "Abonelik ve Ödeme Güvenliği Garantisi",
        "hi" to "सदस्यता और भुगतान सुरक्षा गारंटी",
    ),
    "Cancel Anytime · Zero Commitment" to mapOf(
        "es" to "Cancela Cuando Quieras · Cero Compromiso",
        "fr" to "Annulez à Tout Moment · Zéro Engagement",
        "de" to "Jederzeit Kündbar · Null Bindung",
        "ja" to "いつでも解約可能 · 契約期間の縛りなし",
        "ko" to "언제든 해지 가능 · 약정 및 위약금 없음",
        "it" to "Disdici Quando Vuoi · Zero Vincoli",
        "ru" to "Отмена в любой момент · Без обязательств",
        "id" to "Batalkan Kapan Saja · Tanpa Komitmen",
        "tr" to "İstediğiniz Zaman İptal · Sıfır Taahhüt",
        "hi" to "कभी भी रद्द करें · शून्य बाध्यता",
    ),
    "Manage or cancel Monthly/Yearly plans anytime with 1 tap in Google Play → Subscriptions. You keep full VIP access until the end of your paid billing period." to mapOf(
        "es" to "Gestiona o cancela tu plan Mensual/Anual en cualquier momento con 1 toque en Google Play → Suscripciones. Conservas el acceso VIP hasta el final del periodo pagado.",
        "fr" to "Gérez ou annulez votre forfait Mensuel/Annuel à tout moment en 1 clic dans Google Play → Abonnements. Vous conservez le VIP jusqu'à la fin de la période payée.",
        "de" to "Verwalte oder kündige Monats-/Jahres-Abos jederzeit mit 1 Tipp in Google Play → Abos. Dein VIP-Zugang bleibt bis zum Ende des bezahlten Zeitraums aktiv.",
        "ja" to "Google Playの「定期購入」からワンタップでいつでも管理・解約できます。解約後も支払い済み期間の終了までVIP特典をそのままご利用いただけます。",
        "ko" to "Google Play → 정기 결제에서 터치 한 번으로 언제든 관리하거나 해지할 수 있습니다. 해지 후에도 결제된 기간이 끝날 때까지 VIP 혜택이 유지됩니다.",
        "it" to "Gestisci o annulla i piani Mensile/Annuale in qualsiasi momento con 1 tocco in Google Play → Abbonamenti. Mantieni il VIP fino al termine del periodo pagato.",
        "ru" to "Управляйте подпиской или отменяйте её в 1 касание в Google Play → Подписки. Доступ VIP сохраняется до конца оплаченного периода.",
        "id" to "Kelola atau batalkan paket Bulanan/Tahunan kapan saja dengan 1 ketukan di Google Play → Langganan. Akses VIP tetap aktif hingga akhir periode berjalan.",
        "tr" to "Aylık/Yıllık planları Google Play → Abonelikler bölümünden tek dokunuşla istediğiniz an iptal edebilirsiniz. Ödenmiş dönem sonuna kadar VIP erişiminiz devam eder.",
        "hi" to "Google Play → सदस्यताएँ में 1 टैप से कभी भी मासिक/वार्षिक प्लान प्रबंधित या रद्द करें। भुगतान की गई अवधि समाप्त होने तक आपका VIP एक्सेस बना रहेगा।",
    ),
    "100% Encrypted Google Play Payment Security" to mapOf(
        "es" to "Seguridad de Pago 100% Cifrada por Google Play",
        "fr" to "Sécurité de Paiement 100% Chiffrée par Google Play",
        "de" to "100% Verschlüsselte Zahlungssicherheit über Google Play",
        "ja" to "Google Playによる100%暗号化された安全な決済",
        "ko" to "Google Play 100% 암호화 안전 결제 시스템",
        "it" to "Sicurezza di Pagamento Crittografata al 100% da Google Play",
        "ru" to "100% защита платежей через систему Google Play",
        "id" to "Keamanan Pembayaran Terenkripsi 100% oleh Google Play",
        "tr" to "Google Play ile %100 Şifreli Ödeme Güvenliği",
        "hi" to "Google Play द्वारा 100% एन्क्रिप्टेड भुगतान सुरक्षा",
    ),
    "All transactions are processed directly by Google Play's official billing system. We never see or store your card, e-wallet, or bank details." to mapOf(
        "es" to "Todas las transacciones son procesadas directamente por el sistema oficial de Google Play. Nunca vemos ni almacenamos los datos de tu tarjeta o banco.",
        "fr" to "Toutes les transactions sont traitées directement par le système officiel de Google Play. Nous ne voyons ni ne stockons jamais vos données bancaires.",
        "de" to "Alle Zahlungen werden direkt über das offizielle Google Play-Abrechnungssystem abgewickelt. Wir speichern niemals deine Karten- oder Bankdaten.",
        "ja" to "すべての決済はGoogle Playの公式システムで直接処理されます。アプリ側でお客様のカード情報や銀行口座情報を保存・閲覧することは一切ありません。",
        "ko" to "모든 결제는 Google Play 공식 결제 시스템에서 직접 처리됩니다. 앱은 사용자의 카드, 전자지갑 또는 은행 정보를 절대 저장하거나 열람하지 않습니다.",
        "it" to "Tutte le transazioni sono elaborate direttamente dal sistema ufficiale di Google Play. Non memorizziamo mai i dati della tua carta o conto bancario.",
        "ru" to "Все платежи обрабатываются напрямую официальной системой Google Play. Приложение не имеет доступа к данным ваших карт или счетов.",
        "id" to "Semua transaksi diproses langsung oleh sistem resmi Google Play. Kami tidak pernah melihat atau menyimpan detail kartu maupun rekening Anda.",
        "tr" to "Tüm işlemler doğrudan Google Play resmi ödeme sistemi tarafından işlenir. Kart veya banka bilgilerinizi asla görmeyiz ve saklamayız.",
        "hi" to "सभी लेन-देन सीधे Google Play के आधिकारिक बिलिंग सिस्टम द्वारा संसाधित होते हैं। हम आपके कार्ड या बैंक विवरण कभी नहीं देखते या सहेजते हैं।",
    ),
    "No Hidden Fees & Free Multi-Device Restore" to mapOf(
        "es" to "Sin Cargos Ocultos y Restauración Gratis en Varios Dispositivos",
        "fr" to "Aucun Frais Caché & Restauration Multi-Appareils Gratuite",
        "de" to "Keine Versteckten Gebühren & Kostenlose Wiederherstellung",
        "ja" to "隠れた費用なし＆機種変更時の無料復元",
        "ko" to "숨겨진 요금 없음 & 기기 변경 시 무료 복원",
        "it" to "Nessun Costo Nascosto e Ripristino Gratuito Multi-Dispositivo",
        "ru" to "Без скрытых комиссий и бесплатное восстановление",
        "id" to "Tanpa Biaya Tersembunyi & Pemulihan Gratis Antar Perangkat",
        "tr" to "Gizli Ücret Yok ve Cihazlar Arası Ücretsiz Geri Yükleme",
        "hi" to "कोई छिपा शुल्क नहीं और मुफ़्त मल्टी-डिवाइस रीस्टोर",
    ),
    "Yearly & Monthly plans auto-renew at the listed price unless canceled at least 24h before renewal. Lifetime VIP is billed once forever. Restore VIP free on any Android device signed into your Google account." to mapOf(
        "es" to "Los planes Anual y Mensual se renuevan automáticamente al precio indicado a menos que se cancelen 24h antes. El VIP Vitalicio se paga una sola vez. Restaura gratis en cualquier Android.",
        "fr" to "Les forfaits Annuel et Mensuel se renouvellent automatiquement au tarif affiché sauf annulation 24h avant. Le VIP à Vie est payé une seule fois. Restauration gratuite sur tout appareil Android.",
        "de" to "Jahres- & Monats-Abos verlängern sich automatisch zum angezeigten Preis (Kündigung bis 24h vorher möglich). Lebenslanges VIP ist eine Einmalzahlung. Kostenlose Wiederherstellung auf jedem Android-Gerät.",
        "ja" to "年額・月額プランは更新日の24時間前までに解約しない限り表示価格で自動更新されます。永久VIPは1回のお支払いのみです。同じGoogleアカウントのどのAndroid端末でも無料で復元できます。",
        "ko" to "연간 및 월간 플랜은 갱신 24시간 전까지 해지하지 않으면 표시된 가격으로 자동 갱신됩니다. 평생 VIP는 1회만 결제됩니다. 동일한 Google 계정의 모든 Android 기기에서 무료로 복원할 수 있습니다.",
        "it" to "I piani Annuale e Mensile si rinnovano automaticamente al prezzo indicato salvo disdetta 24h prima. Il VIP a Vita si paga una sola volta. Ripristina gratis su qualsiasi dispositivo Android.",
        "ru" to "Годовой и месячный тарифы продлеваются автоматически по указанной цене, если не отменены за 24 часа. Тариф «Навсегда» оплачивается один раз. Бесплатное восстановление на любом Android.",
        "id" to "Paket Tahunan & Bulanan diperpanjang otomatis sesuai harga tertera kecuali dibatalkan 24 jam sebelumnya. VIP Seumur Hidup hanya dibayar sekali. Pulihkan gratis di perangkat Android mana pun.",
        "tr" to "Yıllık ve Aylık planlar, yenilemeden en az 24 saat önce iptal edilmediği sürece belirtilen fiyattan otomatik yenilenir. Ömür Boyu VIP tek ödemedir. Google hesabınızla her Android cihazda ücretsiz geri yükleyin.",
        "hi" to "वार्षिक और मासिक प्लान नवीनीकरण से 24 घंटे पहले रद्द न करने पर सूचीबद्ध मूल्य पर स्वतः नवीनीकृत होते हैं। लाइफटाइम VIP का केवल एक बार भुगतान होता है। किसी भी Android डिवाइस पर मुफ़्त रीस्टोर करें।",
    ),
    "Tap to View VIP Plans & Rewarded Ad Guide" to mapOf(
        "es" to "Toca para Ver Planes VIP y Guía de Anuncios",
        "fr" to "Appuyez pour Voir les Offres VIP et le Guide Pub",
        "de" to "Tippen für VIP-Pläne & Werbevideo-Anleitung",
        "ja" to "タップしてVIPプランとリワード広告ガイドを表示",
        "ko" to "탭하여 VIP 플랜 및 보상형 광고 안내 보기",
        "it" to "Tocca per Vedere i Piani VIP e la Guida Annunci",
        "ru" to "Нажмите, чтобы открыть тарифы VIP и правила наград",
        "id" to "Ketuk untuk Melihat Paket VIP & Panduan Iklan",
        "tr" to "VIP Planlarını ve Ödüllü Reklam Rehberini Görmek İçin Dokunun",
        "hi" to "VIP प्लान और रिवॉर्ड विज्ञापन गाइड देखने के लिए टैप करें",
    ),
    "✓ Watch 20 rewarded ads: Unlock 3 hours of 100% Ad-Free (no Banner or Full Ads) & Full VIP access for free." to mapOf(
        "es" to "✓ Mira 20 anuncios premiados: Desbloquea 3 horas 100% Sin Anuncios (sin banners ni pantalla completa) y Acceso VIP Total gratis.",
        "fr" to "✓ Regardez 20 publicités : Débloquez gratuitement 3 heures 100% Sans Pub (ni bannière ni plein écran) et l'Accès VIP Complet.",
        "de" to "✓ 20 Werbevideos ansehen: Schalte kostenlos 3 Stunden 100% Werbefrei (keine Banner- oder Vollbild-Ads) & Vollständiges VIP frei.",
        "ja" to "✓ リワード広告を20回視聴：3時間100%広告なし（バナー・全画面広告なし）＆全VIP機能を無料で解放。",
        "ko" to "✓ 보상형 광고 20개 시청: 3시간 동안 100% 광고 제거(배너 및 전면 광고 없음) 및 전체 VIP 무료 잠금 해제.",
        "it" to "✓ Guarda 20 annunci: Sblocca gratis 3 ore 100% Senza Pubblicità (niente banner o schermo intero) e Accesso VIP Completo.",
        "ru" to "✓ Посмотрите 20 роликов: Бесплатно откройте 3 часа полностью без рекламы (без баннеров и полноэкранной рекламы) и полный VIP.",
        "id" to "✓ Tonton 20 iklan berhadiah: Buka gratis 3 jam 100% Bebas Iklan (tanpa banner & iklan layar penuh) + Akses VIP Penuh.",
        "tr" to "✓ 20 ödüllü reklam izleyin: 3 saat %100 Reklamsız (Banner ve Tam Ekran reklam yok) ve Tam VIP erişimini ücretsiz açın.",
        "hi" to "✓ 20 रिवॉर्ड विज्ञापन देखें: 3 घंटे के लिए 100% विज्ञापन-मुक्त (कोई बैनर या फुल विज्ञापन नहीं) और पूर्ण VIP मुफ़्त अनलॉक करें।",
    ),
    "✓ Watch 1 ad on any locked item: Immediately unlock that VIP sound or story for 12 hours + earn +1 toward the 20-ad pass." to mapOf(
        "es" to "✓ Mira 1 anuncio en cualquier contenido bloqueado: Desbloquea ese sonido o historia VIP por 12 horas + suma +1 para el pase de 20 anuncios.",
        "fr" to "✓ Regardez 1 pub sur un contenu verrouillé : Débloquez ce son ou cette histoire VIP pendant 12h + gagnez +1 vers le pass de 20 pubs.",
        "de" to "✓ 1 Video bei gesperrtem Inhalt ansehen: Schalte diesen VIP-Klang oder diese Geschichte sofort für 12 Std. frei + erhalte +1 für den 20er-Pass.",
        "ja" to "✓ ロックされた項目で広告を1回視聴：そのVIPサウンドやストーリーを即座に12時間解放＋20回パスに+1カウント。",
        "ko" to "✓ 잠긴 항목에서 광고 1개 시청: 해당 VIP 사운드나 스토리를 즉시 12시간 잠금 해제 + 20개 패스에 +1 적립.",
        "it" to "✓ Guarda 1 annuncio su un elemento bloccato: Sblocca subito quel suono o storia VIP per 12 ore + ottieni +1 per il pass da 20 annunci.",
        "ru" to "✓ Посмотрите 1 рекламу на закрытом контенте: Сразу откройте этот VIP-звук или сказку на 12 часов + получите +1 к 20 просмотрам.",
        "id" to "✓ Tonton 1 iklan pada item terkunci: Buka langsung suara atau cerita VIP tersebut selama 12 jam + tambah +1 ke progres 20 iklan.",
        "tr" to "✓ Kilitli bir içerikte 1 reklam izleyin: O VIP sesi veya hikayeyi hemen 12 saatliğine açın + 20 reklam paketine +1 kazanın.",
        "hi" to "✓ किसी भी लॉक आइटम पर 1 विज्ञापन देखें: उस VIP ध्वनि या कहानी को तुरंत 12 घंटे के लिए अनलॉक करें + 20-विज्ञापन पास में +1 जोड़ें।",
    ),
    "✓ Unlimited views & auto-saved progress: Accumulate anytime at your own pace and extend +3 hours endlessly." to mapOf(
        "es" to "✓ Vistas ilimitadas y progreso guardado: Acumula a tu ritmo en cualquier momento y extiende +3 horas sin límites.",
        "fr" to "✓ Vues illimitées et progression sauvegardée : Cumulez à votre rythme à tout moment et prolongez de +3 heures sans limite.",
        "de" to "✓ Unbegrenzte Aufrufe & automatisch gespeicherter Fortschritt: Jederzeit im eigenen Tempo sammeln und beliebig oft um +3 Std. verlängern.",
        "ja" to "✓ 視聴回数無制限＆進捗自動保存：いつでも自分のペースで貯められ、何度でも+3時間延長可能。",
        "ko" to "✓ 무제한 시청 및 진행률 자동 저장: 언제든 원하는 속도로 적립하고 무제한으로 +3시간씩 연장하세요.",
        "it" to "✓ Visualizzazioni illimitate e progresso salvato: Accumula quando vuoi al tuo ritmo ed estendi di +3 ore senza limiti.",
        "ru" to "✓ Безлимитные просмотры и автосохранение: Копите в удобном темпе в любое время и продлевайте на +3 часа без ограничений.",
        "id" to "✓ Tontonan tanpa batas & progres tersimpan otomatis: Kumpulkan kapan saja dan perpanjang +3 jam tanpa batas.",
        "tr" to "✓ Sınırsız izleme ve otomatik kayıtlı ilerleme: İstediğiniz zaman biriktirin ve sınırsızca +3 saat uzatın.",
        "hi" to "✓ असीमित व्यू और ऑटो-सेव प्रगति: कभी भी अपनी गति से जमा करें और असीमित रूप से +3 घंटे बढ़ाएं।",
    ),
)
private val LOWERCASE_PHRASE_TRANSLATIONS: Map<String, Map<String, String>> by lazy {
    EXTRA_PHRASE_TRANSLATIONS.entries.associate { (k, v) -> k.lowercase(Locale.ROOT) to v }
}
private val SOUNDS_COUNT_REGEX = Regex("^(\\d+)\\s+sounds?$", RegexOption.IGNORE_CASE)
fun translateTextForLanguage(en: String, languageCode: String): String {
    if (en.isBlank()) return en
    val norm = UserPreferencesRepository.normalizeLanguage(languageCode)
    if (norm == UserPreferencesRepository.LANGUAGE_EN) return en
    EXTRA_PHRASE_TRANSLATIONS[en]?.get(norm)?.let { return it }
    LOWERCASE_PHRASE_TRANSLATIONS[en.trim().lowercase(Locale.ROOT)]?.get(norm)?.let { return it }
    SOUNDS_COUNT_REGEX.matchEntire(en.trim())?.groupValues?.getOrNull(1)?.let { count ->
        return formatDynamicSoundsCount(count, norm)
    }
    return en
}
@Composable
@ReadOnlyComposable
fun tr(
    en: String,
    ptBr: String,
    vi: String = en,
): String {
    val lang = LocalAppLanguage.current
    return when (lang) {
        UserPreferencesRepository.LANGUAGE_EN -> en
        UserPreferencesRepository.LANGUAGE_PT_BR -> ptBr
        UserPreferencesRepository.LANGUAGE_VI -> vi
        else -> translateTextForLanguage(en, lang)
    }
}
@Composable
@ReadOnlyComposable
fun tr(en: String): String {
    val lang = LocalAppLanguage.current
    return translateTextForLanguage(en, lang)
}
private fun formatDynamicSoundsCount(count: String, lang: String): String = when (lang) {
    UserPreferencesRepository.LANGUAGE_PT_BR -> "$count sons"
    UserPreferencesRepository.LANGUAGE_VI -> "$count âm thanh"
    UserPreferencesRepository.LANGUAGE_ES -> "$count sonidos"
    UserPreferencesRepository.LANGUAGE_FR -> "$count sons"
    UserPreferencesRepository.LANGUAGE_DE -> "$count Klänge"
    UserPreferencesRepository.LANGUAGE_JA -> "$count サウンド"
    UserPreferencesRepository.LANGUAGE_KO -> "$count 사운드"
    UserPreferencesRepository.LANGUAGE_IT -> "$count suoni"
    UserPreferencesRepository.LANGUAGE_RU -> "$count звука"
    UserPreferencesRepository.LANGUAGE_ID -> "$count suara"
    UserPreferencesRepository.LANGUAGE_TR -> "$count ses"
    UserPreferencesRepository.LANGUAGE_HI -> "$count ध्वनियाँ"
    else -> "$count sounds"
}
/**
 * Dynamically updates the active Locale, [LocalConfiguration], and [LocalContext] using an
 * Activity-preserving [ContextWrapper] so all `stringResource(...)` and `tr(...)` calls
 * recompose immediately when the user switches language.
 */
@Composable
fun ProvideAppLocale(
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val normalized = UserPreferencesRepository.normalizeLanguage(languageCode)
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current

    val locale = remember(normalized) {
        UserPreferencesRepository.localeForLanguage(normalized)
    }

    val localizedConfiguration = remember(baseConfiguration, normalized, locale) {
        val singleLocaleList = LocaleList(locale)
        LocaleList.setDefault(singleLocaleList)
        Locale.setDefault(locale)
        Configuration(baseConfiguration).apply {
            setLocales(singleLocaleList)
            setLayoutDirection(locale)
        }
    }

    val localizedContextWrapper = remember(baseContext, normalized, localizedConfiguration) {
        val configContext = baseContext.createConfigurationContext(localizedConfiguration)
        object : ContextWrapper(baseContext) {
            override fun getResources(): Resources = configContext.resources
            override fun getAssets(): AssetManager = configContext.assets
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides normalized,
        LocalConfiguration provides localizedConfiguration,
        LocalContext provides localizedContextWrapper,
        content = content,
    )
}
data class SupportedAppLanguage(
    val code: String,
    val flagEmoji: String,
    val nativeName: String,
    val englishSubtitle: String,
)
