package lutukai.pesalens.core.domain.calc

object PinPolicy {
    private val repeated = Regex("""^(\d)\1{3}$""")
    private val sequences = setOf("1234", "4321", "0123", "9876")

    fun isWeak(pin: String): Boolean = repeated.matches(pin) || pin in sequences
}
