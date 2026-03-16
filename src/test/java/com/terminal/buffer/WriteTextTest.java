package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WriteTextTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(10, 5, 100);
    }

    @Test
    void writeTextPlacesCharsAtCursorAndAdvancesCursor() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        assertEquals('H', buf.getChar(0, 0));
        assertEquals('e', buf.getChar(1, 0));
        assertEquals('l', buf.getChar(2, 0));
        assertEquals('l', buf.getChar(3, 0));
        assertEquals('o', buf.getChar(4, 0));
        assertEquals(5, buf.getCursor().getCol());
        assertEquals(0, buf.getCursor().getRow());
    }

    @Test
    void writeTextOverwritesExistingContent() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.setCursor(2, 0);
        buf.writeText("AB");
        assertEquals('H', buf.getChar(0, 0));
        assertEquals('e', buf.getChar(1, 0));
        assertEquals('A', buf.getChar(2, 0));
        assertEquals('B', buf.getChar(3, 0));
        assertEquals('o', buf.getChar(4, 0));
    }

    @Test
    void writeTextStoresCurrentAttributes() {
        buf.setForeground(TerminalColor.RED);
        buf.addStyle(TextStyle.BOLD);
        buf.setCursor(0, 0);
        buf.writeText("X");
        CellAttributes attrs = buf.getAttributesAt(0, 0);
        assertEquals(TerminalColor.RED, attrs.getForeground());
        assertTrue(attrs.hasStyle(TextStyle.BOLD));
    }

    @Test
    void writeTextOnNonZeroRow() {
        buf.setCursor(0, 2);
        buf.writeText("Hi");
        assertEquals('H', buf.getChar(0, 2));
        assertEquals('i', buf.getChar(1, 2));
        assertEquals(' ', buf.getChar(0, 0), "other rows must be untouched");
    }

    @Test
    void writeEmptyStringIsNoOp() {
        buf.setCursor(3, 1);
        buf.writeText("");
        assertEquals(3, buf.getCursor().getCol());
        assertEquals(1, buf.getCursor().getRow());
    }

    @Test
    void writeTextNullIsNoOp() {
        buf.setCursor(3, 1);
        buf.writeText(null);
        assertEquals(3, buf.getCursor().getCol());
    }

    @Test
    void writeTextTruncatesAtRightEdgeWithPendingWrap() {
        // width=10; cursor at col 8, write "AB"
        buf.setCursor(8, 0);
        buf.writeText("AB");
        // 'A' at col 8, 'B' at col 9, cursor stays at 9 with pending wrap
        assertEquals('A', buf.getChar(8, 0));
        assertEquals('B', buf.getChar(9, 0));
        assertEquals(9, buf.getCursor().getCol());
    }

    @Test
    void pendingWrapCausesNextWriteToWrapToNextLine() {
        buf.setCursor(9, 0); // last col
        buf.writeText("A");   // writes 'A' at col 9, sets pending wrap
        buf.writeText("B");   // should wrap to (0, 1) then write 'B'
        assertEquals('A', buf.getChar(9, 0));
        assertEquals('B', buf.getChar(0, 1));
        assertEquals(1, buf.getCursor().getCol());
        assertEquals(1, buf.getCursor().getRow());
    }

    @Test
    void pendingWrapAtLastRowScrolls() {
        buf = new TerminalBuffer(5, 2, 100);
        buf.setCursor(4, 1);  // last col, last row
        buf.writeText("A");   // writes 'A' at (4,1), sets pending wrap
        buf.writeText("B");   // scroll occurs: old row 1 → row 0, new row 1 gets 'B'
        assertEquals('A', buf.getChar(4, 0)); // 'A' shifted up to row 0 after scroll
        assertEquals('B', buf.getChar(0, 1)); // 'B' at start of new bottom row
        assertEquals(1, buf.getScrollbackSize());
    }

    @Test
    void setCursorClearsPendingWrap() {
        buf.setCursor(9, 0);
        buf.writeText("X"); // pending wrap
        buf.setCursor(5, 0); // explicit setCursor should cancel pending wrap
        buf.writeText("Y");  // should write at (5,0), not wrap
        assertEquals('Y', buf.getChar(5, 0));
        assertEquals(0, buf.getCursor().getRow());
    }

    @Test
    void writeTextOnStartOfNonFirstRow() {
        buf.setCursor(0, 2);
        buf.writeText("Test");
        assertEquals('T', buf.getChar(0, 2));
        assertEquals('e', buf.getChar(1, 2));
        assertEquals(' ', buf.getChar(0, 1), "previous row untouched");
    }
}
