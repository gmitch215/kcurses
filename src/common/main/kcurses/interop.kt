@file:OptIn(ExperimentalForeignApi::class)

package kcurses

import kotlinx.cinterop.ExperimentalForeignApi
import ncurses.endwin
import ncurses.initscr
import ncurses.keypad
import ncurses.raw
import ncurses.stdscr

/**
 * Starts curses mode.
 */
fun start() = initscr()

/**
 * Disables line buffering.
 */
fun noLineBuffering() = raw()

/**
 * Ends curses mode.
 */
fun end() = endwin()

/**
 * Enables or disables the keypad of the user's terminal.
 * @param enable Whether to enable the keypad.
 */
fun keypad(enable: Boolean) = keypad(stdscr, enable)
