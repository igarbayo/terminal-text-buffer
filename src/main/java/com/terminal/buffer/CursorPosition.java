package com.terminal.buffer;

import java.util.Objects;

public final class CursorPosition {

    private final int col;
    private final int row;

    public CursorPosition(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

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

    @Override
    public String toString() {
        return "CursorPosition{col=" + col + ", row=" + row + "}";
    }
}
