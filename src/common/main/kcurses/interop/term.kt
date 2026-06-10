@file:OptIn(ExperimentalForeignApi::class)

package kcurses.interop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import ncurses.putp
import ncurses.restartterm
import ncurses.setupterm
import ncurses.tigetflag
import ncurses.tigetnum
import ncurses.tigetstr
import ncurses.tparm

/**
 * The result of initializing or restarting a terminfo entry via [setupTerminal] or
 * [restartTerminal].
 *
 * @property status The result of the underlying call, either `OK` (0) or `ERR` (-1)
 * @property detail The detail code reported by ncurses: `1` if the call succeeded, `0` if
 * the terminal type was not found in the terminfo database, or `-1` if the terminfo database
 * itself was missing
 */
data class TerminalSetupResult(val status: Int, val detail: Int)

/**
 * Initializes the terminfo database for the given terminal name.
 *
 * Must be called before any of the other terminfo query functions in this file unless ncurses
 * has already initialized the terminal via [start].
 *
 * @param name The terminal name to look up, or `null` to use the `TERM` environment variable
 * @param fileDescriptor The file descriptor of the terminal, typically `1` for standard output
 */
fun setupTerminal(name: String? = null, fileDescriptor: Int = 1): TerminalSetupResult = memScoped {
    val errret = alloc<IntVar>()
    val status = setupterm(name, fileDescriptor, errret.ptr)
    TerminalSetupResult(status, errret.value)
}

/**
 * Re-initializes the terminfo database for a different terminal type.
 *
 * Unlike [setupTerminal], this function preserves the previously selected terminal's state
 * rather than freeing it, allowing the caller to switch back later.
 *
 * @param name The terminal name to switch to
 * @param fileDescriptor The file descriptor of the terminal
 */
fun restartTerminal(name: String, fileDescriptor: Int = 1): TerminalSetupResult = memScoped {
    val errret = alloc<IntVar>()
    val status = restartterm(name, fileDescriptor, errret.ptr)
    TerminalSetupResult(status, errret.value)
}

/**
 * Looks up a terminfo string capability by its short name.
 *
 * Common string capabilities include `clear` (clear the screen), `cup` (move cursor to a
 * given position), `bel` (audible bell), `setaf` (set foreground color), and `setab` (set
 * background color). The full list is documented in the terminfo(5) manual.
 *
 * @param name The terminfo capability short name
 * @return The capability string, or `null` if the capability is not supported by this
 * terminal
 */
fun queryStringCapability(name: String): String? = tigetstr(name)?.toKString()

/**
 * Looks up a terminfo numeric capability by its short name.
 *
 * Common numeric capabilities include `lines` (number of rows), `cols` (number of columns),
 * and `colors` (number of colors supported).
 *
 * @param name The terminfo capability short name
 * @return The numeric value of the capability, `-1` if the capability is not present, or
 * `-2` if the capability is not a numeric one
 */
fun queryNumberCapability(name: String): Int = tigetnum(name)

/**
 * Looks up a terminfo boolean capability by its short name.
 *
 * Common boolean capabilities include `am` (auto margin), `bce` (back color erase), and
 * `xenl` (newline glitch).
 *
 * @param name The terminfo capability short name
 * @return `true` if the capability is present, `false` otherwise
 */
fun queryFlagCapability(name: String): Boolean = tigetflag(name) == 1

/**
 * Substitutes parameters into a terminfo capability string.
 *
 * Many capability strings — such as those for cursor movement — contain placeholders that
 * must be substituted with concrete values before they can be sent to the terminal. This
 * wraps `tparm` to perform that substitution.
 *
 * @param format The capability string containing placeholders
 * @return The expanded string, or `null` if substitution failed
 */
fun substituteParameters(format: String): String? = tparm(format)?.toKString()

/**
 * Outputs a terminfo capability string to the terminal.
 *
 * Use this together with [queryStringCapability] (and [substituteParameters] when needed) to
 * render a capability without going through the higher-level curses output functions.
 *
 * @param capability The capability string to output
 */
fun outputCapabilityString(capability: String) = putp(capability)
