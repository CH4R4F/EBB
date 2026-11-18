package app.ebbplanner.ui.today

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelinePillLabelPickTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pickTimeLeftPill_f100_52dp_slot_usesShortFormWithLeft() = assertChosen(1f, 52.dp, "4m left")

    @Test
    fun pickTimeLeftPill_f200_104dp_slot_usesShortFormWithLeft() = assertChosen(2f, 104.dp, "4m left")

    private fun assertChosen(
        fontScale: Float,
        maxTextWidth: Dp,
        expected: String,
    ) {
        val chosen = mutableStateOf<String?>(null)
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    val textMeasurer = rememberTextMeasurer()
                    val density = LocalDensity.current
                    SideEffect {
                        chosen.value =
                            pickTimeLeftPillLabel(
                                textMeasurer = textMeasurer,
                                density = density,
                                fullLabel = "4 min left",
                                maxTextWidth = maxTextWidth,
                            )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(expected, chosen.value)
    }
}