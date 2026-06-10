@file:OptIn(ExperimentalForeignApi::class)

package kcurses.interop

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import ncurses.A_BLINK
import ncurses.A_BOLD
import ncurses.A_DIM
import ncurses.A_INVIS
import ncurses.A_REVERSE
import ncurses.A_UNDERLINE
import ncurses.COLORS
import ncurses.COLOR_PAIR
import ncurses.COLOR_PAIRS
import ncurses.addch
import ncurses.addnstr
import ncurses.addstr
import ncurses.attroff
import ncurses.attron
import ncurses.attrset
import ncurses.beep
import ncurses.can_change_color
import ncurses.cbreak
import ncurses.clear
import ncurses.clrtobot
import ncurses.clrtoeol
import ncurses.color_content
import ncurses.curs_set
import ncurses.def_prog_mode
import ncurses.def_shell_mode
import ncurses.delch
import ncurses.deleteln
import ncurses.doupdate
import ncurses.echo
import ncurses.endwin
import ncurses.erase
import ncurses.flash
import ncurses.flushinp
import ncurses.getch
import ncurses.getcurx
import ncurses.getcury
import ncurses.getmaxx
import ncurses.getmaxy
import ncurses.getnstr
import ncurses.halfdelay
import ncurses.has_colors
import ncurses.hline
import ncurses.inch
import ncurses.init_color
import ncurses.init_pair
import ncurses.initscr
import ncurses.insch
import ncurses.insdelln
import ncurses.insertln
import ncurses.intrflush
import ncurses.isendwin
import ncurses.keypad
import ncurses.longname
import ncurses.move
import ncurses.mvaddch
import ncurses.mvaddnstr
import ncurses.mvaddstr
import ncurses.mvdelch
import ncurses.mvgetch
import ncurses.mvhline
import ncurses.mvinch
import ncurses.mvinsch
import ncurses.mvvline
import ncurses.napms
import ncurses.nl
import ncurses.nocbreak
import ncurses.nodelay
import ncurses.noecho
import ncurses.nonl
import ncurses.noraw
import ncurses.notimeout
import ncurses.pair_content
import ncurses.raw
import ncurses.refresh
import ncurses.reset_prog_mode
import ncurses.reset_shell_mode
import ncurses.standend
import ncurses.standout
import ncurses.start_color
import ncurses.stdscr
import ncurses.termname
import ncurses.ungetch
import ncurses.use_default_colors
import ncurses.vline
import ncurses.wtimeout

// Curses Mode

/**
 * Starts curses mode.
 * @return The current window representing the entire terminal screen, which can be used for output and input operations.
 */
fun start(): Window {
	val ptr = initscr() ?: throw RuntimeException("Failed to initialize curses mode")
	return Window(ptr)
}

/**
 * Ends curses mode.
 */
fun end() = endwin()

/**
 * Whether curses mode has been ended.
 */
fun hasEnded(): Boolean = isendwin()

/**
 * Refreshes the screen output, loading any changes from the buffer to the terminal.
 */
fun reloadScreen() = refresh()

/**
 * Forces an update of the physical screen from all virtual screens.
 *
 * This is typically used in conjunction with output functions that update virtual screens
 * without refreshing the physical screen directly (such as `wnoutrefresh` on windows). Calling
 * this flushes all pending updates to the terminal in one pass, reducing flicker.
 */
fun forceUpdateScreen() = doupdate()

// Input Mode

/**
 * Disables line buffering.
 *
 * Disabling line buffering enables taking input characters one at a time,
 * without waiting for the user to press Enter.
 */
fun disableLineBuffering() = cbreak()

/**
 * Enables line buffering.
 *
 * Line buffering requires an Enter press before characters can be passed
 * to the input.
 */
fun enableLineBuffering() = nocbreak()

/**
 * Enables raw input mode.
 *
 * Raw mode is similar to disabling line buffering, but additionally disables terminal-level
 * processing of control characters such as Ctrl+C and Ctrl+Z, allowing them to be read directly
 * as input.
 */
fun enableRawInput() = raw()

/**
 * Disables raw input mode.
 */
fun disableRawInput() = noraw()

/**
 * Enables half-delay input mode.
 *
 * In half-delay mode, characters typed by the user are immediately available, but if no
 * character is typed within the given timeout the input call returns without a result.
 *
 * @param tenthsOfSeconds The number of tenths of a second to wait, between 1 and 255
 */
fun enableHalfDelay(tenthsOfSeconds: Int) {
	require(tenthsOfSeconds in 1..255) { "tenthsOfSeconds must be in 1..255" }
	halfdelay(tenthsOfSeconds)
}

/**
 * Enables echo.
 *
 * Enabling echoes make the characters visible while you type them in the terminal.
 */
fun enableEcho() = echo()

/**
 * Disables echo.
 *
 * Disabling echoes make the characters no longer visible while you type them in the terminal.
 */
fun disableEcho() = noecho()

/**
 * Enables newline translation mode.
 *
 * When enabled, the return key is translated into a newline on input and newlines are
 * translated into carriage return + linefeed on output. This is the default behavior.
 */
fun enableNewlineMode() = nl()

/**
 * Disables newline translation mode.
 *
 * When disabled, the return key is not translated on input and newlines are not translated on
 * output. This allows for faster cursor motion since the cursor does not need to return to the
 * start of the line on a newline.
 */
fun disableNewlineMode() = nonl()

/**
 * Enables or disables the keypad of the user's terminal.
 *
 * Enabling the keypad allows for the capture of special keys such as arrow keys,
 * function keys, and other non-character keys. When the keypad is enabled, these
 * keys will be returned as single values by input functions, rather than as escape sequences.
 *
 * @param enable Whether to enable the keypad.
 */
fun keypad(enable: Boolean) = keypad(stdscr, enable)

/**
 * Enables or disables no-delay mode on the standard screen.
 *
 * In no-delay mode, character input functions are non-blocking and return without a result if
 * no input is pending.
 *
 * @param enable Whether to enable no-delay mode
 */
fun enableNoDelay(enable: Boolean) = nodelay(stdscr, enable)

/**
 * Enables or disables no-timeout mode on the standard screen.
 *
 * When enabled, ncurses waits indefinitely for the rest of an escape sequence rather than
 * timing out and returning the escape character on its own.
 *
 * @param enable Whether to enable no-timeout mode
 */
fun enableNoTimeout(enable: Boolean) = notimeout(stdscr, enable)

/**
 * Sets the input timeout in milliseconds for the standard screen.
 *
 * A negative value blocks indefinitely, zero is non-blocking, and a positive value waits up
 * to the given number of milliseconds before returning without a result.
 *
 * @param milliseconds The number of milliseconds to wait
 */
fun setInputTimeout(milliseconds: Int) = wtimeout(stdscr, milliseconds)

/**
 * Enables or disables interrupt flushing on the standard screen.
 *
 * When enabled, the input queue is flushed when an interrupt key (such as Ctrl+C) is pressed.
 * When disabled, pending input is preserved.
 *
 * @param enable Whether to enable interrupt flushing
 */
fun enableInterruptFlush(enable: Boolean) = intrflush(stdscr, enable)

/**
 * Discards any input that has been typed by the user but has not yet been read.
 */
fun flushInput() = flushinp()

// Cursor

/**
 * Moves the cursor to the given position.
 *
 * @param x The X coordinate to move to
 * @param y The Y coordinate to move to
 */
fun moveCursor(x: Int, y: Int) = move(y, x)

/**
 * Gets the current row the cursor is in.
 */
fun getCursorY(): Int = getcury(stdscr)

/**
 * Gets the current column the cursor is in.
 */
fun getCursorX(): Int = getcurx(stdscr)

/**
 * Sets the visibility of the cursor.
 *
 * A value of 0 makes the cursor invisible, 1 makes it normally visible, and 2 makes it very
 * visible. Not all terminals support every level.
 *
 * @param level The desired cursor visibility level, between 0 and 2
 */
fun setCursorVisibility(level: Int): Int {
	require(level in 0..2) { "level must be in 0..2" }
	return curs_set(level)
}

// Colors

/**
 * Whether color printing capabilities are available in this terminal.
 */
fun hasColors() = has_colors()

/**
 * Enables color printing capabilities in this terminal.
 */
fun beginColor() = start_color()

/**
 * Whether color changing is enabled.
 *
 * If true, the colors can be redefined using [reassignColor].
 * If false, the colors are fixed and cannot be changed.
 */
fun canChangeColors() = can_change_color()

/**
 * Assigns the value `-1` to the default foreground and background colors.
 *
 * This allows initializing color pairs with `-1` as the foreground or background to inherit
 * the user's terminal theme colors instead of using a fixed value.
 */
fun useDefaultColors() = use_default_colors()

/**
 * The number of distinct colors supported by the current terminal.
 */
fun getColorCount(): Int = COLORS

/**
 * The number of distinct color pairs supported by the current terminal.
 */
fun getColorPairCount(): Int = COLOR_PAIRS

/**
 * Reassigns a color RGB value.
 *
 * The index must be between 0 and COLORS - 1, inclusive. The r, g, and b values must be between 0 and 255, inclusive.
 *
 * @param index The index of the color to modify
 * @param r The red value to reassign
 * @param g The green value to reassign
 * @param b The blue value to reassign
 */
fun reassignColor(index: Int, r: Int, g: Int, b: Int) {
	require(index >= 0) { "index must be greater than zero" }
	require(r in 0..255) { "r must be in 0..255" }
	require(g in 0..255) { "g must be in 0..255" }
	require(b in 0..255) { "b must be in 0..255" }

	init_color(index.toShort(), r.toShort(), g.toShort(), b.toShort())
}

/**
 * Gets the RGB components of the color at the given index.
 *
 * Each component is reported on a scale of 0 to 1000 as ncurses uses internally.
 *
 * @param index The index of the color to query
 * @return A triple of (red, green, blue) components
 */
fun getColorContent(index: Int): Triple<Int, Int, Int> {
	require(index >= 0) { "index must be greater than zero" }

	return memScoped {
		val r = alloc<ShortVar>()
		val g = alloc<ShortVar>()
		val b = alloc<ShortVar>()
		color_content(index.toShort(), r.ptr, g.ptr, b.ptr)
		Triple(r.value.toInt(), g.value.toInt(), b.value.toInt())
	}
}

/**
 * Initializes a color pair.
 *
 * After creating a color with [reassignColor], you can pair foreground and background colors
 * together into a third color that can be used.
 *
 * @param index The index of the pair to assign
 * @param foreground The index of the foreground color
 * @param background The index of the background color
 */
fun createColorPair(index: Int, foreground: Int, background: Int) {
	// index 0 cannot be reassigned
	require(index > 0) { "index must be greater than zero" }
	require(foreground in 0..255) { "foreground must be in 0..255" }
	require(background in 0..255) { "background must be in 0..255" }

	init_pair(index.toShort(), foreground.toShort(), background.toShort())
}

/**
 * Gets the foreground and background color indices of the color pair at the given index.
 *
 * @param index The index of the color pair to query
 * @return A pair of (foreground, background) indices
 */
fun getColorPairContent(index: Int): Pair<Int, Int> {
	require(index > 0) { "index must be greater than zero" }

	return memScoped {
		val f = alloc<ShortVar>()
		val b = alloc<ShortVar>()
		pair_content(index.toShort(), f.ptr, b.ptr)
		Pair(f.value.toInt(), b.value.toInt())
	}
}

/**
 * Tells the terminal to use a color pair to set the visuals of the terminal.
 * @param index The index of the color pair to use.
 */
fun useColorPair(index: Int) {
	require(index >= 0) { "index must be greater than zero" }

	attron(COLOR_PAIR(index))
}

// Attributes

/**
 * Enables a text attribute, such as bold or underline.
 *
 * Multiple attributes can be combined using bitwise OR before passing the result to this
 * function.
 *
 * @param attribute The attribute bit-mask to enable
 */
fun enableAttribute(attribute: Int) = attron(attribute)

/**
 * Disables a text attribute previously enabled with [enableAttribute].
 *
 * @param attribute The attribute bit-mask to disable
 */
fun disableAttribute(attribute: Int) = attroff(attribute)

/**
 * Replaces the current set of text attributes with the given attribute.
 *
 * @param attribute The attribute bit-mask to set
 */
fun setAttributes(attribute: Int) = attrset(attribute)

/**
 * Resets all text attributes to their default values.
 */
fun resetAttributes() = standend()

/**
 * Enables bold text rendering.
 */
fun enableBold() = attron(A_BOLD.toInt())

/**
 * Disables bold text rendering.
 */
fun disableBold() = attroff(A_BOLD.toInt())

/**
 * Enables underlined text rendering.
 */
fun enableUnderline() = attron(A_UNDERLINE.toInt())

/**
 * Disables underlined text rendering.
 */
fun disableUnderline() = attroff(A_UNDERLINE.toInt())

/**
 * Enables reverse video, swapping the foreground and background colors of subsequent output.
 */
fun enableReverse() = attron(A_REVERSE.toInt())

/**
 * Disables reverse video.
 */
fun disableReverse() = attroff(A_REVERSE.toInt())

/**
 * Enables blinking text rendering.
 */
fun enableBlink() = attron(A_BLINK.toInt())

/**
 * Disables blinking text rendering.
 */
fun disableBlink() = attroff(A_BLINK.toInt())

/**
 * Enables dim (half-bright) text rendering.
 */
fun enableDim() = attron(A_DIM.toInt())

/**
 * Disables dim text rendering.
 */
fun disableDim() = attroff(A_DIM.toInt())

/**
 * Enables invisible text rendering.
 */
fun enableInvisible() = attron(A_INVIS.toInt())

/**
 * Disables invisible text rendering.
 */
fun disableInvisible() = attroff(A_INVIS.toInt())

/**
 * Enables standout mode, the most visible rendering mode the terminal supports.
 */
fun enableStandout() = standout()

/**
 * Disables standout mode.
 */
fun disableStandout() = standend()

// Output - Characters

/**
 * Adds a character to the buffer at the current cursor position.
 * @param char The character to add.
 */
fun addCharacter(char: Char) = addch(char.code.toUInt())

/**
 * Adds a character to the buffer at the given position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param char The character to add
 */
fun addCharacterAt(x: Int, y: Int, char: Char) = mvaddch(y, x, char.code.toUInt())

/**
 * Inserts a character into the buffer at the current cursor position, shifting existing
 * characters to the right.
 *
 * @param char The character to insert
 */
fun insertCharacter(char: Char) = insch(char.code.toUInt())

/**
 * Inserts a character into the buffer at the given position, shifting existing characters to
 * the right.
 *
 * @param x The X coordinate to insert at
 * @param y The Y coordinate to insert at
 * @param char The character to insert
 */
fun insertCharacterAt(x: Int, y: Int, char: Char) = mvinsch(y, x, char.code.toUInt())

/**
 * Deletes the character at the current cursor position, shifting characters to the right of
 * the cursor leftwards by one.
 */
fun deleteCharacter() = delch()

/**
 * Deletes the character at the given position, shifting characters to the right of that
 * position leftwards by one.
 *
 * @param x The X coordinate to delete at
 * @param y The Y coordinate to delete at
 */
fun deleteCharacterAt(x: Int, y: Int) = mvdelch(y, x)

/**
 * Gets the character currently rendered on the screen at the cursor position.
 */
fun getCharacterAtCursor(): Char = inch().toInt().toChar()

/**
 * Gets the character currently rendered on the screen at the given position.
 *
 * @param x The X coordinate to query
 * @param y The Y coordinate to query
 */
fun getCharacterOnScreenAt(x: Int, y: Int): Char = mvinch(y, x).toInt().toChar()

// Output - Strings

/**
 * Adds a string to the buffer at the current cursor position.
 * @param string The string to add.
 */
fun addString(string: String) = addstr(string)

/**
 * Adds a string to the buffer at the given position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param string The string to add
 */
fun addStringAt(x: Int, y: Int, string: String) = mvaddstr(y, x, string)

/**
 * Adds at most the given number of characters from a string to the buffer at the current
 * cursor position.
 *
 * @param string The string to add
 * @param maxLength The maximum number of characters to add
 */
fun addLimitedString(string: String, maxLength: Int) = addnstr(string, maxLength)

/**
 * Adds at most the given number of characters from a string to the buffer at the given
 * position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param string The string to add
 * @param maxLength The maximum number of characters to add
 */
fun addLimitedStringAt(x: Int, y: Int, string: String, maxLength: Int) =
	mvaddnstr(y, x, string, maxLength)

/**
 * Prints a message to the buffer at the current cursor position.
 *
 * Formatting is supported, and the arguments will be substituted into the string in order.
 *
 * @param string The string to print out
 * @param arguments Format arguments to pass to the string
 */
fun print(string: String, vararg arguments: Any?) = addstr(applyFormat(string, arguments))

/**
 * Prints a message to the buffer at a specified cursor position.
 *
 * Formatting is supported, and the arguments will be substituted into the string in order.
 *
 * @param x The X coordinate to print at
 * @param y The Y coordinate to print at
 * @param string The string to print out
 * @param arguments Format arguments to pass to the string
 */
fun print(x: Int, y: Int, string: String, vararg arguments: Any?) =
	mvaddstr(y, x, applyFormat(string, arguments))

/**
 * Substitutes positional arguments into a printf-style format string.
 *
 * Each unescaped percent sign followed by a letter consumes one argument and is replaced by
 * that argument's `toString()` representation. Literal percent signs may be expressed with
 * `%%`. This is a small portable substitute for `String.format`, which is not available in
 * commonMain on every Kotlin/Native configuration.
 */
internal fun applyFormat(template: String, args: Array<out Any?>): String {
	if (args.isEmpty() && '%' !in template) return template

	val sb = StringBuilder()
	var i = 0
	var argIndex = 0

	while (i < template.length) {
		val char = template[i]
		if (char == '%' && i + 1 < template.length) {
			val spec = template[i + 1]
			if (spec == '%') {
				sb.append('%')
				i += 2
				continue
			}
			if (spec.isLetter() && argIndex < args.size) {
				sb.append(args[argIndex].toString())
				argIndex++
				i += 2
				continue
			}
		}
		sb.append(char)
		i++
	}

	return sb.toString()
}

// Lines

/**
 * Inserts a blank line above the current cursor line, shifting all lines below it downward
 * by one.
 */
fun insertLine() = insertln()

/**
 * Deletes the line containing the cursor, shifting all lines below it upward by one.
 */
fun deleteLine() = deleteln()

/**
 * Inserts (positive count) or deletes (negative count) the given number of lines at the
 * current cursor position.
 *
 * @param count The number of lines to insert (positive) or delete (negative)
 */
fun insertOrDeleteLines(count: Int) = insdelln(count)

/**
 * Draws a horizontal line of repeated characters starting at the current cursor position.
 *
 * The cursor itself does not move after drawing.
 *
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun drawHorizontalLine(char: Char, length: Int) = hline(char.code.toUInt(), length)

/**
 * Draws a vertical line of repeated characters starting at the current cursor position.
 *
 * The cursor itself does not move after drawing.
 *
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun drawVerticalLine(char: Char, length: Int) = vline(char.code.toUInt(), length)

/**
 * Draws a horizontal line of repeated characters starting at the given position.
 *
 * @param x The X coordinate to start drawing at
 * @param y The Y coordinate to start drawing at
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun drawHorizontalLineAt(x: Int, y: Int, char: Char, length: Int) =
	mvhline(y, x, char.code.toUInt(), length)

/**
 * Draws a vertical line of repeated characters starting at the given position.
 *
 * @param x The X coordinate to start drawing at
 * @param y The Y coordinate to start drawing at
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun drawVerticalLineAt(x: Int, y: Int, char: Char, length: Int) =
	mvvline(y, x, char.code.toUInt(), length)

// Input

/**
 * Gets the current character input being typed.
 */
fun getCharacter(): Char = getch().toChar()

/**
 * Moves the cursor to the given position and reads a single character of input from there.
 *
 * @param x The X coordinate to read from
 * @param y The Y coordinate to read from
 */
fun getCharacterFrom(x: Int, y: Int): Char = mvgetch(y, x).toChar()

/**
 * Reads a string of input from the user, up to the given number of characters.
 *
 * Input is collected until a newline is entered or the maximum length is reached.
 *
 * @param maxLength The maximum number of characters to read
 */
fun getString(maxLength: Int = 256): String {
	require(maxLength > 0) { "maxLength must be greater than zero" }

	return memScoped {
		val buffer = allocArray<ByteVar>(maxLength + 1)
		getnstr(buffer, maxLength)
		buffer.toKString()
	}
}

/**
 * Pushes a character back onto the input stream so that it is returned by the next input
 * call.
 *
 * @param char The character to push back
 */
fun pushBackCharacter(char: Char) = ungetch(char.code)

// Screen Dimensions

/**
 * Gets the number of rows available in this window.
 */
fun getRows(): Int = getmaxy(stdscr)

/**
 * Gets the number of columns available in this window.
 */
fun getColumns(): Int = getmaxx(stdscr)

// Clearing

/**
 * Clears the entire screen, wiping all buffer output.
 */
fun clearScreen() = erase()

/**
 * Clears the entire screen and forces a complete redraw on the next refresh.
 *
 * Unlike [clearScreen], this guarantees the terminal is fully repainted, which is useful to
 * recover from corrupted display state.
 */
fun clearAndRedraw() = clear()

/**
 * Clears the screen from the current cursor position to the end of the screen.
 */
fun clearFromCursorToEnd() = clrtobot()

/**
 * Clears the current line from the beginning to where your cursor is.
 */
fun clearToEnd() = clrtoeol()

// Sound and Visual Bells

/**
 * Plays an audible beep sound if the terminal supports it; otherwise, flashes the screen.
 */
fun playBeep() = beep()

/**
 * Flashes the screen if the terminal supports it; otherwise, plays an audible beep.
 */
fun flashScreen() = flash()

// Timing

/**
 * Sleeps for the given number of milliseconds without consuming any pending input.
 *
 * @param milliseconds The number of milliseconds to sleep
 */
fun sleepMillis(milliseconds: Int) = napms(milliseconds)

// Terminal Modes

/**
 * Saves the current terminal modes as the program (curses) mode for later restoration with
 * [restoreProgramMode].
 */
fun saveProgramMode() = def_prog_mode()

/**
 * Restores the terminal modes that were last saved with [saveProgramMode].
 */
fun restoreProgramMode() = reset_prog_mode()

/**
 * Saves the current terminal modes as the shell (non-curses) mode for later restoration with
 * [restoreShellMode].
 */
fun saveShellMode() = def_shell_mode()

/**
 * Restores the terminal modes that were last saved with [saveShellMode].
 */
fun restoreShellMode() = reset_shell_mode()

/**
 * The short name of the terminal type, as taken from the TERM environment variable.
 */
fun getTerminalName(): String? = termname()?.toKString()

/**
 * The verbose, human-readable name of the terminal type.
 */
fun getTerminalLongName(): String? = longname()?.toKString()
