// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearScreenTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // A fresh buffer must render as a grid of spaces (no content written yet).
    @Test
    void freshBufferScreenContentIsAllSpaces() {
        String content = buf.getScreenContent();
        // 3 rows of 5 spaces joined by newlines
        assertEquals("     \n     \n     ", content);
    }

    // clearScreen must reset every cell on the screen to a space with default attributes.
    @Test
    void clearScreenResetsAllCellsToEmpty() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.clearScreen();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                assertEquals(' ', buf.getChar(col, row));
                assertEquals(CellAttributes.DEFAULT, buf.getAttributesAt(col, row));
            }
        }
    }

    // clearScreen must leave the scrollback buffer intact.
    @Test
    void clearScreenDoesNotRemoveScrollback() {
        // Push something into scrollback
        buf.insertEmptyLine(); // pushes row 0 → scrollback
        assertEquals(1, buf.getScrollbackSize());

        buf.clearScreen();
        assertEquals(1, buf.getScrollbackSize(), "clearScreen must not touch scrollback");
    }

    // clearAll must wipe the scrollback completely.
    @Test
    void clearAllRemovesScrollback() {
        buf.insertEmptyLine();
        buf.insertEmptyLine();
        assertEquals(2, buf.getScrollbackSize());

        buf.clearAll();
        assertEquals(0, buf.getScrollbackSize());
    }

    // clearAll must also clear the screen content, not only the scrollback.
    @Test
    void clearAllAlsoClearsScreen() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.clearAll();
        assertEquals(' ', buf.getChar(0, 0));
    }

    // getScreenContent must return exactly height lines each of exactly width characters.
    @Test
    void getScreenContentHasCorrectDimensions() {
        String content = buf.getScreenContent();
        String[] lines = content.split("\n", -1);
        assertEquals(3, lines.length);
        for (String line : lines) {
            assertEquals(5, line.length());
        }
    }
}
