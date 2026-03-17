// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Arrays;

final class Row {

    // Row represents a single line in the terminal buffer, consisting of an array of Cell objects. It provides methods to access and modify cells, fill ranges with a specific cell, and convert the row content to a string while skipping placeholder cells.
    private final Cell[] cells;

    // Constructor to create a new Row with a specified width, initializing all cells to the shared EMPTY instance to optimize memory usage for blank spaces.
    Row(int width) {
        this.cells = new Cell[width];
        Arrays.fill(cells, Cell.EMPTY);
    }

    // Copy constructor to create a new Row by copying the cells from another Row, ensuring that the new Row has its own array of cells while sharing the same Cell instances where possible.
    Row(Row other) {
        this.cells = Arrays.copyOf(other.cells, other.cells.length);
    }

    // Getters
    Cell getCell(int col) {
        return cells[col];
    }

    int getWidth() {
        return cells.length;
    }

    // Setters
    void setCell(int col, Cell cell) {
        cells[col] = cell;
    }

    // Methods to fill the entire row or a specific range of columns with a given Cell instance, which can be used for operations like clearing a line or applying a uniform style.
    void fill(Cell cell) {
        Arrays.fill(cells, cell);
    }

    void fill(int fromCol, int toCol, Cell cell) {
        Arrays.fill(cells, fromCol, toCol, cell);
    }

    // Converts the content of the row to a string by concatenating the characters of the cells, while skipping any placeholder cells (those that are part of a wide character) to ensure that the resulting string accurately represents the visible content of the row.
    String toContentString() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (Cell cell : cells) {
            if (!cell.isPlaceholder()) {
                sb.append(cell.getCharacter());
            }
        }
        return sb.toString();
    }
}
