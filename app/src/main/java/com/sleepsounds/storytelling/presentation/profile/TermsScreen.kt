package com.sleepsounds.storytelling.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.ContentSafeArea
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.LocalAppLanguage
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth

private val LEGAL_DOCUMENT_SECTIONS: List<LegalDocumentEntry> = listOf(
    LegalDocumentEntry.DocumentHeader("Deep Sleep — Terms & Conditions"),
    LegalDocumentEntry.BodyParagraph(
        "These terms and conditions apply to the Deep Sleep app for mobile devices, together " +
            "with any related services operated by Huu Tri (collectively, the \"Application\"). " +
            "Huu Tri is hereby referred to as the \"Service Provider\"."
    ),
    LegalDocumentEntry.BodyParagraph(
        "By downloading or using the Application, you agree to these Terms and Conditions. You " +
            "should read them carefully before using the Application."
    ),
    LegalDocumentEntry.SectionHeader("License to use the Application"),
    LegalDocumentEntry.BodyParagraph(
        "Subject to your compliance with these Terms, the Service Provider grants you a limited, " +
            "non-exclusive, non-transferable, revocable license to install and use the Application " +
            "on a mobile device for personal or internal business purposes. You may not reproduce, " +
            "distribute, modify, create derivative works from, reverse engineer, decompile, or " +
            "disassemble the Application, except as and only to the extent that such activity is " +
            "expressly permitted by applicable law."
    ),
    LegalDocumentEntry.SectionHeader("Intellectual Property"),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider retains all intellectual property rights in the Application, " +
            "including its code, design, trademarks, service marks, trade names, logos, and " +
            "branding (the \"IP\"). Nothing in these Terms grants you any license or right to use " +
            "the Service Provider's trademarks, logos, or branding for any purpose. You agree not " +
            "to remove, alter, or obscure any copyright, trademark, or other proprietary notices " +
            "displayed in or on the Application."
    ),
    LegalDocumentEntry.SectionHeader("Termination"),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider may suspend your access to the Application or services if you " +
            "materially breach these Terms. The Service Provider will provide you with written " +
            "notice of the breach and, where the breach is capable of cure, you will have 14 days " +
            "from receipt of notice to remedy the breach. If you fail to cure the breach within " +
            "that period, the Service Provider may terminate your access."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider may suspend or terminate your access immediately without notice if " +
            "you violate applicable law, infringe intellectual property rights, or engage in " +
            "activity that could cause harm to other users or the Service Provider."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Upon termination, your right to use the Application will end and you must delete all " +
            "copies from your devices."
    ),
    LegalDocumentEntry.BodyParagraph(
        "By accessing and using this Application, you represent that you are legally permitted to " +
            "use it in your jurisdiction. You must be at least 16 years of age (the age of digital " +
            "consent in your jurisdiction) to use the Application. If you are below 16, a parent or " +
            "legal guardian must review and accept these Terms on your behalf."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Unauthorized copying, modification of the Application, any part of the Application, or the " +
            "Service Provider's trademarks is strictly prohibited. Any attempts to extract the " +
            "source code of the Application, translate the Application into other languages, or " +
            "create derivative versions are not permitted. All trademarks, copyrights, database " +
            "rights, and other intellectual property rights related to the Application remain the " +
            "property of the Service Provider."
    ),
    LegalDocumentEntry.SectionHeader("User-Generated Content and Acceptable Use"),
    LegalDocumentEntry.BodyParagraph(
        "If this Application allows users to post, share, or upload content, you agree not to post " +
            "content that:"
    ),
    LegalDocumentEntry.BulletItem(
        "Is illegal or violates third-party intellectual property rights (copyright, trademark, " +
            "patents)"
    ),
    LegalDocumentEntry.BulletItem("Is abusive, threatening, harassing, defamatory, or hate speech"),
    LegalDocumentEntry.BulletItem("Contains discrimination or incitement to violence or illegal activity"),
    LegalDocumentEntry.BulletItem("Is spam, phishing, or contains malware"),
    LegalDocumentEntry.BulletItem("Violates the privacy or personal data rights of others"),
    LegalDocumentEntry.BulletItem("Is misleading, false, or deceptive"),
    LegalDocumentEntry.BulletItem(
        "Contains explicit violence or sexual content (unless age-gated appropriately)"
    ),
    LegalDocumentEntry.BodyParagraph("The Service Provider reserves the right to:"),
    LegalDocumentEntry.BulletItem("Remove or disable access to content that violates these guidelines"),
    LegalDocumentEntry.BulletItem(
        "Suspend or terminate accounts of users who repeatedly violate these guidelines"
    ),
    LegalDocumentEntry.BulletItem("Cooperate with law enforcement if illegal content is reported"),
    LegalDocumentEntry.BulletItem(
        "Moderate, filter, or hide content that violates these Terms, applicable law, or the " +
            "guidelines set out above"
    ),
    LegalDocumentEntry.BodyParagraph(
        "Content submitted through the Application may be visible to other users or to the public, " +
            "depending on how the Application functions."
    ),
    LegalDocumentEntry.BodyParagraph(
        "If you believe content violates these Terms, infringes your rights, or is unlawful, you " +
            "may report it to the Service Provider at hoo309@gmail.com. The report should include " +
            "enough information for the Service Provider to identify the content, evaluate the " +
            "complaint, and contact you if follow-up is required."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Where the Application provides such features, you may also report content, block other " +
            "users, or mute notifications directly through the Application's interface. The Service " +
            "Provider will review in-app reports with the same standards described in these Terms."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider may review reported content, request additional information where " +
            "necessary, remove or restrict access to content, and take action against the " +
            "responsible account where appropriate. Users affected by moderation decisions may " +
            "contact the Service Provider at hoo309@gmail.com to request further review. The " +
            "Service Provider will respond to appeals within a reasonable period and provide the " +
            "reasons for any upheld moderation decision, subject to applicable law."
    ),
    LegalDocumentEntry.BodyParagraph(
        "By submitting User-Generated Content you grant the Service Provider a non-exclusive, " +
            "worldwide, royalty-free license to use, reproduce, distribute, prepare derivative " +
            "works of, display and perform the content in connection with the Application and the " +
            "Service Provider's business. This license does not grant the Service Provider the " +
            "right to sell or sublicense your content to third parties independently of the " +
            "Application. You represent and warrant that you own or control all rights in the " +
            "content you post and that use of the content does not violate these Terms or " +
            "applicable law."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Your content may include personal data. Processing of personal data related to " +
            "User-Generated Content is governed by the Privacy Policy. Do not post personal data " +
            "of others without their consent."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider is dedicated to ensuring that the Application is as beneficial and " +
            "efficient as possible. As such, they reserve the right to modify the Application or " +
            "charge for their services at any time and for any reason. The Service Provider " +
            "assures you that any charges for the Application or its services will be clearly " +
            "communicated to you."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Application stores and processes personal data that you have provided to the Service " +
            "Provider in order to provide the Service. It is your responsibility to maintain the " +
            "security of your mobile device and access to the Application."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider strongly advises against jailbreaking or rooting your mobile device, " +
            "which involves removing software restrictions and limitations imposed by the official " +
            "operating system of your mobile device. Such actions could expose your mobile device " +
            "to malware, viruses, malicious programs, compromise your mobile device's security " +
            "features, and may result in the Application not functioning correctly or at all."
    ),
    LegalDocumentEntry.SectionHeader("Third Party Services"),
    LegalDocumentEntry.BulletItem("Google Play Services"),
    LegalDocumentEntry.BulletItem("Google Analytics for Firebase"),
    LegalDocumentEntry.BulletItem("Firebase Crashlytics"),
    LegalDocumentEntry.BodyParagraph(
        "Please be aware that the Service Provider does not assume responsibility for certain " +
            "aspects. Some functions of the Application require an active internet connection, " +
            "which can be Wi-Fi or provided by your mobile network provider. The Service Provider " +
            "cannot be held responsible if the Application does not function at full capacity due " +
            "to lack of access to Wi-Fi or if you have exhausted your data allowance."
    ),
    LegalDocumentEntry.BodyParagraph(
        "If you are using the application outside of a Wi-Fi area, please be aware that your " +
            "mobile network provider's agreement terms still apply. Consequently, you may incur " +
            "charges from your mobile provider for data usage during the connection to the " +
            "application, or other third-party charges. By using the application, you accept " +
            "responsibility for any such charges, including roaming data charges if you use the " +
            "application outside of your home territory (i.e., region or country) without " +
            "disabling data roaming. If you are not the bill payer for the device on which you are " +
            "using the application, they assume that you have obtained permission from the bill " +
            "payer."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Similarly, the Service Provider cannot always assume responsibility for your usage of " +
            "the application. For instance, it is your responsibility to ensure that your device " +
            "remains charged. If your device runs out of battery and you are unable to access the " +
            "Service, the Service Provider cannot be held responsible."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Nothing in these Terms shall limit any rights you have under applicable consumer " +
            "protection laws that cannot be lawfully excluded."
    ),
    LegalDocumentEntry.SectionHeader("Limitation of Liability"),
    LegalDocumentEntry.BodyParagraph(
        "To the fullest extent permitted by law, the Service Provider shall not be liable for any " +
            "indirect, incidental, special, consequential, or punitive damages, including but not " +
            "limited to lost profits, data loss, or business interruption, even if advised of the " +
            "possibility of such damages."
    ),
    LegalDocumentEntry.BodyParagraph("However, the Service Provider retains full liability for:"),
    LegalDocumentEntry.BulletItem("Death or personal injury caused by negligence"),
    LegalDocumentEntry.BulletItem("Fraud or fraudulent misrepresentation"),
    LegalDocumentEntry.BulletItem("Any other liability that cannot be excluded or limited under applicable law"),
    LegalDocumentEntry.BodyParagraph(
        "To the fullest extent permitted by law, the total liability of the Service Provider for " +
            "any claim shall not exceed the amount paid by you to the Service Provider for the " +
            "Application in the 12 months preceding the claim, or the minimum amount that must be " +
            "paid under applicable law, whichever is greater. If the Application is provided free " +
            "of charge, this means the Service Provider's liability is limited to the minimum " +
            "amount permitted by applicable law."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider accepts no liability for any loss, direct or indirect, that you " +
            "experience as a result of relying entirely on third-party information provided " +
            "through this Application, or for inaccuracies in content provided by third parties."
    ),
    LegalDocumentEntry.SectionHeader("Indemnification"),
    LegalDocumentEntry.BodyParagraph(
        "To the fullest extent permitted by law, you agree to indemnify and hold harmless the " +
            "Service Provider, its affiliates, officers, directors, employees and agents from and " +
            "against any claims, liabilities, damages, losses and expenses, including reasonable " +
            "legal fees, arising out of or directly related to your breach of these Terms or your " +
            "intentional misuse of the Application, including User-Generated Content you submit in " +
            "violation of these Terms."
    ),
    LegalDocumentEntry.BodyParagraph(
        "This indemnification does not apply to claims arising from the Service Provider's own " +
            "negligence, breach of these Terms, or violation of applicable law. In jurisdictions " +
            "where consumer indemnification is restricted by law, this clause shall be limited to " +
            "the maximum extent permitted."
    ),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider may wish to update the application at some point. The application is " +
            "currently available as per the requirements for the operating system (and for any " +
            "additional systems they decide to extend the availability of the application to) may " +
            "change, and you will need to download the updates if you want to continue using the " +
            "application. The Service Provider does not guarantee that it will always update the " +
            "application so that it is relevant to you and/or compatible with the particular " +
            "operating system version installed on your device. You should accept updates when " +
            "offered; if you choose not to, the Service Provider may cease to support earlier " +
            "versions and the Application may not function properly. The Service Provider may also " +
            "wish to cease providing the application and may terminate its use at any time without " +
            "providing termination notice to you. Unless they inform you otherwise, upon any " +
            "termination, (a) the rights and licenses granted to you in these terms will end; (b) " +
            "you must cease using the application, and (if necessary) delete it from your device."
    ),
    LegalDocumentEntry.SectionHeader("Governing Law and Jurisdiction"),
    LegalDocumentEntry.BodyParagraph(
        "These Terms and Conditions are governed by the laws of the jurisdiction in which the " +
            "Service Provider is established, excluding conflict of law rules, except to the extent " +
            "mandatory consumer protection laws provide otherwise."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Any dispute arising out of or relating to these Terms will be brought before the courts " +
            "that have jurisdiction under applicable law. Nothing in this clause limits any rights " +
            "you may have to bring a claim in a court that is competent under mandatory law."
    ),
    LegalDocumentEntry.SectionHeader("DSA Compliance (Digital Services Act)"),
    LegalDocumentEntry.BodyParagraph(
        "If the Application is an intermediary service as defined under the Digital Services Act " +
            "(Regulation (EU) 2022/2065, \"DSA\"), the following provisions apply in addition to " +
            "the terms above."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Point of Contact: The Service Provider maintains a single point of contact for direct " +
            "communication with EU authorities and recipients of the service, reachable at " +
            "hoo309@gmail.com. Where the Service Provider is established outside the European " +
            "Union, a legal representative in the EU has been designated in accordance with " +
            "Article 13 of the DSA."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Content Moderation and Statement of Reasons: When the Service Provider restricts access " +
            "to content, suspends or terminates an account, or otherwise limits the availability " +
            "of the Application's features, a clear and specific statement of reasons will be " +
            "provided to the affected user. The statement will include the nature of the " +
            "restriction, the legal or contractual basis for the decision, and information on " +
            "available redress mechanisms, in accordance with Article 17 of the DSA."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Notice and Action: Users and third parties may submit notices of allegedly illegal " +
            "content through the contact details provided in these Terms. The Service Provider " +
            "will process notices promptly, diligently, and without automated decision-making " +
            "where the circumstances require human review. Notices will be acknowledged " +
            "electronically and a decision communicated without undue delay, in accordance with " +
            "Article 16 of the DSA."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Out-of-Court Dispute Settlement: Disputes regarding content moderation decisions, " +
            "including decisions to restrict content or suspend accounts, may be submitted to an " +
            "out-of-court dispute settlement body certified in accordance with Article 21 of the " +
            "DSA. The Service Provider will engage with such bodies in good faith. Use of " +
            "out-of-court dispute settlement does not affect your right to seek judicial remedy " +
            "under applicable law."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Transparency Reporting: The Service Provider publishes periodic transparency reports " +
            "covering content moderation activities, including the volume of notices received, " +
            "actions taken, and automated means used, in accordance with Article 24 of the DSA. " +
            "Reports are made available upon request at hoo309@gmail.com."
    ),
    LegalDocumentEntry.BodyParagraph(
        "These DSA provisions apply to the extent that the Application qualifies as an " +
            "intermediary service under the DSA and does not replace or limit any rights or " +
            "obligations under applicable consumer protection or data protection law."
    ),
    LegalDocumentEntry.SectionHeader("Severability"),
    LegalDocumentEntry.BodyParagraph(
        "If any provision of these Terms and Conditions is held to be invalid, illegal, or " +
            "unenforceable by a court of competent jurisdiction, such provision shall be modified " +
            "to the minimum extent necessary to make it valid and enforceable, and the remaining " +
            "provisions of these Terms shall remain in full force and effect."
    ),
    LegalDocumentEntry.SectionHeader("Entire Agreement"),
    LegalDocumentEntry.BodyParagraph(
        "These Terms and Conditions, together with the Privacy Policy, constitute the entire " +
            "agreement between you and the Service Provider concerning your use of the " +
            "Application, superseding any prior agreements or understandings."
    ),
    LegalDocumentEntry.SectionHeader("Changes to These Terms and Conditions"),
    LegalDocumentEntry.BodyParagraph(
        "The Service Provider may periodically update their Terms and Conditions. Therefore, you " +
            "are advised to review this page regularly for any changes. The Service Provider will " +
            "notify you of any changes by posting the new Terms and Conditions on this page."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Previous versions of these Terms and Conditions will be maintained and made available " +
            "upon request by contacting the Service Provider at hoo309@gmail.com."
    ),
    LegalDocumentEntry.BodyParagraph("These terms and conditions are effective as of 2026-07-22."),
    LegalDocumentEntry.SectionHeader("Contact Us"),
    LegalDocumentEntry.BodyParagraph(
        "If you have any questions or suggestions about the Terms and Conditions, please do not " +
            "hesitate to contact the Service Provider at hoo309@gmail.com."
    ),
)
private val VI_LEGAL_DOCUMENT_SECTIONS: List<LegalDocumentEntry> = listOf(
    LegalDocumentEntry.DocumentHeader("Deep Sleep — Điều Khoản & Điều Kiện Sử Dụng"),
    LegalDocumentEntry.BodyParagraph(
        "Các điều khoản và điều kiện này áp dụng cho ứng dụng di động Deep Sleep cùng các dịch vụ liên quan do Huu Tri vận hành (gọi chung là \"Ứng dụng\"). Huu Tri dưới đây được gọi là \"Nhà cung cấp dịch vụ\"."
    ),
    LegalDocumentEntry.BodyParagraph(
        "Khi tải xuống hoặc sử dụng Ứng dụng, bạn đồng ý tuân thủ các Điều khoản và Điều kiện này. Vui lòng đọc kỹ trước khi sử dụng."
    ),
    LegalDocumentEntry.SectionHeader("1. Giấy phép sử dụng Ứng dụng"),
    LegalDocumentEntry.BodyParagraph(
        "Nhà cung cấp dịch vụ cấp cho bạn giấy phép cá nhân, không độc quyền, không thể chuyển nhượng để cài đặt và sử dụng Ứng dụng trên thiết bị di động cho mục đích chăm sóc giấc ngủ và thư giãn cá nhân. Nghiêm cấm sao chép, dịch ngược mã nguồn hoặc phát tán trái phép."
    ),
    LegalDocumentEntry.SectionHeader("2. Quyền sở hữu trí tuệ"),
    LegalDocumentEntry.BodyParagraph(
        "Toàn bộ mã nguồn, giao diện, âm thanh, bản phối, truyện ngủ, biểu tượng và thương hiệu thuộc quyền sở hữu trí tuệ của Nhà cung cấp dịch vụ."
    ),
    LegalDocumentEntry.SectionHeader("3. Quyền riêng tư & Bảo mật dữ liệu"),
    LegalDocumentEntry.BodyParagraph(
        "Ứng dụng lưu trữ nhật ký giấc ngủ, bản phối yêu thích và cài đặt ngôn ngữ cục bộ trên thiết bị của bạn để phục vụ trải nghiệm cá nhân hóa. Ứng dụng sử dụng Google Play Services và Firebase để đồng bộ danh mục âm thanh."
    ),
    LegalDocumentEntry.SectionHeader("4. Giới hạn trách nhiệm"),
    LegalDocumentEntry.BodyParagraph(
        "Nội dung âm thanh, bài tập nhịp thở và phân tích giấc ngủ trong Ứng dụng chỉ phục vụ mục đích thư giãn và chăm sóc sức khỏe tinh thần, không thay thế cho chẩn đoán hoặc chỉ định y khoa chuyên nghiệp."
    ),
    LegalDocumentEntry.SectionHeader("5. Cập nhật & Thay đổi điều khoản"),
    LegalDocumentEntry.BodyParagraph(
        "Nhà cung cấp dịch vụ có thể định kỳ cập nhật Ứng dụng và các Điều khoản này để nâng cao chất lượng dịch vụ. Các điều khoản này có hiệu lực từ ngày 22/07/2026."
    ),
    LegalDocumentEntry.SectionHeader("6. Liên hệ hỗ trợ"),
    LegalDocumentEntry.BodyParagraph(
        "Nếu bạn có bất kỳ câu hỏi hoặc góp ý nào về Điều khoản & Điều kiện, vui lòng liên hệ Nhà cung cấp dịch vụ qua email: hoo309@gmail.com."
    ),
)
private val HEADER_TRANSLATIONS: Map<String, Map<String, String>> = mapOf(
    "Deep Sleep — Terms & Conditions" to mapOf(
        "pt-BR" to "Deep Sleep — Termos e Condições",
        "es" to "Deep Sleep — Términos y Condiciones",
        "fr" to "Deep Sleep — Conditions Générales d'Utilisation",
        "de" to "Deep Sleep — Nutzungsbedingungen",
        "ja" to "Deep Sleep — 利用規約",
        "ko" to "Deep Sleep — 이용약관",
        "it" to "Deep Sleep — Termini e Condizioni",
        "ru" to "Deep Sleep — Условия использования",
        "id" to "Deep Sleep — Syarat & Ketentuan",
        "tr" to "Deep Sleep — Kullanım Koşulları",
        "hi" to "Deep Sleep — नियम और शर्तें",
    ),
    "License to use the Application" to mapOf(
        "pt-BR" to "Licença de Uso do Aplicativo", "es" to "Licencia de uso de la Aplicación",
        "fr" to "Licence d'utilisation de l'Application", "de" to "Nutzungslizenz",
        "ja" to "アプリケーションの利用許諾", "ko" to "애플리케이션 사용 라이선스",
        "it" to "Licenza d'uso dell'Applicazione", "ru" to "Лицензия на использование",
        "id" to "Lisensi Penggunaan Aplikasi", "tr" to "Uygulama Kullanım Lisansı",
        "hi" to "एप्लिकेशन उपयोग लाइसेंस",
    ),
    "Intellectual Property" to mapOf(
        "pt-BR" to "Propriedade Intelectual", "es" to "Propiedad Intelectual",
        "fr" to "Propriété Intellectuelle", "de" to "Geistiges Eigentum",
        "ja" to "知的財産権", "ko" to "지식재산권",
        "it" to "Proprietà Intellettuale", "ru" to "Интеллектуальная собственность",
        "id" to "Kekayaan Intelektual", "tr" to "Fikri Mülkiyet",
        "hi" to "बौद्धिक संपदा",
    ),
    "Termination" to mapOf(
        "pt-BR" to "Rescisão", "es" to "Terminación",
        "fr" to "Résiliation", "de" to "Kündigung",
        "ja" to "利用終了・解除", "ko" to "이용 해지",
        "it" to "Risoluzione", "ru" to "Прекращение доступа",
        "id" to "Penghentian Layanan", "tr" to "Fesih",
        "hi" to "समाप्ति",
    ),
    "User-Generated Content and Acceptable Use" to mapOf(
        "pt-BR" to "Uso Aceitável", "es" to "Uso Aceptable",
        "fr" to "Utilisation Acceptable", "de" to "Zulässige Nutzung",
        "ja" to "適正な利用規約", "ko" to "올바른 서비스 이용",
        "it" to "Uso Consentito", "ru" to "Правила допустимого использования",
        "id" to "Penggunaan yang Diizinkan", "tr" to "Kabul Edilebilir Kullanım",
        "hi" to "स्वीकार्य उपयोग",
    ),
    "Third Party Services" to mapOf(
        "pt-BR" to "Serviços de Terceiros", "es" to "Servicios de Terceros",
        "fr" to "Services Tiers", "de" to "Drittanbieter-Dienste",
        "ja" to "第三者サービス", "ko" to "제3자 서비스",
        "it" to "Servizi di Terze Parti", "ru" to "Сторонние сервисы",
        "id" to "Layanan Pihak Ketiga", "tr" to "Üçüncü Taraf Hizmetleri",
        "hi" to "तृतीय-पक्ष सेवाएं",
    ),
    "Limitation of Liability" to mapOf(
        "pt-BR" to "Limitação de Responsabilidade", "es" to "Limitación de Responsabilidad",
        "fr" to "Limitation de Responsabilité", "de" to "Haftungsbeschränkung",
        "ja" to "免責事項・責任の制限", "ko" to "책임의 제한",
        "it" to "Limitazione di Responsabilità", "ru" to "Ограничение ответственности",
        "id" to "Batasan Tanggung Jawab", "tr" to "Sorumluluğun Sınırlandırılması",
        "hi" to "दायित्व की सीमा",
    ),
    "Changes to These Terms and Conditions" to mapOf(
        "pt-BR" to "Alterações nestes Termos", "es" to "Cambios en estos Términos",
        "fr" to "Modifications des Conditions", "de" to "Änderungen dieser Bedingungen",
        "ja" to "本規約の変更について", "ko" to "약관 변경 안내",
        "it" to "Modifiche ai presenti Termini", "ru" to "Изменения настоящих Условий",
        "id" to "Perubahan Syarat & Ketentuan", "tr" to "Koşullardaki Değişiklikler",
        "hi" to "नियमों और शर्तों में परिवर्तन",
    ),
    "Contact Us" to mapOf(
        "pt-BR" to "Fale Conosco", "es" to "Contáctanos",
        "fr" to "Nous Contacter", "de" to "Kontakt",
        "ja" to "お問い合わせ", "ko" to "문의하기",
        "it" to "Contattaci", "ru" to "Связаться с нами",
        "id" to "Hubungi Kami", "tr" to "Bize Ulaşın",
        "hi" to "संपर्क करें",
    ),
)
private fun termsEntriesForLanguage(langCode: String): List<LegalDocumentEntry> {
    if (langCode == "vi") return VI_LEGAL_DOCUMENT_SECTIONS
    if (langCode == "en") return LEGAL_DOCUMENT_SECTIONS
    return LEGAL_DOCUMENT_SECTIONS.map { entry ->
        when (entry) {
            is LegalDocumentEntry.DocumentHeader ->
                entry.copy(content = HEADER_TRANSLATIONS[entry.content]?.get(langCode) ?: entry.content)
            is LegalDocumentEntry.SectionHeader ->
                entry.copy(content = HEADER_TRANSLATIONS[entry.content]?.get(langCode) ?: entry.content)
            else -> entry
        }
    }
}
private sealed interface LegalDocumentEntry {
    data class DocumentHeader(val content: String) : LegalDocumentEntry
    data class SectionHeader(val content: String) : LegalDocumentEntry
    data class BodyParagraph(val content: String) : LegalDocumentEntry
    data class BulletItem(val content: String) : LegalDocumentEntry
}
@Composable
private fun LegalSectionEntryView(entry: LegalDocumentEntry) {
    when (entry) {
        is LegalDocumentEntry.DocumentHeader -> Text(
            text = entry.content,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        is LegalDocumentEntry.SectionHeader -> Text(
            text = entry.content,
            style = MaterialTheme.typography.titleMedium,
            color = PastelPurple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        is LegalDocumentEntry.BodyParagraph -> Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        is LegalDocumentEntry.BulletItem -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PastelPurple,
                )
            }
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}
/**
 * Displays the application Terms & Conditions document.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
    ScreenBackground(contentSafeArea = ContentSafeArea.None) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_terms),
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = TextPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { innerPadding ->
            val layoutMetrics = rememberResponsiveLayoutInfo()
            val activeLang = LocalAppLanguage.current
            val localizedSections = remember(activeLang) { termsEntriesForLanguage(activeLang) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier = Modifier.responsiveContentWidth(760.dp),
                    contentPadding = PaddingValues(
                        start = layoutMetrics.screenHorizontalPadding,
                        end = layoutMetrics.screenHorizontalPadding,
                        top = 8.dp,
                        bottom = 48.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "terms_inline_banner") {
                        AdaptiveBannerAdBar(slotKey = "terms_screen_banner")
                    }
                    items(localizedSections) { entry ->
                        LegalSectionEntryView(entry = entry)
                    }
                }
            }
        }
    }
}
