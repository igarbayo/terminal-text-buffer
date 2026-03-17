// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WideCharTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(8, 4, 100);
    }

    // Writing a CJK character must place a WIDE_LEFT cell at the cursor column
    // and a WIDE_RIGHT placeholder cell in the next column.
    @Test
    void writingCjkCharOccupiesTwoColumns() {
        buf.setCursor(0, 0);
        buf.writeText("字");
        assertEquals('字', buf.getChar(0, 0));
        assertEquals('\0', buf.getChar(1, 0), "right placeholder must be null char");
        assertEquals(Cell.CellType.WIDE_LEFT, buf.getCell(0, 0).getType());
        assertEquals(Cell.CellType.WIDE_RIGHT, buf.getCell(1, 0).getType());
    }

    // After writing a wide character, the cursor must advance by two columns.
    @Test
    void cursorAdvancesTwoAfterWideChar() {
        buf.setCursor(0, 0);
        buf.writeText("字");
        assertEquals(2, buf.getCursor().getCol());
    }

    // getLine must treat a wide character as a single logical character (not two columns).
    @Test
    void getLineTreatsWideCharAsOneCharacter() {
        buf.setCursor(0, 0);
        buf.writeText("字A");
        String line = buf.getLine(0);
        // "字A" — wide char is one logical character followed by 'A'
        assertTrue(line.startsWith("字A"), "line should start with wide char then A, got: " + line);
    }

    // A wide character that would start at the last column must not be written
    // because there is no room for its right half; the column must remain empty.
    @Test
    void wideCharAtLastColumnIsTruncated() {
        // width=8; position wide char at col 7 (last) — no room for both halves
        buf.setCursor(7, 0);
        buf.writeText("字");
        // Wide char truncated: col 7 stays empty or gets placeholder behaviour
        // The implementation writes nothing and moves to pending wrap
        assertEquals(' ', buf.getChar(7, 0), "wide char at last col should not be written");
    }

    // Writing a new character over the WIDE_LEFT cell of an existing wide character
    // must also clear the corresponding WIDE_RIGHT placeholder.
    @Test
    void writingOverWideLeftClearsBothHalves() {
        buf.setCursor(0, 0);
        buf.writeText("字"); // WIDE_LEFT at 0, WIDE_RIGHT at 1
        buf.setCursor(0, 0);
        buf.writeText("A");  // overwrite left half
        assertEquals('A', buf.getChar(0, 0));
        assertEquals(' ', buf.getChar(1, 0), "right placeholder must be cleared when left is overwritten");
        assertEquals(Cell.CellType.NORMAL, buf.getCell(0, 0).getType());
        assertEquals(Cell.CellType.NORMAL, buf.getCell(1, 0).getType());
    }

    // Writing a new character over the WIDE_RIGHT placeholder must also clear the WIDE_LEFT cell.
    @Test
    void writingOverWideRightClearsBothHalves() {
        buf.setCursor(0, 0);
        buf.writeText("字"); // WIDE_LEFT at 0, WIDE_RIGHT at 1
        buf.setCursor(1, 0);
        buf.writeText("B");  // overwrite right half (placeholder)
        assertEquals(' ', buf.getChar(0, 0), "left half must be cleared when right placeholder is overwritten");
        assertEquals('B', buf.getChar(1, 0));
    }

    // Consecutive wide characters must each occupy two columns in sequence.
    @Test
    void multipleWideCharsWrittenSequentially() {
        buf.setCursor(0, 0);
        buf.writeText("日本");
        assertEquals('日', buf.getChar(0, 0));
        assertEquals('\0', buf.getChar(1, 0));
        assertEquals('本', buf.getChar(2, 0));
        assertEquals('\0', buf.getChar(3, 0));
        assertEquals(4, buf.getCursor().getCol());
    }

    // Narrow and wide characters can be mixed freely; the cursor advances by 1 or 2 accordingly.
    @Test
    void mixedNarrowAndWideChars() {
        buf.setCursor(0, 0);
        buf.writeText("A字B");
        assertEquals('A', buf.getChar(0, 0));
        assertEquals('字', buf.getChar(1, 0));
        assertEquals('\0', buf.getChar(2, 0));
        assertEquals('B', buf.getChar(3, 0));
        assertEquals(4, buf.getCursor().getCol());
    }
}
