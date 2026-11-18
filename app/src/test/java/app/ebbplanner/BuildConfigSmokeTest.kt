package app.ebbplanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BuildConfigSmokeTest {
    @Test
    fun versionName_matchesVersionProperties() {
        assertEquals("0.1.0", BuildConfig.VERSION_NAME.removeSuffix("-debug"))
        assertTrue(BuildConfig.VERSION_CODE >= 100)
    }
}