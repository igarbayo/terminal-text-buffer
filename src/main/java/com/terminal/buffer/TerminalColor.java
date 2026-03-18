// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

// TerminalColor is an enumeration representing the standard terminal colors, including both normal and bright variants, as well as a default color that can be used to indicate no specific color preference. This enum can be used in CellAttributes to specify the foreground and background colors of terminal cells.
public enum TerminalColor {
    DEFAULT,
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
    BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
    BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE
}
