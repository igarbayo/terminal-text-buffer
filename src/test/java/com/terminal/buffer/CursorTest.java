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

    @Test
    void initialCursorIsAtOrigin() {
        CursorPosition pos = buf.getCursor();
        assertEquals(0, pos.getCol());
        assertEquals(0, pos.getRow());
    }

    @Test
    void setCursorMovesToPosition() {
        buf.setCursor(3, 2);
        assertEquals(3, buf.getCursor().getCol());
        assertEquals(2, buf.getCursor().getRow());
    }

    @Test
    void setCursorClampsNegativeColToZero() {
        buf.setCursor(-5, 0);
        assertEquals(0, buf.getCursor().getCol());
    }

    @Test
    void setCursorClampsNegativeRowToZero() {
        buf.setCursor(0, -1);
        assertEquals(0, buf.getCursor().getRow());
    }

    @Test
    void setCursorClampsColBeyondWidth() {
        buf.setCursor(10, 0);  // width=10, valid cols 0..9
        assertEquals(9, buf.getCursor().getCol());
    }

    @Test
    void setCursorClampsRowBeyondHeight() {
        buf.setCursor(0, 5);  // height=5, valid rows 0..4
        assertEquals(4, buf.getCursor().getRow());
    }

    @Test
    void moveCursorRightByN() {
        buf.setCursor(2, 0);
        buf.moveCursorRight(3);
        assertEquals(5, buf.getCursor().getCol());
    }

    @Test
    void moveCursorRightClampsAtRightEdge() {
        buf.setCursor(8, 0);
        buf.moveCursorRight(100);
        assertEquals(9, buf.getCursor().getCol());
    }

    @Test
    void moveCursorLeftByN() {
        buf.setCursor(7, 0);
        buf.moveCursorLeft(3);
        assertEquals(4, buf.getCursor().getCol());
    }

    @Test
    void moveCursorLeftClampsAtLeftEdge() {
        buf.setCursor(2, 0);
        buf.moveCursorLeft(100);
        assertEquals(0, buf.getCursor().getCol());
    }

    @Test
    void moveCursorDownByN() {
        buf.setCursor(0, 1);
        buf.moveCursorDown(2);
        assertEquals(3, buf.getCursor().getRow());
    }

    @Test
    void moveCursorDownClampsAtBottom() {
        buf.setCursor(0, 3);
        buf.moveCursorDown(100);
        assertEquals(4, buf.getCursor().getRow());
    }

    @Test
    void moveCursorUpByN() {
        buf.setCursor(0, 4);
        buf.moveCursorUp(2);
        assertEquals(2, buf.getCursor().getRow());
    }

    @Test
    void moveCursorUpClampsAtTop() {
        buf.setCursor(0, 1);
        buf.moveCursorUp(100);
        assertEquals(0, buf.getCursor().getRow());
    }

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

    @Test
    void bufferExposesWidthAndHeight() {
        assertEquals(10, buf.getWidth());
        assertEquals(5, buf.getHeight());
    }

    @Test
    void bufferExposesMaxScrollback() {
        assertEquals(100, buf.getMaxScrollback());
    }
}
