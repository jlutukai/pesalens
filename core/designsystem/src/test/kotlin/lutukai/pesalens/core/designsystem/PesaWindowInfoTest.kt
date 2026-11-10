package lutukai.pesalens.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.pesaWindowInfo

class PesaWindowInfoTest {

    @Test
    fun width599_isCompact() {
        assertEquals(PesaWidthClass.Compact, pesaWindowInfo(599, 800).widthClass)
    }

    @Test
    fun widths600And839_areMedium() {
        assertEquals(PesaWidthClass.Medium, pesaWindowInfo(600, 800).widthClass)
        assertEquals(PesaWidthClass.Medium, pesaWindowInfo(839, 800).widthClass)
    }

    @Test
    fun width840_isExpanded() {
        assertEquals(PesaWidthClass.Expanded, pesaWindowInfo(840, 800).widthClass)
    }

    @Test
    fun landscapePhone_hasCompactHeight() {
        assertEquals(true, pesaWindowInfo(892, 412).isCompactHeight)
    }

    @Test
    fun height480_isNotCompactHeight() {
        assertEquals(false, pesaWindowInfo(400, 480).isCompactHeight)
        assertEquals(true, pesaWindowInfo(400, 479).isCompactHeight)
    }
}
