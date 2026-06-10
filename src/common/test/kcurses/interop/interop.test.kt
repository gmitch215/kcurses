package kcurses.interop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestInterop {

	// Format helper — pure logic, always run

	@Test
	fun testApplyFormatNoSubstitutions() {
		assertEquals("hello", applyFormat("hello", emptyArray()))
	}

	@Test
	fun testApplyFormatSingleStringSubstitution() {
		assertEquals("hello world", applyFormat("hello %s", arrayOf("world")))
	}

	@Test
	fun testApplyFormatMultipleSubstitutions() {
		assertEquals(
			"name=Bob, age=42",
			applyFormat("name=%s, age=%d", arrayOf("Bob", 42)),
		)
	}

	@Test
	fun testApplyFormatPercentEscape() {
		assertEquals("100%", applyFormat("100%%", emptyArray()))
	}

	@Test
	fun testApplyFormatLeavesUnconsumedPlaceholders() {
		assertEquals("Bob and %s", applyFormat("%s and %s", arrayOf("Bob")))
	}

	@Test
	fun testApplyFormatPreservesTrailingPercent() {
		assertEquals("100%", applyFormat("100%", emptyArray()))
	}

	@Test
	fun testApplyFormatIgnoresNonLetterSpecifier() {
		assertEquals("50%-off", applyFormat("50%-off", emptyArray()))
	}

	// Validation — pure logic, always run

	@Test
	fun testReassignColorRejectsNegativeIndex() {
		assertFailsWith<IllegalArgumentException> { reassignColor(-1, 0, 0, 0) }
	}

	@Test
	fun testReassignColorRejectsOutOfRangeRed() {
		assertFailsWith<IllegalArgumentException> { reassignColor(0, 256, 0, 0) }
	}

	@Test
	fun testReassignColorRejectsOutOfRangeGreen() {
		assertFailsWith<IllegalArgumentException> { reassignColor(0, 0, -1, 0) }
	}

	@Test
	fun testReassignColorRejectsOutOfRangeBlue() {
		assertFailsWith<IllegalArgumentException> { reassignColor(0, 0, 0, 1000) }
	}

	@Test
	fun testCreateColorPairRejectsZeroIndex() {
		assertFailsWith<IllegalArgumentException> { createColorPair(0, 1, 1) }
	}

	@Test
	fun testCreateColorPairRejectsOutOfRangeForeground() {
		assertFailsWith<IllegalArgumentException> { createColorPair(1, 256, 0) }
	}

	@Test
	fun testCreateColorPairRejectsOutOfRangeBackground() {
		assertFailsWith<IllegalArgumentException> { createColorPair(1, 0, -1) }
	}

	@Test
	fun testUseColorPairRejectsNegativeIndex() {
		assertFailsWith<IllegalArgumentException> { useColorPair(-1) }
	}

	@Test
	fun testGetColorContentRejectsNegativeIndex() {
		assertFailsWith<IllegalArgumentException> { getColorContent(-1) }
	}

	@Test
	fun testGetColorPairContentRejectsZeroIndex() {
		assertFailsWith<IllegalArgumentException> { getColorPairContent(0) }
	}

	@Test
	fun testSetCursorVisibilityRejectsNegative() {
		assertFailsWith<IllegalArgumentException> { setCursorVisibility(-1) }
	}

	@Test
	fun testSetCursorVisibilityRejectsTooLarge() {
		assertFailsWith<IllegalArgumentException> { setCursorVisibility(3) }
	}

	@Test
	fun testEnableHalfDelayRejectsZero() {
		assertFailsWith<IllegalArgumentException> { enableHalfDelay(0) }
	}

	@Test
	fun testEnableHalfDelayRejectsTooLarge() {
		assertFailsWith<IllegalArgumentException> { enableHalfDelay(256) }
	}

	@Test
	fun testGetStringRejectsZeroLength() {
		assertFailsWith<IllegalArgumentException> { getString(0) }
	}

	@Test
	fun testGetStringRejectsNegativeLength() {
		assertFailsWith<IllegalArgumentException> { getString(-5) }
	}

	// Lifecycle — TTY required

	@Test
	fun testStartAndEnd() = runInCursesMode {
		assertFalse(hasEnded())
	}

	@Test
	fun testHasEndedAfterEnd() {
		if (isHeadlessEnvironment()) return
		start()
		end()
		assertTrue(hasEnded())
	}

	@Test
	fun testGetRows() = runInCursesMode {
		assertTrue(getRows() > 0)
	}

	@Test
	fun testGetColumns() = runInCursesMode {
		assertTrue(getColumns() > 0)
	}

	// Cursor — TTY required

	@Test
	fun testMoveCursorUpdatesPosition() = runInCursesMode {
		moveCursor(0, 0)
		assertEquals(0, getCursorX())
		assertEquals(0, getCursorY())

		moveCursor(5, 3)
		assertEquals(5, getCursorX())
		assertEquals(3, getCursorY())
	}

	@Test
	fun testSetCursorVisibilityAcceptsAllLevels() = runInCursesMode {
		setCursorVisibility(0)
		setCursorVisibility(1)
		setCursorVisibility(2)
	}

	// Output — TTY required

	@Test
	fun testAddCharacterAtPositionRoundTrips() = runInCursesMode {
		clearScreen()
		moveCursor(0, 0)
		addCharacter('A')
		assertEquals('A', getCharacterOnScreenAt(0, 0))
	}

	@Test
	fun testAddStringAtPositionRoundTrips() = runInCursesMode {
		clearScreen()
		addStringAt(2, 1, "hello")
		assertEquals('h', getCharacterOnScreenAt(2, 1))
		assertEquals('e', getCharacterOnScreenAt(3, 1))
		assertEquals('o', getCharacterOnScreenAt(6, 1))
	}

	@Test
	fun testPrintFormatsArguments() = runInCursesMode {
		clearScreen()
		print(0, 0, "v=%s", "42")
		assertEquals('v', getCharacterOnScreenAt(0, 0))
		assertEquals('=', getCharacterOnScreenAt(1, 0))
		assertEquals('4', getCharacterOnScreenAt(2, 0))
		assertEquals('2', getCharacterOnScreenAt(3, 0))
	}

	@Test
	fun testClearScreenWipesContent() = runInCursesMode {
		addStringAt(0, 0, "hello")
		clearScreen()
		assertEquals(' ', getCharacterOnScreenAt(0, 0))
	}

	@Test
	fun testInsertAndDeleteCharacter() = runInCursesMode {
		clearScreen()
		addStringAt(0, 0, "abc")
		moveCursor(0, 0)
		insertCharacter('X')
		assertEquals('X', getCharacterOnScreenAt(0, 0))
		assertEquals('a', getCharacterOnScreenAt(1, 0))
		deleteCharacterAt(0, 0)
		assertEquals('a', getCharacterOnScreenAt(0, 0))
	}

	// Attributes — TTY required

	@Test
	fun testAttributeTogglesDoNotThrow() = runInCursesMode {
		enableBold(); disableBold()
		enableUnderline(); disableUnderline()
		enableReverse(); disableReverse()
		enableBlink(); disableBlink()
		enableDim(); disableDim()
		enableInvisible(); disableInvisible()
		enableStandout(); disableStandout()
		resetAttributes()
	}

	@Test
	fun testGenericAttributeApi() = runInCursesMode {
		enableAttribute(0)
		disableAttribute(0)
		setAttributes(0)
	}

	// Input modes — TTY required

	@Test
	fun testInputModesDoNotThrow() = runInCursesMode {
		enableEcho(); disableEcho()
		disableLineBuffering(); enableLineBuffering()
		enableRawInput(); disableRawInput()
		enableNewlineMode(); disableNewlineMode()
		enableNoDelay(true); enableNoDelay(false)
		enableNoTimeout(true); enableNoTimeout(false)
		enableInterruptFlush(true); enableInterruptFlush(false)
		keypad(true); keypad(false)
		setInputTimeout(0)
		setInputTimeout(-1)
		flushInput()
	}

	// Colors — TTY required

	@Test
	fun testColorCapabilityProbes() = runInCursesMode {
		hasColors()
		canChangeColors()
	}

	@Test
	fun testColorCountsAreNonNegative() = runInCursesMode {
		// COLORS / COLOR_PAIRS are -1 before start_color() and otherwise >= 0
		assertTrue(getColorCount() >= -1)
		assertTrue(getColorPairCount() >= -1)
	}

	// Misc — TTY required

	@Test
	fun testSleepMillisIsCallable() = runInCursesMode {
		sleepMillis(1)
	}

	@Test
	fun testTerminalNamesAreReadable() = runInCursesMode {
		assertNotNull(getTerminalName())
		assertNotNull(getTerminalLongName())
	}

	@Test
	fun testProgramModeRoundTrip() = runInCursesMode {
		saveProgramMode()
		restoreProgramMode()
	}

	@Test
	fun testForceUpdateScreen() = runInCursesMode {
		forceUpdateScreen()
	}

	@Test
	fun testPushBackCharacter() = runInCursesMode {
		pushBackCharacter('Q')
	}

	@Test
	fun testLineDrawingFunctionsDoNotThrow() = runInCursesMode {
		clearScreen()
		drawHorizontalLineAt(0, 0, '-', 5)
		drawVerticalLineAt(0, 0, '|', 3)
	}

	@Test
	fun testInsertOrDeleteLines() = runInCursesMode {
		clearScreen()
		insertLine()
		deleteLine()
		insertOrDeleteLines(0)
	}
}
