package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaIconTile
import lutukai.pesalens.core.designsystem.component.TileTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.catalog.model.TransactionUi
import lutukai.pesalens.core.ui.catalog.model.TxIcon
import lutukai.pesalens.core.ui.format.formatKes

internal fun TxIcon.imageVector(): ImageVector = when (this) {
    TxIcon.House -> PesaIcons.House
    TxIcon.ShoppingCart -> PesaIcons.ShoppingCart
    TxIcon.Basket -> PesaIcons.Basket
    TxIcon.ForkKnife -> PesaIcons.ForkKnife
    TxIcon.ShoppingBag -> PesaIcons.ShoppingBag
    TxIcon.GasPump -> PesaIcons.GasPump
    TxIcon.Bus -> PesaIcons.Bus
    TxIcon.Car -> PesaIcons.Car
    TxIcon.Lightning -> PesaIcons.Lightning
    TxIcon.WifiHigh -> PesaIcons.WifiHigh
    TxIcon.Drop -> PesaIcons.Drop
    TxIcon.Sneaker -> PesaIcons.Sneaker
    TxIcon.CookingPot -> PesaIcons.CookingPot
    TxIcon.Barbell -> PesaIcons.Barbell
    TxIcon.DeviceMobile -> PesaIcons.DeviceMobile
    TxIcon.Scissors -> PesaIcons.Scissors
    TxIcon.Other -> PesaIcons.Info
}

/** 64dp+ transaction line: 40dp icon tile, name over date, amount; 1dp top border. */
@Composable
fun TransactionRow(item: TransactionUi) {
    val colors = PesaLensTheme.colors
    val border = colors.border
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(border, Offset(0f, w / 2), Offset(size.width, w / 2), w)
            }
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PesaIconTile(item.icon.imageVector(), size = 40.dp, iconSize = 19.dp, tone = TileTone.Neutral)
        Column(Modifier.weight(1f)) {
            Text(item.name, style = PesaLensTheme.type.body, color = colors.text)
            Text(item.dateLabel, style = SmallLabel, color = colors.textMuted)
        }
        Text(item.amount.formatKes(), style = TabularBody, color = colors.text)
    }
}
