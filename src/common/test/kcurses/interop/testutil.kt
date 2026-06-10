@file:OptIn(ExperimentalForeignApi::class)

package kcurses.interop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.isatty

internal fun isHeadlessEnvironment(): Boolean {
	if (getenv("CI") != null) return true
	if (getenv("HEADLESS") != null) return true

	val term = getenv("TERM")?.toKString()
	if (term.isNullOrEmpty() || term == "dumb") return true

	if (isatty(0) == 0) return true
	if (isatty(1) == 0) return true

	return false
}

internal fun runInCursesMode(block: () -> Unit) {
	if (isHeadlessEnvironment()) return

	start()
	try {
		block()
	} finally {
		end()
	}
}
