// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Test
    void emptyConstantHasSpaceAndDefaultAttributes() {
        assertEquals(' ', Cell.EMPTY.getCharacter());
        assertEquals(CellAttributes.DEFAULT, Cell.EMPTY.getAttributes());
        assertEquals(Cell.CellType.NORMAL, Cell.EMPTY.getType());
    }

    @Test
    void twoArgConstructorDefaultsToNormalType() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        assertEquals('A', cell.getCharacter());
        assertEquals(Cell.CellType.NORMAL, cell.getType());
        assertFalse(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    @Test
    void wideCellIsWideAndNotPlaceholder() {
        Cell cell = new Cell('字', CellAttributes.DEFAULT, Cell.CellType.WIDE_LEFT);
        assertTrue(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    @Test
    void placeholderCellIsPlaceholderAndNotWide() {
        Cell cell = new Cell('\0', CellAttributes.DEFAULT, Cell.CellType.WIDE_RIGHT);
        assertFalse(cell.isWide());
        assertTrue(cell.isPlaceholder());
    }

    @Test
    void normalCellIsNeitherWideNorPlaceholder() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.NORMAL);
        assertFalse(cell.isWide());
        assertFalse(cell.isPlaceholder());
    }

    @Test
    void equalCellsAreEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('A', CellAttributes.DEFAULT);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void cellsWithDifferentCharsAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('B', CellAttributes.DEFAULT);
        assertNotEquals(a, b);
    }

    @Test
    void cellsWithDifferentTypesAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.NORMAL);
        Cell b = new Cell('A', CellAttributes.DEFAULT, Cell.CellType.WIDE_LEFT);
        assertNotEquals(a, b);
    }

    @Test
    void cellsWithDifferentAttributesAreNotEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('A', CellAttributes.DEFAULT.withForeground(TerminalColor.RED));
        assertNotEquals(a, b);
    }

    @Test
    void cellStoresAttributes() {
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(TerminalColor.RED).withStyle(TextStyle.BOLD);
        Cell cell = new Cell('X', attrs);
        assertEquals(attrs, cell.getAttributes());
    }
}
