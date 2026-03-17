// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentAccessTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    // getLine must strip trailing spaces so callers receive a clean string.
    @Test
    void getLineTrimmsTrailingSpaces() {
        buf.setCursor(0, 0);
        buf.writeText("Hi");
        assertEquals("Hi", buf.getLine(0));
    }

    // getLine on a blank row must return an empty string (all spaces trimmed).
    @Test
    void getLineReturnsEmptyStringForBlankLine() {
        assertEquals("", buf.getLine(0));
    }

    // getRawLine must return the full fixed-width content including trailing spaces.
    @Test
    void getRawLineReturnsFixedWidth() {
        buf.setCursor(0, 0);
        buf.writeText("Hi");
        assertEquals("Hi   ", buf.getRawLine(0)); // padded to width=5
    }

    // getLine with a negative index must address the scrollback (row -1 = most recent).
    @Test
    void getLineScrollbackRow() {
        buf.setCursor(0, 0);
        buf.writeText("ABCDE");
        buf.insertEmptyLine();
        assertEquals("ABCDE", buf.getLine(-1));
    }

    // getLine with a screen row index >= height must throw IllegalArgumentException.
    @Test
    void getLineOutOfRangePositiveThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(3));
    }

    // getLine with a negative index when the scrollback is empty must throw IllegalArgumentException.
    @Test
    void getLineOutOfRangeNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(-1));
    }

    // getScreenContent must contain exactly height-1 newline characters.
    @Test
    void getScreenContentHasHeightMinusOneNewlines() {
        String content = buf.getScreenContent();
        long newlines = content.chars().filter(c -> c == '\n').count();
        assertEquals(buf.getHeight() - 1, newlines);
    }

    // getAllContent must contain scrollback lines followed by screen lines separated by newlines.
    @Test
    void getAllContentHasScrollbackPlusScreenLines() {
        buf.insertEmptyLine();
        buf.insertEmptyLine();
        String all = buf.getAllContent();
        long newlines = all.chars().filter(c -> c == '\n').count();
        // 2 scrollback + 3 screen = 5 lines, 4 newlines
        assertEquals(buf.getScrollbackSize() + buf.getHeight() - 1, newlines);
    }

    // getChar must be able to read a character from a scrollback row using a negative index.
    @Test
    void getCharOnScrollbackRow() {
        buf.setForeground(TerminalColor.RED);
        buf.setCursor(2, 0);
        buf.writeText("X");
        buf.insertEmptyLine();
        assertEquals('X', buf.getChar(2, -1));
    }

    // getAttributesAt must be able to read attributes from a scrollback row using a negative index.
    @Test
    void getAttributesAtOnScrollbackRow() {
        buf.setForeground(TerminalColor.BLUE);
        buf.setCursor(1, 0);
        buf.writeText("Y");
        buf.insertEmptyLine();
        assertEquals(TerminalColor.BLUE, buf.getAttributesAt(1, -1).getForeground());
    }

    // getLine on a scrollback row must also trim trailing spaces.
    @Test
    void getLineScrollbackWithTrimming() {
        buf.setCursor(0, 0);
        buf.writeText("AB");
        buf.insertEmptyLine();
        assertEquals("AB", buf.getLine(-1)); // trimmed, not "AB   "
    }
}
