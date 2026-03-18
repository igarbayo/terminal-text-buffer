// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollbackTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // Row index -1 must address the most recently pushed scrollback line.
    @Test
    void getLineMinus1ReturnsMostRecentScrollbackLine() {
        buf.setCursor(0, 0);
        buf.writeText("First");
        buf.insertEmptyLine();
        assertEquals("First", buf.getLine(-1));
    }

    // Scrollback is ordered newest-to-oldest: -1 is the newest, -2 is older, etc.
    @Test
    void scrollbackLinesOrderedOldestToNewest() {
        buf.setCursor(0, 0);
        buf.writeText("AAA");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("BBB");
        buf.insertEmptyLine();

        // row -1 = newest = "BBB", row -2 = oldest = "AAA"
        assertEquals("BBB", buf.getLine(-1));
        assertEquals("AAA", buf.getLine(-2));
    }

    // getAllContent must return scrollback lines first, followed by screen lines, all separated by newlines.
    @Test
    void getAllContentReturnScrollbackThenScreen() {
        buf = new TerminalBuffer(3, 2, 100);
        buf.setCursor(0, 0);
        buf.writeText("AAA");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("BBB");

        // scrollback: "AAA"; screen row0: "BBB", row1: blank
        String all = buf.getAllContent();
        String[] lines = all.split("\n", -1);
        assertEquals("AAA", lines[0]);
        assertEquals("BBB", lines[1]);
        assertEquals("   ", lines[2]);
    }

    // Accessing a scrollback row when the scrollback is empty, or a screen row >= height, must throw.
    @Test
    void getLineOutOfBoundsThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(-1),
                "should throw when scrollback is empty");
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(buf.getHeight()),
                "should throw for row >= height");
    }

    // clearScreen must not remove any scrollback lines.
    @Test
    void clearScreenDoesNotAffectScrollback() {
        buf.insertEmptyLine();
        buf.insertEmptyLine();
        buf.clearScreen();
        assertEquals(2, buf.getScrollbackSize());
    }

    // clearAll must remove all scrollback lines and make negative row indices invalid again.
    @Test
    void clearAllResetsScrollbackToZero() {
        buf.insertEmptyLine();
        buf.insertEmptyLine();
        buf.clearAll();
        assertEquals(0, buf.getScrollbackSize());
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(-1));
    }

    // Cell attributes written before the line was pushed to scrollback must be preserved there.
    @Test
    void scrollbackAttributesArePreserved() {
        buf.setForeground(TerminalColor.RED);
        buf.setCursor(0, 0);
        buf.writeText("R");
        buf.insertEmptyLine();

        CellAttributes attrs = buf.getAttributesAt(0, -1);
        assertEquals(TerminalColor.RED, attrs.getForeground());
    }
}
