// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResizeTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    @Test
    void resizeToSameDimensionsIsNoOp() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.resize(5, 3);
        assertEquals("Hello", buf.getLine(0));
        assertEquals(5, buf.getWidth());
        assertEquals(3, buf.getHeight());
    }

    @Test
    void resizeWiderPadsRowsWithEmptyCells() {
        buf.setCursor(0, 0);
        buf.writeText("Hi");
        buf.resize(8, 3);
        assertEquals(8, buf.getWidth());
        assertEquals("Hi      ", buf.getRawLine(0));
    }

    @Test
    void resizeNarrowerTruncatesRows() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.resize(3, 3);
        assertEquals(3, buf.getWidth());
        assertEquals("Hel", buf.getRawLine(0));
    }

    @Test
    void resizeTallerAddsEmptyRowsAtBottom() {
        buf.resize(5, 5);
        assertEquals(5, buf.getHeight());
        assertEquals("", buf.getLine(3));
        assertEquals("", buf.getLine(4));
    }

    @Test
    void resizeShorterPushesTopRowsToScrollback() {
        buf.setCursor(0, 0);
        buf.writeText("AAAAA");
        buf.setCursor(0, 1);
        buf.writeText("BBBBB");
        buf.setCursor(0, 2);
        buf.writeText("CCCCC");

        buf.resize(5, 2); // lose row 0 → scrollback

        assertEquals(2, buf.getHeight());
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("AAAAA", buf.getLine(-1));
        assertEquals("BBBBB", buf.getLine(0));
        assertEquals("CCCCC", buf.getLine(1));
    }

    @Test
    void resizeClampsCursorToNewBounds() {
        buf.setCursor(4, 2);
        buf.resize(3, 2);
        assertEquals(2, buf.getCursor().getCol(), "cursor col clamped to new width-1");
        assertEquals(1, buf.getCursor().getRow(), "cursor row clamped to new height-1");
    }

    @Test
    void resizeWithInvalidWidthThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.resize(0, 3));
        assertThrows(IllegalArgumentException.class, () -> buf.resize(-1, 3));
    }

    @Test
    void resizeWithInvalidHeightThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.resize(5, 0));
    }
}
