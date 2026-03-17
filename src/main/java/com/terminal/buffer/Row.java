// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Arrays;

final class Row {

    private final Cell[] cells;

    Row(int width) {
        this.cells = new Cell[width];
        Arrays.fill(cells, Cell.EMPTY);
    }

    Row(Row other) {
        this.cells = Arrays.copyOf(other.cells, other.cells.length);
    }

    Cell getCell(int col) {
        return cells[col];
    }

    void setCell(int col, Cell cell) {
        cells[col] = cell;
    }

    void fill(Cell cell) {
        Arrays.fill(cells, cell);
    }

    void fill(int fromCol, int toCol, Cell cell) {
        Arrays.fill(cells, fromCol, toCol, cell);
    }

    String toContentString() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (Cell cell : cells) {
            if (!cell.isPlaceholder()) {
                sb.append(cell.getCharacter());
            }
        }
        return sb.toString();
    }

    int getWidth() {
        return cells.length;
    }
}
