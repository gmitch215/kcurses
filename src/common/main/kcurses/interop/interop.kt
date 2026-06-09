@file:OptIn(ExperimentalForeignApi::class)

package kcurses.interop

import kotlinx.cinterop.ExperimentalForeignApi
import ncurses.COLOR_PAIR
import ncurses.addch
import ncurses.addstr
import ncurses.attron
import ncurses.can_change_color
import ncurses.cbreak
import ncurses.echo
import ncurses.endwin
import ncurses.getch
import ncurses.has_colors
import ncurses.init_color
import ncurses.init_pair
import ncurses.initscr
import ncurses.keypad
import ncurses.nocbreak
import ncurses.noecho
import ncurses.start_color
import ncurses.stdscr

/**
 * Starts curses mode.
 */
fun start() = initscr()

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
 * Tells the terminal to use a color pair to set the visuals of the terminal.
 * @param index The index of the color pair to use.
 */
fun useColorPair(index: Int) {
	require(index >= 0) { "index must be greater than zero" }

	attron(COLOR_PAIR(index))
}

/**
 * Adds a character to the buffer at the current cursor position.
 * @param char The character to add.
 */
fun addCharacter(char: Char) = addch(char.code.toUInt())

/**
 * Adds a string to the buffer at the current cursor position.
 * @param string The string to add.
 */
fun addString(string: String) = addstr(string)

/**
 * Gets the character input 
 */
fun getCharacter(): Char = getch().toChar()

/**
 * Ends curses mode.
 */
fun end() = endwin()
