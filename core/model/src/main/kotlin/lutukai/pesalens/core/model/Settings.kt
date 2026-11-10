package lutukai.pesalens.core.model

enum class AssistantMode { Cloud, OnDevice }

enum class ThemePreference { System, Light, Dark }

data class AppSettings(
    val theme: ThemePreference = ThemePreference.System,
    val assistantMode: AssistantMode = AssistantMode.Cloud,
    val cloudConsentGiven: Boolean = false,
    val onboardingComplete: Boolean = false,
)
