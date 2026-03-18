// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertTextTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // Inserting into an empty row must write the characters starting at the cursor column.
    @Test
    void insertTextAtStartOfEmptyRowWritesChars() {
        buf.setCursor(0, 0);
        buf.insertText("Hi");
        assertEquals('H', buf.getChar(0, 0));
        assertEquals('i', buf.getChar(1, 0));
    }

    // Inserting in the middle of a row must push existing characters to the right;
    // characters that overflow the row width wrap to the next row.
    @Test
    void insertTextShiftsExistingContentRight() {
        buf.setCursor(0, 0);
        buf.writeText("ABCDE"); // fills row 0
        buf.setCursor(2, 0);
        buf.insertText("XY");
        // Before: A B C D E
        // Insert XY at col 2 → A B X Y C  (D E wrap to row 1)
        assertEquals("ABXYC", buf.getRawLine(0).substring(0, 5));
        assertEquals("DE", buf.getLine(1));
    }

    // Inserting at column 0 must shift every existing character right by one position per inserted char.
    @Test
    void insertTextAtColumnZeroShiftsEntireRow() {
        buf.setCursor(0, 0);
        buf.writeText("ABCDE");
        buf.setCursor(0, 0);
        buf.insertText("Z");
        // Z at col 0, ABCD shift to cols 1-4, only E overflows to row 1
        assertEquals('Z', buf.getChar(0, 0));
        assertEquals('A', buf.getChar(1, 0));
        assertEquals('B', buf.getChar(2, 0));
        assertEquals('C', buf.getChar(3, 0));
        assertEquals('D', buf.getChar(4, 0));
        assertEquals("E", buf.getLine(1));
    }

    // When the inserted text fits exactly up to the end of the row, no wrapping must occur.
    @Test
    void insertTextFillsExactlyToEndOfLineNoWrap() {
        buf.setCursor(3, 0);
        buf.insertText("AB"); // 2 chars fit in cols 3-4 exactly
        assertEquals('A', buf.getChar(3, 0));
        assertEquals('B', buf.getChar(4, 0));
        assertEquals("", buf.getLine(1), "no wrap needed, row 1 must be empty");
    }

    // When overflow reaches the last row, the top row must scroll into the scrollback.
    @Test
    void insertTextWrappingAtLastRowCausesScroll() {
        buf = new TerminalBuffer(3, 2, 100);
        buf.setCursor(0, 0);
        buf.writeText("ABC");
        buf.setCursor(0, 1);
        buf.writeText("DEF");
        // Now insert at row 1 col 0 causing overflow
        buf.setCursor(0, 1);
        buf.insertText("XY");
        // Expected: row 0 gets "ABC" pushed to scrollback, screen:
        // row0: DEF shifted → X Y D (E F pushed and scrolled)
        assertEquals(1, buf.getScrollbackSize(), "top row should have scrolled to scrollback");
    }

    // The cursor must advance past all inserted characters; wrapping to the next row if necessary.
    @Test
    void insertTextAdvancesCursorPastInsertedContent() {
        buf.setCursor(0, 0);
        buf.insertText("ABCDE"); // fills row 0, cursor should be at start of row 1
        // After inserting 5 chars in a width-5 row from col 0, cursor wraps to (0,1)
        assertEquals(0, buf.getCursor().getCol());
        assertEquals(1, buf.getCursor().getRow());
    }

    // insertText must stamp the current buffer attributes onto every character it writes.
    @Test
    void insertTextUsesCurrentAttributes() {
        buf.setForeground(TerminalColor.GREEN);
        buf.setCursor(0, 0);
        buf.insertText("A");
        assertEquals(TerminalColor.GREEN, buf.getAttributesAt(0, 0).getForeground());
    }

    // Inserting an empty string must be a no-op (cursor and content unchanged).
    @Test
    void insertEmptyStringIsNoOp() {
        buf.setCursor(2, 1);
        buf.insertText("");
        assertEquals(2, buf.getCursor().getCol());
        assertEquals(1, buf.getCursor().getRow());
    }
}
