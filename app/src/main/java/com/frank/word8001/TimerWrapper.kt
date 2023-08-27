package com.frank.word8001

import java.util.*

/**
 * The timer used by the app is customizable. This way tests can run synchronously and very fast.
 *
 * See [DefaultTimer] for the default implementation and IntervalTimerViewModelTest.kt for a
 * test implementation.
 */
interface Timer {
    fun start(task: TimerTask)
}

/**
 * The default timer is used in the normal execution of the app.
 */
object DefaultTimer : Timer {

    private const val TIMER_PERIOD_MS = 1000L

    private var timer = Timer()

    override fun start(task: TimerTask) {
        timer = Timer()
        timer.scheduleAtFixedRate(task, 0, TIMER_PERIOD_MS)
    }
}
