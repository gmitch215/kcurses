@file:OptIn(ExperimentalForeignApi::class)

package kcurses.interop

import cnames.structs._win_st
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import ncurses.box
import ncurses.copywin
import ncurses.delwin
import ncurses.derwin
import ncurses.dupwin
import ncurses.getbegx
import ncurses.getbegy
import ncurses.getbkgd
import ncurses.getcurx
import ncurses.getcury
import ncurses.getmaxx
import ncurses.getmaxy
import ncurses.getparx
import ncurses.getpary
import ncurses.intrflush
import ncurses.is_linetouched
import ncurses.is_wintouched
import ncurses.keypad
import ncurses.meta
import ncurses.mvderwin
import ncurses.mvwaddch
import ncurses.mvwaddnstr
import ncurses.mvwaddstr
import ncurses.mvwdelch
import ncurses.mvwgetch
import ncurses.mvwhline
import ncurses.mvwin
import ncurses.mvwinch
import ncurses.mvwinsch
import ncurses.mvwvline
import ncurses.newpad
import ncurses.newwin
import ncurses.nodelay
import ncurses.notimeout
import ncurses.overlay
import ncurses.overwrite
import ncurses.pnoutrefresh
import ncurses.prefresh
import ncurses.redrawwin
import ncurses.scrollok
import ncurses.stdscr
import ncurses.subwin
import ncurses.touchwin
import ncurses.untouchwin
import ncurses.waddch
import ncurses.waddnstr
import ncurses.waddstr
import ncurses.wattroff
import ncurses.wattron
import ncurses.wattrset
import ncurses.wbkgd
import ncurses.wbkgdset
import ncurses.wborder
import ncurses.wclear
import ncurses.wclrtobot
import ncurses.wclrtoeol
import ncurses.wcursyncup
import ncurses.wdelch
import ncurses.wdeleteln
import ncurses.werase
import ncurses.wgetch
import ncurses.wgetnstr
import ncurses.whline
import ncurses.winch
import ncurses.winsch
import ncurses.winsdelln
import ncurses.winsertln
import ncurses.wmove
import ncurses.wnoutrefresh
import ncurses.wredrawln
import ncurses.wrefresh
import ncurses.wscrl
import ncurses.wsetscrreg
import ncurses.wstandend
import ncurses.wstandout
import ncurses.wsyncdown
import ncurses.wsyncup
import ncurses.wtimeout
import ncurses.wtouchln
import ncurses.wvline

/**
 * A handle to an ncurses window backed by a `WINDOW *` pointer (`cnames.structs._win_st`).
 *
 * Use [createWindow] to allocate a new window, [createPad] to create an off-screen pad, or
 * [getStandardScreen] to obtain a wrapper around the standard screen created by [start].
 *
 * @property handle The underlying ncurses window pointer
 */
class Window(val handle: CPointer<_win_st>)

/**
 * Gets the standard screen, the default window created by [start].
 *
 * Must only be called after [start] has been invoked; otherwise the underlying pointer is
 * null and this throws.
 */
fun getStandardScreen(): Window =
	Window(stdscr ?: error("Curses mode is not initialized; call start() first."))

/**
 * Creates a new window with the given position and dimensions.
 *
 * @param x The X coordinate of the upper-left corner
 * @param y The Y coordinate of the upper-left corner
 * @param columns The number of columns the window will span
 * @param rows The number of rows the window will span
 * @return The new window, or `null` if allocation failed
 */
fun createWindow(x: Int, y: Int, columns: Int, rows: Int): Window? {
	require(x >= 0) { "x must be greater than or equal to zero" }
	require(y >= 0) { "y must be greater than or equal to zero" }
	require(columns > 0) { "columns must be greater than zero" }
	require(rows > 0) { "rows must be greater than zero" }

	val ptr = newwin(rows, columns, y, x) ?: return null
	return Window(ptr)
}

/**
 * Creates a new off-screen pad with the given dimensions.
 *
 * Pads are like windows except they are not associated with a screen position. A portion of
 * a pad is rendered to the screen on demand via [refreshPortion] or [markPortionForRefresh].
 *
 * @param columns The number of columns the pad will span
 * @param rows The number of rows the pad will span
 * @return The new pad, or `null` if allocation failed
 */
fun createPad(columns: Int, rows: Int): Window? {
	require(columns > 0) { "columns must be greater than zero" }
	require(rows > 0) { "rows must be greater than zero" }

	val ptr = newpad(rows, columns) ?: return null
	return Window(ptr)
}

/**
 * Creates a sub-window of this window.
 *
 * The new window shares its character storage with the parent, so changes to one are visible
 * in the other after a refresh.
 *
 * @param x The X coordinate of the sub-window's upper-left corner, in absolute screen coordinates
 * @param y The Y coordinate of the sub-window's upper-left corner, in absolute screen coordinates
 * @param columns The number of columns the sub-window will span
 * @param rows The number of rows the sub-window will span
 */
fun Window.createSubwindow(x: Int, y: Int, columns: Int, rows: Int): Window? {
	val ptr = subwin(handle, rows, columns, y, x) ?: return null
	return Window(ptr)
}

/**
 * Creates a derived sub-window of this window.
 *
 * Identical to [createSubwindow], except the coordinates are relative to the parent rather
 * than the screen.
 *
 * @param x The X coordinate relative to the parent window
 * @param y The Y coordinate relative to the parent window
 * @param columns The number of columns the sub-window will span
 * @param rows The number of rows the sub-window will span
 */
fun Window.createDerivedWindow(x: Int, y: Int, columns: Int, rows: Int): Window? {
	val ptr = derwin(handle, rows, columns, y, x) ?: return null
	return Window(ptr)
}

/**
 * Creates a duplicate of this window with the same data and attributes.
 */
fun Window.duplicate(): Window? {
	val ptr = dupwin(handle) ?: return null
	return Window(ptr)
}

/**
 * Releases the resources associated with this window.
 *
 * The window must not be referenced after this call.
 */
fun Window.delete() = delwin(handle)

/**
 * Moves this window to a new position on the screen.
 *
 * @param x The new X coordinate of the upper-left corner
 * @param y The new Y coordinate of the upper-left corner
 */
fun Window.moveWindow(x: Int, y: Int) = mvwin(handle, y, x)

/**
 * Moves this derived sub-window to a new position relative to its parent.
 *
 * @param x The new X coordinate relative to the parent
 * @param y The new Y coordinate relative to the parent
 */
fun Window.moveDerivedWindow(x: Int, y: Int) = mvderwin(handle, y, x)

// Refresh

/**
 * Refreshes this window, copying its buffered contents to the physical screen.
 */
fun Window.refresh() = wrefresh(handle)

/**
 * Marks this window's buffered contents as ready to display without flushing to the physical
 * screen.
 *
 * Use this when refreshing multiple windows in sequence; call [forceUpdateScreen] afterwards
 * to flush all pending updates at once. This avoids flicker that occurs when each window is
 * refreshed individually.
 */
fun Window.markForRefresh() = wnoutrefresh(handle)

/**
 * Forces a full redraw of this window from scratch on the next refresh.
 */
fun Window.redraw() = redrawwin(handle)

/**
 * Forces a full redraw of the specified range of lines on the next refresh.
 *
 * @param startLine The first line to redraw
 * @param lineCount The number of lines to redraw
 */
fun Window.redrawLines(startLine: Int, lineCount: Int) = wredrawln(handle, startLine, lineCount)

/**
 * Refreshes a portion of this pad to a region of the physical screen.
 *
 * Only valid for pads created by [createPad].
 *
 * @param padX The X coordinate within the pad to start copying from
 * @param padY The Y coordinate within the pad to start copying from
 * @param screenX The X coordinate on the screen to start copying to
 * @param screenY The Y coordinate on the screen to start copying to
 * @param endScreenX The X coordinate on the screen to end copying at
 * @param endScreenY The Y coordinate on the screen to end copying at
 */
fun Window.refreshPortion(
	padX: Int,
	padY: Int,
	screenX: Int,
	screenY: Int,
	endScreenX: Int,
	endScreenY: Int,
) = prefresh(handle, padY, padX, screenY, screenX, endScreenY, endScreenX)

/**
 * Marks a portion of this pad as ready to display without flushing to the physical screen.
 *
 * Pad equivalent of [markForRefresh] — see [refreshPortion] for the meaning of arguments.
 */
fun Window.markPortionForRefresh(
	padX: Int,
	padY: Int,
	screenX: Int,
	screenY: Int,
	endScreenX: Int,
	endScreenY: Int,
) = pnoutrefresh(handle, padY, padX, screenY, screenX, endScreenY, endScreenX)

// Clearing

/**
 * Clears this window's buffer output and forces a complete redraw on the next refresh.
 */
fun Window.clearScreen() = wclear(handle)

/**
 * Erases this window's buffer output without forcing a complete redraw.
 */
fun Window.erase() = werase(handle)

/**
 * Clears this window from the current cursor position to the end of the window.
 */
fun Window.clearFromCursorToEnd() = wclrtobot(handle)

/**
 * Clears the current line in this window from the cursor to the end of the line.
 */
fun Window.clearToEnd() = wclrtoeol(handle)

// Cursor

/**
 * Moves the cursor in this window to the given position.
 *
 * @param x The X coordinate to move to
 * @param y The Y coordinate to move to
 */
fun Window.moveCursor(x: Int, y: Int) = wmove(handle, y, x)

/**
 * Gets the current row the cursor is in within this window.
 */
fun Window.getCursorY(): Int = getcury(handle)

/**
 * Gets the current column the cursor is in within this window.
 */
fun Window.getCursorX(): Int = getcurx(handle)

/**
 * Gets the number of rows in this window.
 */
fun Window.getRows(): Int = getmaxy(handle)

/**
 * Gets the number of columns in this window.
 */
fun Window.getColumns(): Int = getmaxx(handle)

/**
 * Gets the X coordinate of this window's upper-left corner on the screen.
 */
fun Window.getScreenX(): Int = getbegx(handle)

/**
 * Gets the Y coordinate of this window's upper-left corner on the screen.
 */
fun Window.getScreenY(): Int = getbegy(handle)

/**
 * Gets the X coordinate of this window's upper-left corner relative to its parent window.
 *
 * Returns -1 if this window has no parent.
 */
fun Window.getParentX(): Int = getparx(handle)

/**
 * Gets the Y coordinate of this window's upper-left corner relative to its parent window.
 *
 * Returns -1 if this window has no parent.
 */
fun Window.getParentY(): Int = getpary(handle)

// Output - Characters

/**
 * Adds a character to this window at the current cursor position.
 *
 * @param char The character to add
 */
fun Window.addCharacter(char: Char) = waddch(handle, char.code.toUInt())

/**
 * Adds a character to this window at the given position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param char The character to add
 */
fun Window.addCharacterAt(x: Int, y: Int, char: Char) = mvwaddch(handle, y, x, char.code.toUInt())

/**
 * Inserts a character at the current cursor position in this window, shifting existing
 * characters to the right.
 *
 * @param char The character to insert
 */
fun Window.insertCharacter(char: Char) = winsch(handle, char.code.toUInt())

/**
 * Inserts a character at the given position in this window, shifting existing characters to
 * the right.
 *
 * @param x The X coordinate to insert at
 * @param y The Y coordinate to insert at
 * @param char The character to insert
 */
fun Window.insertCharacterAt(x: Int, y: Int, char: Char) =
	mvwinsch(handle, y, x, char.code.toUInt())

/**
 * Deletes the character at the current cursor position in this window.
 */
fun Window.deleteCharacter() = wdelch(handle)

/**
 * Deletes the character at the given position in this window.
 *
 * @param x The X coordinate to delete at
 * @param y The Y coordinate to delete at
 */
fun Window.deleteCharacterAt(x: Int, y: Int) = mvwdelch(handle, y, x)

/**
 * Gets the character currently rendered at the cursor position in this window.
 */
fun Window.getCharacterAtCursor(): Char = winch(handle).toInt().toChar()

/**
 * Gets the character currently rendered at the given position in this window.
 *
 * @param x The X coordinate to query
 * @param y The Y coordinate to query
 */
fun Window.getCharacterOnScreenAt(x: Int, y: Int): Char =
	mvwinch(handle, y, x).toInt().toChar()

// Output - Strings

/**
 * Adds a string to this window at the current cursor position.
 *
 * @param string The string to add
 */
fun Window.addString(string: String) = waddstr(handle, string)

/**
 * Adds a string to this window at the given position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param string The string to add
 */
fun Window.addStringAt(x: Int, y: Int, string: String) = mvwaddstr(handle, y, x, string)

/**
 * Adds at most the given number of characters from a string to this window at the cursor.
 *
 * @param string The string to add
 * @param maxLength The maximum number of characters to add
 */
fun Window.addLimitedString(string: String, maxLength: Int) =
	waddnstr(handle, string, maxLength)

/**
 * Adds at most the given number of characters from a string to this window at the given
 * position.
 *
 * @param x The X coordinate to add at
 * @param y The Y coordinate to add at
 * @param string The string to add
 * @param maxLength The maximum number of characters to add
 */
fun Window.addLimitedStringAt(x: Int, y: Int, string: String, maxLength: Int) =
	mvwaddnstr(handle, y, x, string, maxLength)

/**
 * Prints a message to this window at the current cursor position.
 *
 * Formatting is supported, and the arguments will be substituted into the string in order.
 *
 * @param string The string to print out
 * @param arguments Format arguments to pass to the string
 */
fun Window.print(string: String, vararg arguments: Any?) =
	waddstr(handle, applyFormat(string, arguments))

/**
 * Prints a message to this window at the given position.
 *
 * Formatting is supported, and the arguments will be substituted into the string in order.
 *
 * @param x The X coordinate to print at
 * @param y The Y coordinate to print at
 * @param string The string to print out
 * @param arguments Format arguments to pass to the string
 */
fun Window.print(x: Int, y: Int, string: String, vararg arguments: Any?) =
	mvwaddstr(handle, y, x, applyFormat(string, arguments))

// Input

/**
 * Reads a single character of input from this window.
 */
fun Window.getCharacter(): Char = wgetch(handle).toChar()

/**
 * Reads a single character of input from this window after moving the cursor to the given
 * position.
 *
 * @param x The X coordinate to read from
 * @param y The Y coordinate to read from
 */
fun Window.getCharacterFrom(x: Int, y: Int): Char = mvwgetch(handle, y, x).toChar()

/**
 * Reads a string of input from this window, up to the given number of characters.
 *
 * @param maxLength The maximum number of characters to read
 */
fun Window.getString(maxLength: Int = 256): String {
	require(maxLength > 0) { "maxLength must be greater than zero" }

	return memScoped {
		val buffer = allocArray<ByteVar>(maxLength + 1)
		wgetnstr(handle, buffer, maxLength)
		buffer.toKString()
	}
}

// Attributes

/**
 * Enables a text attribute for this window, such as bold or underline.
 *
 * Multiple attributes can be combined using bitwise OR before passing the result here.
 *
 * @param attribute The attribute bit-mask to enable
 */
fun Window.enableAttribute(attribute: Int) = wattron(handle, attribute)

/**
 * Disables a text attribute that was previously enabled with [enableAttribute].
 *
 * @param attribute The attribute bit-mask to disable
 */
fun Window.disableAttribute(attribute: Int) = wattroff(handle, attribute)

/**
 * Replaces the current set of text attributes for this window with the given attribute.
 *
 * @param attribute The attribute bit-mask to set
 */
fun Window.setAttributes(attribute: Int) = wattrset(handle, attribute)

/**
 * Enables standout mode for this window, the most visible rendering mode the terminal
 * supports.
 */
fun Window.enableStandout() = wstandout(handle)

/**
 * Disables standout mode for this window.
 */
fun Window.disableStandout() = wstandend(handle)

// Background

/**
 * Sets the background character and attribute of this window, replacing every existing
 * occurrence of the previous background character.
 *
 * @param char The new background character
 */
fun Window.setBackground(char: Char) = wbkgd(handle, char.code.toUInt())

/**
 * Sets the background character and attribute of this window without updating already-drawn
 * characters.
 *
 * @param char The new background character
 */
fun Window.setBackgroundOnly(char: Char) = wbkgdset(handle, char.code.toUInt())

/**
 * Gets the current background character of this window.
 */
fun Window.getBackground(): Char = getbkgd(handle).toInt().toChar()

// Lines

/**
 * Draws a box border around this window using the given side characters and the terminal's
 * default corner characters.
 *
 * @param verticalChar The character used for the left and right sides
 * @param horizontalChar The character used for the top and bottom sides
 */
fun Window.drawBox(verticalChar: Char, horizontalChar: Char) =
	box(handle, verticalChar.code.toUInt(), horizontalChar.code.toUInt())

/**
 * Draws a border around this window using a custom character for each segment.
 *
 * @param leftSide The character for the left vertical side
 * @param rightSide The character for the right vertical side
 * @param topSide The character for the top horizontal side
 * @param bottomSide The character for the bottom horizontal side
 * @param topLeft The character for the top-left corner
 * @param topRight The character for the top-right corner
 * @param bottomLeft The character for the bottom-left corner
 * @param bottomRight The character for the bottom-right corner
 */
fun Window.drawBorder(
	leftSide: Char,
	rightSide: Char,
	topSide: Char,
	bottomSide: Char,
	topLeft: Char,
	topRight: Char,
	bottomLeft: Char,
	bottomRight: Char,
) = wborder(
	handle,
	leftSide.code.toUInt(),
	rightSide.code.toUInt(),
	topSide.code.toUInt(),
	bottomSide.code.toUInt(),
	topLeft.code.toUInt(),
	topRight.code.toUInt(),
	bottomLeft.code.toUInt(),
	bottomRight.code.toUInt(),
)

/**
 * Draws a horizontal line of repeated characters in this window starting at the cursor.
 *
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun Window.drawHorizontalLine(char: Char, length: Int) =
	whline(handle, char.code.toUInt(), length)

/**
 * Draws a vertical line of repeated characters in this window starting at the cursor.
 *
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun Window.drawVerticalLine(char: Char, length: Int) =
	wvline(handle, char.code.toUInt(), length)

/**
 * Draws a horizontal line of repeated characters in this window starting at the given
 * position.
 *
 * @param x The X coordinate to start drawing at
 * @param y The Y coordinate to start drawing at
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun Window.drawHorizontalLineAt(x: Int, y: Int, char: Char, length: Int) =
	mvwhline(handle, y, x, char.code.toUInt(), length)

/**
 * Draws a vertical line of repeated characters in this window starting at the given position.
 *
 * @param x The X coordinate to start drawing at
 * @param y The Y coordinate to start drawing at
 * @param char The character to repeat
 * @param length The number of characters to draw
 */
fun Window.drawVerticalLineAt(x: Int, y: Int, char: Char, length: Int) =
	mvwvline(handle, y, x, char.code.toUInt(), length)

// Lines - insert/delete

/**
 * Inserts a blank line above the cursor line in this window.
 */
fun Window.insertLine() = winsertln(handle)

/**
 * Deletes the line at the cursor in this window.
 */
fun Window.deleteLine() = wdeleteln(handle)

/**
 * Inserts (positive count) or deletes (negative count) the given number of lines in this
 * window starting at the cursor.
 *
 * @param count The number of lines to insert (positive) or delete (negative)
 */
fun Window.insertOrDeleteLines(count: Int) = winsdelln(handle, count)

// Scrolling

/**
 * Enables or disables scrolling for this window.
 *
 * When enabled, content automatically scrolls when output reaches the bottom of the window.
 *
 * @param enable Whether to enable scrolling
 */
fun Window.enableScrolling(enable: Boolean) = scrollok(handle, enable)

/**
 * Scrolls this window's contents by the given number of lines.
 *
 * @param lines The number of lines to scroll; positive scrolls up, negative scrolls down
 */
fun Window.scroll(lines: Int) = wscrl(handle, lines)

/**
 * Sets the software scrolling region of this window.
 *
 * Scrolling, when enabled, only affects lines within this region.
 *
 * @param topLine The first line of the scrolling region
 * @param bottomLine The last line of the scrolling region
 */
fun Window.setScrollRegion(topLine: Int, bottomLine: Int) =
	wsetscrreg(handle, topLine, bottomLine)

// Touched lines

/**
 * Marks all lines in this window as touched, forcing a full redraw on the next refresh.
 */
fun Window.touchAll() = touchwin(handle)

/**
 * Marks all lines in this window as untouched, suppressing changes on the next refresh.
 */
fun Window.untouchAll() = untouchwin(handle)

/**
 * Marks (or unmarks) a range of lines in this window as touched.
 *
 * @param startLine The first line to update
 * @param lineCount The number of lines to update
 * @param touched True to mark the lines as touched (will be redrawn), false to mark as
 * untouched
 */
fun Window.touchLines(startLine: Int, lineCount: Int, touched: Boolean) =
	wtouchln(handle, startLine, lineCount, if (touched) 1 else 0)

/**
 * Whether the given line in this window has been touched since the last refresh.
 *
 * @param line The line to query
 */
fun Window.isLineTouched(line: Int): Boolean = is_linetouched(handle, line)

/**
 * Whether this window has any touched lines since the last refresh.
 */
fun Window.isWindowTouched(): Boolean = is_wintouched(handle)

/**
 * Propagates changes in this window up through all of its parent windows so they are
 * reflected on the next refresh.
 */
fun Window.syncUpToParents() = wsyncup(handle)

/**
 * Touches all sub-windows of this window, propagating changes downward on the next refresh.
 */
fun Window.syncDownToChildren() = wsyncdown(handle)

/**
 * Updates the current cursor position of all ancestor windows to match this window's cursor.
 */
fun Window.syncCursorToParents() = wcursyncup(handle)

// Overlays

/**
 * Copies the non-blank characters of this window onto the destination window, leaving blank
 * positions in the destination untouched.
 *
 * @param destination The destination window to copy to
 */
fun Window.overlayOnto(destination: Window) = overlay(handle, destination.handle)

/**
 * Copies all characters of this window onto the destination window, including blank
 * characters.
 *
 * @param destination The destination window to copy to
 */
fun Window.overwriteOnto(destination: Window) = overwrite(handle, destination.handle)

/**
 * Copies a rectangular region of this window onto a destination window.
 *
 * @param destination The destination window to copy to
 * @param sourceX The X coordinate in this window to start copying from
 * @param sourceY The Y coordinate in this window to start copying from
 * @param destinationX The X coordinate in the destination to start copying to
 * @param destinationY The Y coordinate in the destination to start copying to
 * @param destinationEndX The X coordinate in the destination to end copying at
 * @param destinationEndY The Y coordinate in the destination to end copying at
 * @param overlayMode If true, blank characters in this window are not copied (overlay mode);
 * if false, all characters are copied (overwrite mode)
 */
fun Window.copyTo(
	destination: Window,
	sourceX: Int,
	sourceY: Int,
	destinationX: Int,
	destinationY: Int,
	destinationEndX: Int,
	destinationEndY: Int,
	overlayMode: Boolean,
) = copywin(
	handle,
	destination.handle,
	sourceY,
	sourceX,
	destinationY,
	destinationX,
	destinationEndY,
	destinationEndX,
	if (overlayMode) 1 else 0,
)

// Input behavior

/**
 * Enables or disables the keypad on this window.
 *
 * When enabled, special keys such as arrow keys and function keys are returned as single
 * values by input functions rather than as escape sequences.
 *
 * @param enable Whether to enable the keypad
 */
fun Window.setKeypad(enable: Boolean) = keypad(handle, enable)

/**
 * Enables or disables 8-bit input on this window.
 *
 * When enabled, the eighth bit of each input character is not stripped, allowing for input of
 * characters outside the 7-bit ASCII range.
 *
 * @param enable Whether to enable 8-bit input
 */
fun Window.setMeta(enable: Boolean) = meta(handle, enable)

/**
 * Enables or disables no-delay mode on this window.
 *
 * In no-delay mode, character input functions are non-blocking and return without a result if
 * no input is pending.
 *
 * @param enable Whether to enable no-delay mode
 */
fun Window.setNoDelay(enable: Boolean) = nodelay(handle, enable)

/**
 * Enables or disables no-timeout mode on this window.
 *
 * When enabled, ncurses waits indefinitely for the rest of an escape sequence rather than
 * timing out and returning the escape character on its own.
 *
 * @param enable Whether to enable no-timeout mode
 */
fun Window.setNoTimeout(enable: Boolean) = notimeout(handle, enable)

/**
 * Sets the input timeout for this window, in milliseconds.
 *
 * A negative value blocks indefinitely, zero is non-blocking, and a positive value waits up
 * to the given number of milliseconds.
 *
 * @param milliseconds The number of milliseconds to wait
 */
fun Window.setTimeout(milliseconds: Int) = wtimeout(handle, milliseconds)

/**
 * Enables or disables interrupt flushing for this window.
 *
 * @param enable Whether to enable interrupt flushing
 */
fun Window.setInterruptFlush(enable: Boolean) = intrflush(handle, enable)
