package com.andoni.phrasaldaily

/**
 * Store-screenshot mode, started from adb on debug builds:
 * `am start -n com.andoni.phrasaldaily/.MainActivity --es demo_tab today --es demo_variant LATAM`.
 * It loads sample data and hides ads and pop-up dialogs.
 */
object DemoMode {
    @Volatile var active = false
}
