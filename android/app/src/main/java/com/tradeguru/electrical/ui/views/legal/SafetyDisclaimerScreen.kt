package com.tradeguru.electrical.ui.views.legal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen
import kotlinx.coroutines.delay

private val CriticalRed = Color(0xFFD32F2F)

private data class Section(val number: String, val title: String, val body: String, val isCritical: Boolean = false)

private val sections = listOf(
    Section("1", "Nature of the Service \u2013 Guidance Only",
        "1.1 The Service provides general, best-practice guidance for licensed electricians and apprentices under supervision. It is not a substitute for professional judgment, site risk assessment, manufacturer instructions, or applicable laws and standards.\n\n1.2 The Service is reference material, not a definitive answer for any particular site, installation, or equipment. You remain solely responsible for verifying suitability and correctness in your job context."),
    Section("2", "Your Responsibilities (Non-Delegable)",
        "2.1 You confirm you are appropriately licensed/competent for the work undertaken and will ensure all work is performed in accordance with applicable legislation, WHS/OHS duties, electrical licensing rules, codes, and site procedures.\n\n2.2 Safety first: isolate and verify de-energised before work; wear appropriate PPE; do not defeat protective devices or interlocks; do not perform live work except where lawful and within a controlled test method.\n\n2.3 You will verify measurements, wiring arrangements, device ratings, and manufacturer instructions before acting on any guidance.\n\n2.4 Apprentices must be supervised in accordance with law and company policy."),
    Section("3", "Standards & Compliance (General)",
        "3.1 References are directional only. You are responsible for consulting the relevant standards, network/operator requirements, and manufacturer documentation applicable to your work.\n\n3.2 Where the Service summarises or interprets content you provide, you remain responsible for accuracy and context."),
    Section("4", "Product Information & Web Content",
        "4.1 Product research, links, and \u201ctips\u201d surfaced by the Service are informational only and may be incomplete, outdated, region-specific, or vendor-biased.\n\n4.2 You must independently verify specifications, approvals, and compatibility (for example RCD/RCBO types and ratings, PV/inverter/BMS limits, IP/IK/environmental ratings, warranties) before procurement or installation."),
    Section("5", "Standards & IP (No Standards Reproduction / Clean Room)",
        "5.1 Proprietary Materials. The Service does not reproduce or distribute proprietary standards, codes, manuals, or other protected publications (\u201cProprietary Standards\u201d).\n\n5.2 No Verbatim Requests. Do not prompt the Service to provide verbatim excerpts from Proprietary Standards or other copyrighted works unless you have a lawful right to do so.\n\n5.3 User-Supplied Content. Where you input such material, you warrant you have the right to do so and you authorise the Service to summarise, paraphrase, and transform your material for your internal reference.\n\n5.4 Best-Practice Only. Any references generated are directional best-practice and not a substitute for consulting the applicable Proprietary Standards, service rules, or manufacturer documentation.\n\n5.5 Takedown. On credible notice of infringement, we may remove or restrict access to affected content and take reasonable steps to prevent recurrence."),
    Section("6", "No Warranties",
        "6.1 To the maximum extent permitted by law, the Service is provided \u201cas is\u201d and \u201cas available,\u201d without warranties of any kind (express or implied), including as to accuracy, completeness, fitness for a particular purpose, or non-infringement.\n\n6.2 AI outputs are probabilistic and may contain errors; treat all outputs as advisory only and verify on site."),
    Section("7", "Australian Consumer Law / NZ Consumer Guarantees",
        "7.1 Nothing in this disclaimer excludes, restricts, or modifies any consumer guarantees or rights that cannot be excluded under the Australian Consumer Law or the New Zealand Consumer Guarantees Act.\n\n7.2 Where the Service is supplied to you as a consumer and not for re-supply, you may be entitled to remedies that cannot be excluded. Clauses 6 and 10 apply only to the extent permitted by those laws."),
    Section("8", "Data Handling, Privacy & Redaction",
        "Photos and Personal Information. Do not upload unnecessary personal or sensitive information. You are responsible for redacting client identifiers, addresses, or security details unless you have consent. We may process prompts, readings, and attachments to improve safety and quality; see our Privacy Notice."),
    Section("9", "Logging, Audit & Training Use",
        "Audit Trail. The Service may retain logs of your use (prompts, steps, readings, timestamps) to provide an audit trail and improve safety. You consent to such logging. You are responsible for any legal record-keeping obligations for your work."),
    Section("10", "Limitation of Liability",
        "10.1 To the maximum extent permitted by law, Trade Guru and its officers, employees, and contractors exclude all liability for any indirect, special, incidental, punitive, or consequential loss; loss of profit, revenue, or business; or loss arising from delays or downtime.\n\n10.2 To the extent liability cannot lawfully be excluded, Trade Guru limits its liability to, at its option, the re-supply of the Service or the cost of re-supplying the Service.\n\n10.3 You agree you are responsible for site safety, compliance, and workmanship, and will not rely on the Service as the sole basis for any decision or action taken on site.", isCritical = true),
    Section("11", "Indemnity",
        "To the fullest extent permitted by law, you indemnify and must keep indemnified Trade Guru and its officers, employees, and contractors from and against any and all claims, demands, actions, losses, damages, fines, penalties, costs, and expenses (including reasonable legal costs on a solicitor-client basis) arising out of or in connection with your use of the Service, including without limitation: (a) failure to comply with applicable laws, standards, network/service rules, or manufacturer instructions; (b) unsafe or non-compliant work practices; (c) reliance on outputs without appropriate verification or site assessment; (d) use of the Service outside its intended scope; and (e) any third-party or regulator claims related to the foregoing.", isCritical = true),
    Section("12", "Acceptable Use, Misuse & Emergencies",
        "12.1 Acceptable Use. You must not use the Service to engage in unlawful, unsafe, deceptive, or misleading conduct; to bypass safety devices; or to give unlicensed persons instructions to perform licensable work.\n\n12.2 Emergencies. The Service is not a substitute for emergency response. In case of electric shock, fire, arc-flash, or other incident: stop work, isolate, and contact emergency services immediately."),
    Section("13", "Professional Use & Eligibility",
        "The Service is intended for professional users. Apprentices may use the Service only under licensed supervision. If you are not legally permitted to perform the work, do not use the Service for that purpose."),
    Section("14", "Instruments, Calibration & Competency",
        "You are responsible for using appropriate, in-calibration instruments and test leads rated for the task and environment (including correct CAT ratings), and for competent use per manufacturer instructions."),
    Section("15", "Network Operator / Supply Authority Requirements",
        "You must comply with all requirements of the relevant supply authority or network operator (permits to work, disconnection/reconnection, metering seals, MEN arrangements, service rules). The Service does not supersede those requirements."),
    Section("16", "No Certification or Warranty of Compliance",
        "Outputs are not a certificate of compliance, test report, design certification, Form/CoC, or other regulated documentation. You remain responsible for preparing and lodging any statutory certificates and records."),
    Section("17", "Availability, Beta Features & Model Variability",
        "AI outputs may vary on repeat runs. Beta features may be unstable or withdrawn. We do not guarantee uptime, latency, or particular results."),
    Section("18", "Updates to Terms & Safety Notices",
        "We may update these terms or safety notices. Material changes will be communicated in-app. Continued use constitutes acceptance of updated terms."),
    Section("19", "Governing Law & Jurisdiction (Australia and New Zealand)",
        "19.1 Governing law. These terms are governed by the laws of Australia or New Zealand (as applicable to where you primarily use the Service), and\u2014within Australia\u2014by the laws of the State or Territory in which you primarily use the Service, in each case without regard to conflict-of-laws rules that would result in another law applying.\n\n19.2 Non-excludable rights. Nothing in these terms excludes, restricts, or modifies any rights or remedies that cannot be excluded under the Australian Consumer Law or the New Zealand Consumer Guarantees Act.\n\n19.3 Jurisdiction. You and Trade Guru submit to the non-exclusive jurisdiction of the courts and tribunals of (a) the Australian State or Territory in which you primarily use the Service, or (b) New Zealand (as applicable), and any courts competent to hear appeals from them.\n\n19.4 Venue convenience. If there is a dispute involving users in multiple Australian States/Territories or between Australia and New Zealand, the parties agree that proceedings may be commenced in any of the competent courts described above, and no party will object to venue on convenience grounds.")
)

@Composable
fun SafetyDisclaimerScreen(onAccept: () -> Unit) {
    val colors = LocalTradeGuruColors.current
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value to scrollState.maxValue }.collect { (value, max) ->
            if (max > 0 && value >= max - 50) { delay(500); hasScrolledToBottom = true }
        }
    }

    val buttonColor by animateColorAsState(
        if (hasScrolledToBottom) TradeGreen else colors.tradeTextSecondary, tween(300), label = "btn"
    )

    Column(modifier = Modifier.fillMaxSize().background(colors.tradeBg)) {
        DisclaimerHeader(colors)

        Column(
            Modifier.weight(1f).verticalScroll(scrollState).padding(horizontal = 24.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            DisclaimerIntro(colors)

            sections.forEachIndexed { index, section ->
                if (index == 5 || index == 9) {
                    Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.tradeBorder))
                }
                DisclaimerSection(section, colors)
            }

            Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.tradeBorder))
            DisclaimerAcknowledgement(colors)
            Spacer(Modifier.height(24.dp))
        }

        DisclaimerFooter(hasScrolledToBottom, buttonColor, colors, onAccept)
    }
}

@Composable
private fun DisclaimerHeader(colors: com.tradeguru.electrical.ui.theme.TradeGuruColors) {
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Warning, null, tint = colors.modeFaultFinder, modifier = Modifier.padding(top = 32.dp).size(60.dp))
        Text("Trade Guru", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = colors.tradeText, letterSpacing = (-0.5).sp, modifier = Modifier.padding(top = 8.dp))
        Text("End-User Disclaimer & Acknowledgement", fontSize = 14.sp, color = colors.tradeTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DisclaimerIntro(colors: com.tradeguru.electrical.ui.theme.TradeGuruColors) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.modeFaultFinder.copy(alpha = 0.08f)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Important \u2013 Read Carefully",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colors.modeFaultFinder
        )
        Text(
            "By accessing or using the Trade Guru application, guidance, checklists, \u201cStep Cards,\u201d fault-finding flows, product suggestions, or any related content (the \u201cService\u201d), you acknowledge and agree to the following terms.",
            fontSize = 15.sp,
            color = colors.tradeTextSecondary,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun DisclaimerSection(section: Section, colors: com.tradeguru.electrical.ui.theme.TradeGuruColors) {
    val badgeColor = if (section.isCritical) CriticalRed else colors.modeFaultFinder
    val wrapMod = if (section.isCritical)
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CriticalRed.copy(alpha = 0.08f)).padding(16.dp)
    else Modifier
    Column(modifier = wrapMod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Text(
                section.number,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(badgeColor).padding(top = 4.dp)
            )
            Text(
                section.title,
                fontSize = if (section.isCritical) 19.sp else 17.sp,
                fontWeight = if (section.isCritical) FontWeight.Bold else FontWeight.SemiBold,
                color = if (section.isCritical) CriticalRed else colors.tradeText
            )
        }
        Text(
            section.body,
            fontSize = 15.sp,
            color = colors.tradeTextSecondary,
            lineHeight = 22.sp,
            modifier = Modifier.padding(start = 38.dp)
        )
    }
}

@Composable
private fun DisclaimerAcknowledgement(colors: com.tradeguru.electrical.ui.theme.TradeGuruColors) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TradeGreen.copy(alpha = 0.08f)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Click-Through Acknowledgement",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TradeGreen
        )
        Text(
            "By selecting \u201cI Agree\u201d, you confirm that:",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        val items = listOf(
            "You are licensed/competent for the work (or an apprentice under supervision).",
            "You understand the Service is guidance only, not a definitive answer.",
            "You will isolate, verify de-energised, wear PPE, and never defeat protection.",
            "You will verify all outputs, measurements, and product data before acting.",
            "You accept the limitations of liability, ACL/NZ CGA statements, Privacy, Logging, Acceptable Use, and Standards & IP clauses above."
        )
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 4.dp)) {
                Text("\u2022", fontSize = 15.sp, color = colors.tradeText)
                Text(item, fontSize = 15.sp, color = colors.tradeTextSecondary, lineHeight = 22.sp)
            }
        }
    }
}

@Composable
private fun DisclaimerFooter(
    hasScrolledToBottom: Boolean,
    buttonColor: Color,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors,
    onAccept: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.tradeBorder))
        if (!hasScrolledToBottom) {
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = colors.tradeTextSecondary, modifier = Modifier.size(12.dp))
                Text("Scroll to read all terms", fontSize = 13.sp, color = colors.tradeTextSecondary)
            }
        }
        Button(
            onClick = onAccept,
            enabled = hasScrolledToBottom,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = colors.tradeTextSecondary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(56.dp)
        ) {
            Text("I Agree", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Text(
            "By continuing, you confirm you are a licensed electrical professional",
            fontSize = 12.sp,
            color = colors.tradeTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
        )
    }
}
