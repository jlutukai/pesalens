package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.CalloutTone
import lutukai.pesalens.core.designsystem.component.PesaCallout
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaKicker
import lutukai.pesalens.core.designsystem.component.PesaPill
import lutukai.pesalens.core.designsystem.component.PesaRatioBar
import lutukai.pesalens.core.designsystem.component.PesaValueRow
import lutukai.pesalens.core.designsystem.component.PillTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong

private const val MonthlyStep = 5_000L
private const val AvailableStep = 10_000L

/**
 * Purchase estimate: outcome headline (or callout), progress, month blocks, value rows, a "What if…" stepper block and the
 * disclaimers. The steppers report a [StepDirection]; the caller applies the step (monthly 5,000, available 10,000).
 */
@Composable
fun AffordabilityCard(
    ui: AffordabilityUi,
    onMonthly: (StepDirection) -> Unit,
    onAvailable: (StepDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    val group = stringResource(R.string.core_ui_afford_group)
    PesaCard(modifier.semantics { contentDescription = group }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { PesaKicker(stringResource(R.string.core_ui_afford_kicker)) }
            PesaPill(stringResource(R.string.core_ui_afford_estimate_tag), PillTone.Dashed, height = 24.dp)
        }

        when (val outcome = ui.outcome) {
            is AffordabilityOutcome.OnTrack -> Column(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(pluralStringResource(R.plurals.core_ui_afford_headline, outcome.months, outcome.months), style = type.display, color = colors.text)
                Text(
                    stringResource(
                        R.string.core_ui_afford_headline_detail,
                        outcome.remaining.formatKes(),
                        ui.monthly.formatKes(),
                        outcome.estimatedMonth.formatLong(),
                    ),
                    style = type.bodySmall,
                    color = colors.textMuted,
                )
            }
            is AffordabilityOutcome.NoEndDate -> PesaCallout(
                stringResource(R.string.core_ui_afford_no_end_date_callout),
                PesaIcons.PauseCircle,
                CalloutTone.Warn,
                liveRegion = true,
            )
            AffordabilityOutcome.Covered -> PesaCallout(
                stringResource(R.string.core_ui_afford_covered_callout),
                PesaIcons.CheckCircle,
                CalloutTone.Accent,
                liveRegion = true,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PesaRatioBar(ui.percentSetAside / 100f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.core_ui_afford_percent_set_aside, ui.percentSetAside), style = SmallLabel, color = colors.textMuted)
                Text(stringResource(R.string.core_ui_afford_goal, ui.price.formatKes()), style = SmallLabel, color = colors.textMuted)
            }
        }

        if (ui.monthBlocks.isNotEmpty()) MonthBlocks(ui.monthBlocks)

        val remaining = when (val outcome = ui.outcome) {
            is AffordabilityOutcome.OnTrack -> outcome.remaining
            is AffordabilityOutcome.NoEndDate -> outcome.remaining
            AffordabilityOutcome.Covered -> Money.Zero
        }
        val timeline = when (val outcome = ui.outcome) {
            is AffordabilityOutcome.OnTrack ->
                stringResource(R.string.core_ui_afford_timeline_on_track, outcome.months, outcome.estimatedMonth.formatLong())
            is AffordabilityOutcome.NoEndDate -> stringResource(R.string.core_ui_afford_timeline_no_end)
            AffordabilityOutcome.Covered -> stringResource(R.string.core_ui_afford_timeline_covered)
        }
        Column {
            PesaValueRow(stringResource(R.string.core_ui_afford_row_price), ui.price.formatKes())
            PesaValueRow(stringResource(R.string.core_ui_afford_row_available), ui.available.formatKes())
            PesaValueRow(stringResource(R.string.core_ui_afford_row_remaining), remaining.formatKes())
            PesaValueRow(stringResource(R.string.core_ui_afford_row_timeline), timeline)
        }

        Surface(Modifier.fillMaxWidth(), shape = PesaLensTheme.shapes.tile, color = colors.surfaceAlt) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.core_ui_afford_what_if), style = type.meta.copy(fontWeight = FontWeight.Medium), color = colors.text)
                WhatIfStepper(
                    label = stringResource(R.string.core_ui_afford_what_if_monthly),
                    value = ui.monthly.formatKes(),
                    onDown = { onMonthly(StepDirection.Down) },
                    onUp = { onMonthly(StepDirection.Up) },
                    downEnabled = ui.monthly.minor > 0,
                    downDescription = stringResource(R.string.core_ui_afford_monthly_decrease),
                    upDescription = stringResource(R.string.core_ui_afford_monthly_increase),
                    announce = true,
                )
                WhatIfStepper(
                    label = stringResource(R.string.core_ui_afford_what_if_available),
                    value = ui.available.formatKes(),
                    onDown = { onAvailable(StepDirection.Down) },
                    onUp = { onAvailable(StepDirection.Up) },
                    downEnabled = ui.available.minor > 0,
                    downDescription = stringResource(R.string.core_ui_afford_available_decrease),
                    upDescription = stringResource(R.string.core_ui_afford_available_increase),
                    announce = false,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(PesaIcons.Info, contentDescription = null, tint = colors.textMuted, modifier = Modifier.padding(top = 1.dp).size(16.dp))
            Text(stringResource(R.string.core_ui_afford_assumptions), style = type.meta, color = colors.textMuted)
        }
        Row(
            Modifier.fillMaxWidth().topBorder(colors.border).padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(PesaIcons.GraduationCap, contentDescription = null, tint = colors.text, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.core_ui_afford_educational), style = type.meta.copy(fontWeight = FontWeight.Medium), color = colors.text)
        }
    }
}

/** Decorative month chips (hidden from accessibility: the headline already says the date). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthBlocks(blocks: List<String>) {
    val colors = PesaLensTheme.colors
    FlowRow(
        Modifier.fillMaxWidth().clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        blocks.forEach { label ->
            Surface(
                Modifier.widthIn(min = 40.dp).heightIn(min = 26.dp),
                shape = RoundedCornerShape(6.dp),
                color = colors.tintSubtle,
                border = BorderStroke(1.dp, colors.border),
            ) {
                Box(Modifier.padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                    Text(label, style = PesaLensTheme.type.caption, color = colors.textMuted)
                }
            }
        }
    }
}
