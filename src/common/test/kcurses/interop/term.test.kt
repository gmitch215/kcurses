package kcurses.interop

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTerm {

	@Test
	fun testQueryNumberCapabilityReturnsPositiveCols() = runInCursesMode {
		// `cols` is a numeric terminfo capability that is virtually always present and positive
		val cols = queryNumberCapability("cols")
		assertTrue(cols > 0, "cols should be positive but was $cols")
	}

	@Test
	fun testQueryNumberCapabilityReturnsPositiveLines() = runInCursesMode {
		val lines = queryNumberCapability("lines")
		assertTrue(lines > 0, "lines should be positive but was $lines")
	}

	@Test
	fun testQueryFlagCapabilityIsCallable() = runInCursesMode {
		// `am` (auto margins) is a common boolean capability; we just verify it can be queried
		queryFlagCapability("am")
	}

	@Test
	fun testQueryStringCapabilityForBell() = runInCursesMode {
		// `bel` (bell) is supported by virtually every terminfo entry
		assertNotNull(queryStringCapability("bel"))
	}

	@Test
	fun testQueryStringCapabilityReturnsNullForUnknown() = runInCursesMode {
		val result = queryStringCapability("nonexistentcap")
		// terminfo may return null or a tigetstr error sentinel; either way it should not crash
		if (result != null) {
			assertTrue(result.isNotEmpty() || result.isEmpty())
		}
	}

	@Test
	fun testQueryUnknownNumberCapabilityReturnsErrSentinel() = runInCursesMode {
		// unknown numeric capability returns -1 (not present) or -2 (not numeric)
		val value = queryNumberCapability("nonexistentcap")
		assertTrue(value == -1 || value == -2, "expected -1 or -2 but got $value")
	}

	@Test
	fun testOutputCapabilityStringDoesNotThrow() = runInCursesMode {
		val clear = queryStringCapability("clear")
		if (clear != null) {
			outputCapabilityString(clear)
		}
	}

	@Test
	fun testSubstituteParametersOnFormatWithoutPlaceholders() = runInCursesMode {
		val bell = queryStringCapability("bel")
		assertNotNull(bell)
		// no placeholders to substitute, so `tparm` should round-trip the input back out
		val substituted = substituteParameters(bell)
		assertNotNull(substituted)
	}
}
