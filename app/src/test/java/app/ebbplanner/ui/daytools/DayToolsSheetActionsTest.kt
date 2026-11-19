package app.ebbplanner.ui.daytools

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import app.ebbplanner.domain.day.FitMoveSuggestion
import app.ebbplanner.domain.day.MakeItFitPlan
import app.ebbplanner.domain.day.OverlapOption
import app.ebbplanner.domain.day.OverlapResolutionType
import app.ebbplanner.domain.day.OverlapSituation
import app.ebbplanner.domain.day.ShiftPreviewBlock
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.components.EBB_SHEET_CANCEL_TAG
import app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_TAG
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w360dp-h800dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class DayToolsSheetActionsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shift_actions_f100() = assertActions(1f, SheetKind.Shift)

    @Test
    fun shift_actions_f130() = assertActions(1.3f, SheetKind.Shift)

    @Test
    fun shift_actions_f200() = assertActions(2f, SheetKind.Shift)

    @Test
    fun overlap_actions_f100() = assertActions(1f, SheetKind.Overlap)

    @Test
    fun overlap_actions_f130() = assertActions(1.3f, SheetKind.Overlap)

    @Test
    fun overlap_actions_f200() = assertActions(2f, SheetKind.Overlap)

    @Test
    fun makeItFit_actions_f100() = assertActions(1f, SheetKind.MakeItFit)

    @Test
    fun makeItFit_actions_f130() = assertActions(1.3f, SheetKind.MakeItFit)

    @Test
    fun makeItFit_actions_f200() = assertActions(2f, SheetKind.MakeItFit)

    private enum class SheetKind { Shift, Overlap, MakeItFit }

    private fun assertActions(
        scale: Float,
        kind: SheetKind,
    ) {
        composeRule.setContent {
            WithFontScale(scale) {
                EbbTheme {
                    when (kind) {
                        SheetKind.Shift ->
                            ShiftTheRestDesign(
                                previewBefore = shiftBlocks(),
                                previewAfter = shiftBlocks(offset = 15),
                            )
                        SheetKind.Overlap -> OverlapHelperDesign(situation = overlapSituation())
                        SheetKind.MakeItFit -> MakeItFitDesign(plan = makeItFitPlan())
                    }
                }
            }
        }
        composeRule.waitForIdle()
        listOf(EBB_SHEET_CONFIRM_TAG, EBB_SHEET_CANCEL_TAG).forEach { tag ->
            val node = composeRule.onNodeWithTag(tag)
            node.assertIsDisplayed()
            node.assertHasClickAction()
            val bounds = node.getBoundsInRoot()
            assertTrue("$tag height ${bounds.height} at f$scale $kind", bounds.height >= 48.dp)
            assertTrue("$tag width ${bounds.width} at f$scale $kind", bounds.width >= 48.dp)
        }
    }

    private fun shiftBlocks(offset: Int = 0): List<ShiftPreviewBlock> =
        listOf(
            ShiftPreviewBlock("a", "Emails", "sky", 9 * 60 + offset, 45, pinned = false, isEvent = false),
            ShiftPreviewBlock("b", "Write brief", "amber", 10 * 60 + offset, 75, pinned = false, isEvent = false),
            ShiftPreviewBlock("c", "Lunch", "rose", 12 * 60 + offset, 45, pinned = true, isEvent = false),
        )

    private fun overlapSituation(): OverlapSituation =
        OverlapSituation(
            headline = "These overlap",
            detail = "Emails and Write brief share 15 min.",
            firstTitle = "Emails",
            secondTitle = "Write brief",
            firstStartMinutes = 9 * 60 + 10,
            firstDurationMin = 45,
            secondStartMinutes = 9 * 60 + 40,
            secondDurationMin = 75,
            options =
                listOf(
                    OverlapOption(OverlapResolutionType.PUSH_LATER, "Push later", "Move Write brief after Emails"),
                    OverlapOption(OverlapResolutionType.SHORTEN_EARLIER, "Shorten earlier", "Cut Emails to 30 min"),
                    OverlapOption(OverlapResolutionType.NEXT_FREE_SLOT, "Next free slot", "Park Write brief at 12:00"),
                    OverlapOption(OverlapResolutionType.KEEP_BOTH, "Keep both", "Leave the overlap"),
                ),
        )

    private fun makeItFitPlan(): MakeItFitPlan =
        MakeItFitPlan(
            plannedMinutes = 18 * 60,
            availableMinutes = 15 * 60,
            afterPlannedMinutes = 14 * 60,
            moves =
                listOf(
                    FitMoveSuggestion("late", "Late meeting", 45, "Move to inbox", selected = true),
                    FitMoveSuggestion("admin", "Admin", 30, "Move to tomorrow", selected = true),
                    FitMoveSuggestion("call", "Call bank", 20, "Drop to anytime", selected = false),
                ),
        )
}
