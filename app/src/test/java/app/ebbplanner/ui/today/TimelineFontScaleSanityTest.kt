package app.ebbplanner.ui.today

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.rememberTextMeasurer
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.theme.EbbTypography
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineFontScaleSanityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun withFontScale_doublesMeasuredLabelWidth_at200() {
        var fontScale by mutableStateOf(1f)
        val measured = mutableMapOf<Float, Float>()
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    val measurer = rememberTextMeasurer()
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    SideEffect {
                        measured[fontScale] =
                            measurer
                                .measure(
                                    text = "4 min left",
                                    style = EbbTypography.labelM,
                                    density = density,
                                ).size.width
                            .toFloat()
                    }
                    Text("probe $fontScale")
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { fontScale = 2f }
        composeRule.waitForIdle()
        val w100 = measured[1f] ?: error("missing f100 measure")
        val w200 = measured[2f] ?: error("missing f200 measure")
        val ratio = w200 / w100
        assertTrue("f200/f100 width ratio was $ratio (w100=$w100 w200=$w200)", ratio in 1.85f..2.15f)
    }
}