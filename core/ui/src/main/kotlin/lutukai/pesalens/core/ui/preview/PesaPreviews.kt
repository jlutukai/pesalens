package lutukai.pesalens.core.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone light", group = "Phone", widthDp = 412, heightDp = 892)
@Preview(
    name = "Phone dark",
    group = "Phone",
    widthDp = 412,
    heightDp = 892,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
annotation class PesaPhonePreviews

@PesaPhonePreviews
@Preview(name = "Phone landscape", group = "Adaptive", widthDp = 892, heightDp = 412)
@Preview(name = "Foldable", group = "Adaptive", widthDp = 673, heightDp = 841)
@Preview(name = "Tablet", group = "Adaptive", widthDp = 1280, heightDp = 800)
annotation class PesaAdaptivePreviews
