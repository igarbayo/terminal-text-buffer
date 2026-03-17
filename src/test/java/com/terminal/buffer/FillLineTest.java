// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FillLineTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // fillLine must overwrite every column of the cursor's row with the given character.
    @Test
    void fillLineFillesEntireCurrentRowWithChar() {
        buf.setCursor(2, 1); // cursor on row 1, col 2
        buf.fillLine('X');
        for (int col = 0; col < 5; col++) {
            assertEquals('X', buf.getChar(col, 1));
        }
    }

    // fillLine must only affect the cursor's row; other rows must remain untouched.
    @Test
    void fillLineDoesNotAffectOtherRows() {
        buf.setCursor(0, 1);
        buf.fillLine('Z');
        assertEquals(' ', buf.getChar(0, 0), "row 0 must be untouched");
        assertEquals(' ', buf.getChar(0, 2), "row 2 must be untouched");
    }

    // fillLine must stamp the current buffer attributes onto every cell it writes.
    @Test
    void fillLineUsesCurrentAttributes() {
        buf.setForeground(TerminalColor.CYAN);
        buf.setCursor(0, 0);
        buf.fillLine('A');
        assertEquals(TerminalColor.CYAN, buf.getAttributesAt(2, 0).getForeground());
    }

    // fillLine must not move the cursor; its position must be the same before and after.
    @Test
    void fillLineLeavsCursorColumnUnchanged() {
        buf.setCursor(3, 0);
        buf.fillLine('A');
        assertEquals(3, buf.getCursor().getCol());
        assertEquals(0, buf.getCursor().getRow());
    }

    // fillLine with a space character is equivalent to clearing the entire row.
    @Test
    void fillLineWithSpaceClearsRow() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.setCursor(0, 0);
        buf.fillLine(' ');
        for (int col = 0; col < 5; col++) {
            assertEquals(' ', buf.getChar(col, 0));
        }
    }
}
