// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    // Cell.EMPTY must represent a space with default attributes and NORMAL type.
    @Test
    void emptyConstantHasSpaceAndDefaultAttributes() {
        assertEquals(' ', Cell.EMPTY.getCodePoint());
        assertEquals(CellAttributes.DEFAULT, Cell.EMPTY.getAttributes());
        assertEquals(Cell.CellType.NORMAL, Cell.EMPTY.getType());
    }

    // The two-argument constructor must default to NORMAL type.
    @Test
    void twoArgConstructorDefaultsToNormalType() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        assertEquals('A', cell.getCodePoint());
        assertEquals(Cell.CellType.NORMAL, cell.getType());
        assertFalse(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    // A WIDE_LEFT cell must report isWide=true and isPlaceholder=false.
    @Test
    void wideCellIsWideAndNotPlaceholder() {
        Cell cell = new Cell('字', CellAttributes.DEFAULT, Cell.CellType.WIDE_LEFT);
        assertTrue(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    // A WIDE_RIGHT (placeholder) cell must report isPlaceholder=true and isWide=false.
    @Test
    void placeholderCellIsPlaceholderAndNotWide() {
        Cell cell = new Cell('\0', CellAttributes.DEFAULT, Cell.CellType.WIDE_RIGHT);
        assertFalse(cell.isWide());
        assertTrue(cell.isPlaceholder());
    }

    // A NORMAL cell is neither wide nor a placeholder.
    @Test
    void normalCellIsNeitherWideNorPlaceholder() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.NORMAL);
        assertFalse(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    // Two cells with the same character, attributes and type must be equal (equals and hashCode).
    @Test
    void equalCellsAreEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('A', CellAttributes.DEFAULT);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // Cells with different characters must not be equal.
    @Test
    void cellsWithDifferentCharsAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('B', CellAttributes.DEFAULT);
        assertNotEquals(a, b);
    }

    // Cells with the same character but different types must not be equal.
    @Test
    void cellsWithDifferentTypesAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.NORMAL);
        Cell b = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.WIDE_LEFT);
        assertNotEquals(a, b);
    }

    // Cells with the same character but different attributes must not be equal.
    @Test
    void cellsWithDifferentAttributesAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('A', CellAttributes.DEFAULT.withForeground(TerminalColor.RED));
        assertNotEquals(a, b);
    }

    // A supplementary-plane codepoint (U+1F600, above U+FFFF) must survive a round-trip
    // through Cell without truncation — this was impossible when the field was a char.
    @Test
    void supplementaryPlaneCodePointRoundTrips() {
        int emoji = 0x1F600; // 😀
        Cell cell = new Cell(emoji, CellAttributes.DEFAULT);
        assertEquals(emoji, cell.getCodePoint());
    }

    // The cell must return exactly the attributes it was constructed with.
    @Test
    void cellStoresAttributes() {
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(TerminalColor.RED).withStyle(TextStyle.BOLD);
        Cell cell = new Cell('X', attrs);
        assertEquals(attrs, cell.getAttributes());
    }
}
