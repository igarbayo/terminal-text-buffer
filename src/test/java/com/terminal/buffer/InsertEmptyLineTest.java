// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertEmptyLineTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // insertEmptyLine must shift all existing screen rows up by one, pushing the top row to scrollback,
    // and place a blank row at the bottom.
    @Test
    void insertEmptyLineShiftsContentUp() {
        buf.setCursor(0, 0);
        buf.writeText("AAAAA");
        buf.setCursor(0, 1);
        buf.writeText("BBBBB");
        buf.setCursor(0, 2);
        buf.writeText("CCCCC");

        buf.insertEmptyLine();

        // Old row 0 → scrollback; old row 1 → row 0; old row 2 → row 1; new blank → row 2
        assertEquals("BBBBB", buf.getLine(0));
        assertEquals("CCCCC", buf.getLine(1));
        assertEquals("", buf.getLine(2));
    }

    // The row that was pushed off the top of the screen must appear as the most recent scrollback line.
    @Test
    void insertEmptyLinePushesTopRowToScrollback() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.insertEmptyLine();

        assertEquals(1, buf.getScrollbackSize());
        assertEquals("Hello", buf.getLine(-1));
    }

    // Each call to insertEmptyLine must increase the scrollback size by exactly one.
    @Test
    void scrollbackSizeIncrementsWithEachInsert() {
        for (int i = 0; i < 5; i++) {
            buf.insertEmptyLine();
        }
        assertEquals(5, buf.getScrollbackSize());
    }

    // When the scrollback is full, the oldest entry must be evicted to make room for the new one.
    @Test
    void scrollbackEvictsOldestWhenFull() {
        buf = new TerminalBuffer(5, 1, 3); // maxScrollback=3
        buf.setCursor(0, 0);
        buf.writeText("A");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("B");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("C");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("D");
        buf.insertEmptyLine(); // 'A' should be evicted now

        assertEquals(3, buf.getScrollbackSize());
        assertEquals("D", buf.getLine(-1));
        assertEquals("C", buf.getLine(-2));
        assertEquals("B", buf.getLine(-3));
    }

    // When maxScrollback is 0, the evicted row must be silently discarded.
    @Test
    void insertEmptyLineWithZeroMaxScrollbackDiscardsRow() {
        buf = new TerminalBuffer(5, 2, 0);
        buf.setCursor(0, 0);
        buf.writeText("Lost");
        buf.insertEmptyLine();
        assertEquals(0, buf.getScrollbackSize());
    }

    // The newly inserted bottom row must be blank (empty string after trimming).
    @Test
    void newBottomRowIsBlank() {
        buf.insertEmptyLine();
        assertEquals("", buf.getLine(buf.getHeight() - 1));
    }
}
