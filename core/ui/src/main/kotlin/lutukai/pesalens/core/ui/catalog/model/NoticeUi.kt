package lutukai.pesalens.core.ui.catalog.model

import androidx.compose.runtime.Immutable

enum class NoticeKind { NoData, Unsupported, ModelUnavailable, CloudOffline, InvalidResponse, SaveFailed }

/** [onClickKey] is handed back to the caller through `StatusNotice.onAction`. */
@Immutable
data class NoticeActionUi(val label: String, val onClickKey: String)
