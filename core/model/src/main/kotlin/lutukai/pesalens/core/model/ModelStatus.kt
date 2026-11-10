package lutukai.pesalens.core.model

sealed interface ModelStatus {
    data object NotDownloaded : ModelStatus

    data object ReadyToDownload : ModelStatus

    data class Downloading(val percent: Int) : ModelStatus

    data object Verifying : ModelStatus

    data object Ready : ModelStatus

    data class Failed(val stoppedAtPercent: Int) : ModelStatus

    data object Unsupported : ModelStatus
}
