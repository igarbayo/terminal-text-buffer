package com.terminal.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowTest {

    @Test
    void newRowIsFilledWithEmptyCells() {
        Row row = new Row(5);
        for (int i = 0; i < 5; i++) {
            assertEquals(Cell.EMPTY, row.getCell(i));
        }
    }

    @Test
    void setCellAndGetCellRoundTrip() {
        Row row = new Row(5);
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        row.setCell(2, cell);
        assertEquals(cell, row.getCell(2));
        assertEquals(Cell.EMPTY, row.getCell(1));
        assertEquals(Cell.EMPTY, row.getCell(3));
    }

    @Test
    void fillWithCellFillsAllColumns() {
        Row row = new Row(4);
        Cell cell = new Cell('X', CellAttributes.DEFAULT);
        row.fill(cell);
        for (int i = 0; i < 4; i++) {
            assertEquals(cell, row.getCell(i));
        }
    }

    @Test
    void fillRangeOnlyFillsSpecifiedColumns() {
        Row row = new Row(6);
        Cell cell = new Cell('Z', CellAttributes.DEFAULT);
        row.fill(2, 4, cell);  // cols 2, 3 inclusive
        assertEquals(Cell.EMPTY, row.getCell(0));
        assertEquals(Cell.EMPTY, row.getCell(1));
        assertEquals(cell, row.getCell(2));
        assertEquals(cell, row.getCell(3));
        assertEquals(Cell.EMPTY, row.getCell(4));
        assertEquals(Cell.EMPTY, row.getCell(5));
    }

    @Test
    void copyConstructorProducesIndependentCopy() {
        Row original = new Row(4);
        original.setCell(1, new Cell('A', CellAttributes.DEFAULT));
        Row copy = new Row(original);

        copy.setCell(1, new Cell('B', CellAttributes.DEFAULT));

        assertEquals('A', original.getCell(1).getCharacter(), "mutation of copy must not affect original");
        assertEquals('B', copy.getCell(1).getCharacter());
    }

    @Test
    void toContentStringReturnsAllChars() {
        Row row = new Row(5);
        row.setCell(0, new Cell('H', CellAttributes.DEFAULT));
        row.setCell(1, new Cell('e', CellAttributes.DEFAULT));
        row.setCell(2, new Cell('l', CellAttributes.DEFAULT));
        row.setCell(3, new Cell('l', CellAttributes.DEFAULT));
        row.setCell(4, new Cell('o', CellAttributes.DEFAULT));
        assertEquals("Hello", row.toContentString());
    }

    @Test
    void toContentStringSkipsWidePlaceholderCells() {
        Row row = new Row(4);
        row.setCell(0, new Cell('字', CellAttributes.DEFAULT, Cell.CellType.WIDE_LEFT));
        row.setCell(1, new Cell('\0', CellAttributes.DEFAULT, Cell.CellType.WIDE_RIGHT));
        row.setCell(2, new Cell('A', CellAttributes.DEFAULT));
        row.setCell(3, Cell.EMPTY);
        // '字' occupies cols 0-1; col 1 is placeholder (skipped); col 2 is 'A'; col 3 is space
        assertEquals("字A ", row.toContentString());
    }

    @Test
    void getWidthReturnsConstructedWidth() {
        assertEquals(80, new Row(80).getWidth());
        assertEquals(1, new Row(1).getWidth());
    }
}
