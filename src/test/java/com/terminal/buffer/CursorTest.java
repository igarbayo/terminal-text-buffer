// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(10, 5, 100);
    }

    // A freshly created buffer must place the cursor at the origin (0, 0).
    @Test
    void initialCursorIsAtOrigin() {
        CursorPosition pos = buf.getCursor();
        assertEquals(0, pos.getCol());
        assertEquals(0, pos.getRow());
    }

    // setCursor must move the cursor to the exact requested position.
    @Test
    void setCursorMovesToPosition() {
        buf.setCursor(3, 2);
        assertEquals(3, buf.getCursor().getCol());
        assertEquals(2, buf.getCursor().getRow());
    }

    // A negative column must be clamped to 0.
    @Test
    void setCursorClampsNegativeColToZero() {
        buf.setCursor(-5, 0);
        assertEquals(0, buf.getCursor().getCol());
    }

    // A negative row must be clamped to 0.
    @Test
    void setCursorClampsNegativeRowToZero() {
        buf.setCursor(0, -1);
        assertEquals(0, buf.getCursor().getRow());
    }

    // A column beyond the right edge (>= width) must be clamped to width-1.
    @Test
    void setCursorClampsColBeyondWidth() {
        buf.setCursor(10, 0);  // width=10, valid cols 0..9
        assertEquals(9, buf.getCursor().getCol());
    }

    // A row beyond the bottom edge (>= height) must be clamped to height-1.
    @Test
    void setCursorClampsRowBeyondHeight() {
        buf.setCursor(0, 5);  // height=5, valid rows 0..4
        assertEquals(4, buf.getCursor().getRow());
    }

    // moveCursorRight(n) must advance the column by n steps.
    @Test
    void moveCursorRightByN() {
        buf.setCursor(2, 0);
        buf.moveCursorRight(3);
        assertEquals(5, buf.getCursor().getCol());
    }

    // moveCursorRight must stop at the rightmost valid column when the step overshoots.
    @Test
    void moveCursorRightClampsAtRightEdge() {
        buf.setCursor(8, 0);
        buf.moveCursorRight(100);
        assertEquals(9, buf.getCursor().getCol());
    }

    // moveCursorLeft(n) must retreat the column by n steps.
    @Test
    void moveCursorLeftByN() {
        buf.setCursor(7, 0);
        buf.moveCursorLeft(3);
        assertEquals(4, buf.getCursor().getCol());
    }

    // moveCursorLeft must stop at column 0 when the step overshoots.
    @Test
    void moveCursorLeftClampsAtLeftEdge() {
        buf.setCursor(2, 0);
        buf.moveCursorLeft(100);
        assertEquals(0, buf.getCursor().getCol());
    }

    // moveCursorDown(n) must advance the row by n steps.
    @Test
    void moveCursorDownByN() {
        buf.setCursor(0, 1);
        buf.moveCursorDown(2);
        assertEquals(3, buf.getCursor().getRow());
    }

    // moveCursorDown must stop at the bottom row when the step overshoots.
    @Test
    void moveCursorDownClampsAtBottom() {
        buf.setCursor(0, 3);
        buf.moveCursorDown(100);
        assertEquals(4, buf.getCursor().getRow());
    }

    // moveCursorUp(n) must retreat the row by n steps.
    @Test
    void moveCursorUpByN() {
        buf.setCursor(0, 4);
        buf.moveCursorUp(2);
        assertEquals(2, buf.getCursor().getRow());
    }

    // moveCursorUp must stop at row 0 when the step overshoots.
    @Test
    void moveCursorUpClampsAtTop() {
        buf.setCursor(0, 1);
        buf.moveCursorUp(100);
        assertEquals(0, buf.getCursor().getRow());
    }

    // Moving by zero in any direction must leave the cursor exactly where it was.
    @Test
    void moveCursorByZeroIsNoOp() {
        buf.setCursor(3, 2);
        buf.moveCursorRight(0);
        buf.moveCursorLeft(0);
        buf.moveCursorUp(0);
        buf.moveCursorDown(0);
        assertEquals(3, buf.getCursor().getCol());
        assertEquals(2, buf.getCursor().getRow());
    }

    // getWidth and getHeight must return the dimensions passed to the constructor.
    @Test
    void bufferExposesWidthAndHeight() {
        assertEquals(10, buf.getWidth());
        assertEquals(5, buf.getHeight());
    }

    // getMaxScrollback must return the limit passed to the constructor.
    @Test
    void bufferExposesMaxScrollback() {
        assertEquals(100, buf.getMaxScrollback());
    }
}
