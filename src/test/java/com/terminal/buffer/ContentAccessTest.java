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

    @Test
    void getLineTrimmsTrailingSpaces() {
        buf.setCursor(0, 0);
        buf.writeText("Hi");
        assertEquals("Hi", buf.getLine(0));
    }

    @Test
    void getLineReturnsEmptyStringForBlankLine() {
        assertEquals("", buf.getLine(0));
    }

    @Test
    void getRawLineReturnsFixedWidth() {
        buf.setCursor(0, 0);
        buf.writeText("Hi");
        assertEquals("Hi   ", buf.getRawLine(0)); // padded to width=5
    }

    @Test
    void getLineScrollbackRow() {
        buf.setCursor(0, 0);
        buf.writeText("ABCDE");
        buf.insertEmptyLine();
        assertEquals("ABCDE", buf.getLine(-1));
    }

    @Test
    void getLineOutOfRangePositiveThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(3));
    }

    @Test
    void getLineOutOfRangeNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> buf.getLine(-1));
    }

    @Test
    void getScreenContentHasHeightMinusOneNewlines() {
        String content = buf.getScreenContent();
        long newlines = content.chars().filter(c -> c == '\n').count();
        assertEquals(buf.getHeight() - 1, newlines);
    }

    @Test
    void getAllContentHasScrollbackPlusScreenLines() {
        buf.insertEmptyLine();
        buf.insertEmptyLine();
        String all = buf.getAllContent();
        long newlines = all.chars().filter(c -> c == '\n').count();
        // 2 scrollback + 3 screen = 5 lines, 4 newlines
        assertEquals(buf.getScrollbackSize() + buf.getHeight() - 1, newlines);
    }

    @Test
    void getCharOnScrollbackRow() {
        buf.setForeground(TerminalColor.RED);
        buf.setCursor(2, 0);
        buf.writeText("X");
        buf.insertEmptyLine();
        assertEquals('X', buf.getChar(2, -1));
    }

    @Test
    void getAttributesAtOnScrollbackRow() {
        buf.setForeground(TerminalColor.BLUE);
        buf.setCursor(1, 0);
        buf.writeText("Y");
        buf.insertEmptyLine();
        assertEquals(TerminalColor.BLUE, buf.getAttributesAt(1, -1).getForeground());
    }

    @Test
    void getLineScrollbackWithTrimming() {
        buf.setCursor(0, 0);
        buf.writeText("AB");
        buf.insertEmptyLine();
        assertEquals("AB", buf.getLine(-1)); // trimmed, not "AB   "
    }
}
