// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Objects;

public final class Cell {

    public enum CellType {
        NORMAL,      // regular single-column character
        WIDE_LEFT,   // left half of a 2-column glyph
        WIDE_RIGHT   // synthetic placeholder occupying the right column
    }

    // A shared immutable empty cell instance to avoid unnecessary allocations for blank spaces
    public static final Cell EMPTY = new Cell(' ', CellAttributes.DEFAULT, CellType.NORMAL);

    private final int codePoint;
    private final CellAttributes attributes;
    private final CellType type;

    // Constructors
    public Cell(int codePoint, CellAttributes attributes) {
        this(codePoint, attributes, CellType.NORMAL);
    }

    public Cell(int codePoint, CellAttributes attributes, CellType type) {
        this.codePoint = codePoint;
        this.attributes = attributes;
        this.type = type;
    }

    // Getters
    public int getCodePoint() {
        return codePoint;
    }

    public CellAttributes getAttributes() {
        return attributes;
    }

    public CellType getType() {
        return type;
    }

    // there are no setters needed, as Cell is a final class with private final fields, making it immutable

    // Convenience methods for checking cell type
    public boolean isWide() {
        return type == CellType.WIDE_LEFT;
    }

    public boolean isPlaceholder() {
        return type == CellType.WIDE_RIGHT;
    }

    // we always redefine equals and hashcode together, even if we don't use them in hash-based collections, to ensure consistent behavior when comparing cells
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return codePoint == cell.codePoint &&
                type == cell.type &&
                Objects.equals(attributes, cell.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codePoint, attributes, type);
    }

    // For debugging purposes, we provide a string representation of the cell that includes its character, attributes, and type
    @Override
    public String toString() {
        return "Cell{'" + new String(Character.toChars(codePoint)) + "', " + attributes + ", " + type + "}";
    }
}
