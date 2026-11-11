package lutukai.pesalens.core.model

/** Money in KES minor units (cents). Never use floating point for money. */
@JvmInline
value class Money(val minor: Long) : Comparable<Money> {
    operator fun plus(o: Money): Money = Money(minor + o.minor)

    operator fun minus(o: Money): Money = Money(minor - o.minor)

    /** Whole shillings, truncating toward zero. */
    val wholeShillings: Long get() = minor / 100

    override fun compareTo(other: Money): Int = minor.compareTo(other.minor)

    companion object {
        fun kes(shillings: Long): Money = Money(shillings * 100)

        val Zero: Money = Money(0)
    }
}
