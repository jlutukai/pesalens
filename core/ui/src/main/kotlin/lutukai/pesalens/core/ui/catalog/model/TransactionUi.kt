package lutukai.pesalens.core.ui.catalog.model

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.model.Money

enum class TxIcon {
    House, ShoppingCart, Basket, ForkKnife, ShoppingBag, GasPump, Bus, Car, Lightning, WifiHigh, Drop, Sneaker,
    CookingPot, Barbell, DeviceMobile, Scissors,

    /** Renders the Info icon. */
    Other,
}

/** [dateLabel] is already formatted ("2 Nov", "3–9 Nov · 6 entries"). */
@Immutable
data class TransactionUi(val id: String, val name: String, val dateLabel: String, val amount: Money, val icon: TxIcon)
