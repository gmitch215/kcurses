package kcurses.interop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestWindow {

	// Validation — pure logic, always run

	@Test
	fun testCreateWindowRejectsNegativeX() {
		assertFailsWith<IllegalArgumentException> { createWindow(-1, 0, 10, 5) }
	}

	@Test
	fun testCreateWindowRejectsNegativeY() {
		assertFailsWith<IllegalArgumentException> { createWindow(0, -1, 10, 5) }
	}

	@Test
	fun testCreateWindowRejectsZeroColumns() {
		assertFailsWith<IllegalArgumentException> { createWindow(0, 0, 0, 5) }
	}

	@Test
	fun testCreateWindowRejectsZeroRows() {
		assertFailsWith<IllegalArgumentException> { createWindow(0, 0, 10, 0) }
	}

	@Test
	fun testCreatePadRejectsZeroColumns() {
		assertFailsWith<IllegalArgumentException> { createPad(0, 10) }
	}

	@Test
	fun testCreatePadRejectsZeroRows() {
		assertFailsWith<IllegalArgumentException> { createPad(10, 0) }
	}

	// Standard screen — TTY required

	@Test
	fun testGetStandardScreenReturnsValidWindow() = runInCursesMode {
		val screen = getStandardScreen()
		assertTrue(screen.getRows() > 0)
		assertTrue(screen.getColumns() > 0)
	}

	// Window creation / deletion — TTY required

	@Test
	fun testCreateAndDeleteWindow() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		assertEquals(5, window.getRows())
		assertEquals(10, window.getColumns())
		window.delete()
	}

	@Test
	fun testCreatedWindowReportsScreenPosition() = runInCursesMode {
		val window = createWindow(5, 3, 10, 5)
		assertNotNull(window)
		try {
			assertEquals(5, window.getScreenX())
			assertEquals(3, window.getScreenY())
		} finally {
			window.delete()
		}
	}

	// Cursor on a window — TTY required

	@Test
	fun testWindowCursorMovement() = runInCursesMode {
		val window = createWindow(0, 0, 20, 10)
		assertNotNull(window)
		try {
			window.moveCursor(3, 2)
			assertEquals(3, window.getCursorX())
			assertEquals(2, window.getCursorY())
		} finally {
			window.delete()
		}
	}

	// Output on a window — TTY required

	@Test
	fun testWindowAddCharacterRoundTrips() = runInCursesMode {
		val window = createWindow(0, 0, 20, 10)
		assertNotNull(window)
		try {
			window.moveCursor(0, 0)
			window.addCharacter('Z')
			assertEquals('Z', window.getCharacterOnScreenAt(0, 0))
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowAddStringRoundTrips() = runInCursesMode {
		val window = createWindow(0, 0, 20, 10)
		assertNotNull(window)
		try {
			window.addStringAt(1, 1, "kc")
			assertEquals('k', window.getCharacterOnScreenAt(1, 1))
			assertEquals('c', window.getCharacterOnScreenAt(2, 1))
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowPrintFormatsArguments() = runInCursesMode {
		val window = createWindow(0, 0, 20, 10)
		assertNotNull(window)
		try {
			window.print(0, 0, "x=%s", "9")
			assertEquals('x', window.getCharacterOnScreenAt(0, 0))
			assertEquals('=', window.getCharacterOnScreenAt(1, 0))
			assertEquals('9', window.getCharacterOnScreenAt(2, 0))
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowClearScreen() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.addStringAt(0, 0, "abc")
			window.clearScreen()
			assertEquals(' ', window.getCharacterOnScreenAt(0, 0))
		} finally {
			window.delete()
		}
	}

	// Sub-windows / pads — TTY required

	@Test
	fun testSubwindowCreationAndDelete() = runInCursesMode {
		val parent = createWindow(0, 0, 20, 10)
		assertNotNull(parent)
		try {
			val sub = parent.createSubwindow(2, 1, 5, 3)
			assertNotNull(sub)
			assertEquals(3, sub.getRows())
			assertEquals(5, sub.getColumns())
			sub.delete()
		} finally {
			parent.delete()
		}
	}

	@Test
	fun testDerivedWindowCreation() = runInCursesMode {
		val parent = createWindow(0, 0, 20, 10)
		assertNotNull(parent)
		try {
			val derived = parent.createDerivedWindow(1, 1, 5, 3)
			assertNotNull(derived)
			derived.delete()
		} finally {
			parent.delete()
		}
	}

	@Test
	fun testDuplicateWindow() = runInCursesMode {
		val original = createWindow(0, 0, 10, 5)
		assertNotNull(original)
		try {
			val copy = original.duplicate()
			assertNotNull(copy)
			assertEquals(original.getRows(), copy.getRows())
			assertEquals(original.getColumns(), copy.getColumns())
			copy.delete()
		} finally {
			original.delete()
		}
	}

	@Test
	fun testPadDimensions() = runInCursesMode {
		val pad = createPad(50, 50)
		assertNotNull(pad)
		try {
			assertEquals(50, pad.getRows())
			assertEquals(50, pad.getColumns())
		} finally {
			pad.delete()
		}
	}

	// Attributes / scrolling / touched lines — TTY required

	@Test
	fun testWindowAttributeTogglesDoNotThrow() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.enableAttribute(0)
			window.disableAttribute(0)
			window.setAttributes(0)
			window.enableStandout()
			window.disableStandout()
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowScrollingApi() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.enableScrolling(true)
			window.setScrollRegion(0, 4)
			window.enableScrolling(false)
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowTouchedLines() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.touchAll()
			assertTrue(window.isWindowTouched())
			window.untouchAll()
			window.touchLines(0, 2, true)
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowInputBehaviorTogglesDoNotThrow() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.setKeypad(true)
			window.setKeypad(false)
			window.setNoDelay(true)
			window.setNoDelay(false)
			window.setNoTimeout(true)
			window.setNoTimeout(false)
			window.setTimeout(0)
			window.setInterruptFlush(true)
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowBackground() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.setBackground(' ')
			window.setBackgroundOnly(' ')
			window.getBackground()
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowLineDrawing() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.moveCursor(0, 0)
			window.drawHorizontalLine('-', 5)
			window.drawVerticalLineAt(0, 0, '|', 3)
			window.drawBox('|', '-')
		} finally {
			window.delete()
		}
	}

	@Test
	fun testWindowRefreshFunctions() = runInCursesMode {
		val window = createWindow(0, 0, 10, 5)
		assertNotNull(window)
		try {
			window.markForRefresh()
			window.refresh()
			window.redraw()
		} finally {
			window.delete()
		}
	}

	@Test
	fun testOverlayAndOverwrite() = runInCursesMode {
		val source = createWindow(0, 0, 10, 5)
		val destination = createWindow(0, 0, 10, 5)
		assertNotNull(source)
		assertNotNull(destination)
		try {
			source.addStringAt(0, 0, "abc")
			source.overlayOnto(destination)
			source.overwriteOnto(destination)
		} finally {
			source.delete()
			destination.delete()
		}
	}
}
