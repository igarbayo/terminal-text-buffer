// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Objects;

public final class CursorPosition {

    // CursorPosition is a simple immutable data class representing the column and row of the cursor in the terminal buffer. It provides basic getters, equality, and string representation methods.
    private final int col;
    private final int row;

    // Constructor
    public CursorPosition(int col, int row) {
        this.col = col;
        this.row = row;
    }

    // Getters
    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    // no setters are needed, as CursorPosition is a final class with private final fields, making it immutable

    // we always redefine equals and hashcode together, even if we don't use them in hash-based collections, to ensure consistent behavior when comparing cursor positions
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CursorPosition)) return false;
        CursorPosition that = (CursorPosition) o;
        return col == that.col && row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }

    // For debugging purposes, we provide a string representation of the cursor position that includes its column and row
    @Override
    public String toString() {
        return "CursorPosition{col=" + col + ", row=" + row + "}";
    }
}
