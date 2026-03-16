// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Objects;

public final class Cell {

    public enum CellType {
        NORMAL,
        WIDE_LEFT,   // left half of a 2-column glyph
        WIDE_RIGHT   // synthetic placeholder occupying the right column
    }

    public static final Cell EMPTY = new Cell(' ', CellAttributes.DEFAULT, CellType.NORMAL);

    private final char character;
    private final CellAttributes attributes;
    private final CellType type;

    public Cell(char character, CellAttributes attributes) {
        this(character, attributes, CellType.NORMAL);
    }

    public Cell(char character, CellAttributes attributes, CellType type) {
        this.character = character;
        this.attributes = attributes;
        this.type = type;
    }

    public char getCharacter() {
        return character;
    }

    public CellAttributes getAttributes() {
        return attributes;
    }

    public CellType getType() {
        return type;
    }

    public boolean isWide() {
        return type == CellType.WIDE_LEFT;
    }

    public boolean isPlaceholder() {
        return type == CellType.WIDE_RIGHT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return character == cell.character &&
                type == cell.type &&
                Objects.equals(attributes, cell.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, attributes, type);
    }

    @Override
    public String toString() {
        return "Cell{'" + character + "', " + attributes + ", " + type + "}";
    }
}
