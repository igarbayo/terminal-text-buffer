// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import java.util.Arrays;

public class TerminalBuffer {

    private int width;
    private int height;
    private final int maxScrollback;

    private Row[] screen;
    private final Row[] scrollback;   // fixed-size circular ring
    private int sbHead;               // index of oldest entry
    private int sbCount;              // number of stored rows

    private int cursorCol;
    private int cursorRow;
    // xterm-style deferred wrap once writing reaches the last column.
    private boolean pendingWrap;

    private CellAttributes currentAttributes;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("height must be > 0");
        if (maxScrollback < 0) throw new IllegalArgumentException("maxScrollback must be >= 0");

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;

        // Allocate the screen grid; every cell starts as Cell.EMPTY (space, default attributes).
        this.screen = new Row[height];
        for (int i = 0; i < height; i++) {
            screen[i] = new Row(width);
        }
        // Scrollback is pre-allocated once; entries are filled lazily as rows scroll off.
        this.scrollback = new Row[maxScrollback];
        this.sbHead = 0;
        this.sbCount = 0;
        this.cursorCol = 0;
        this.cursorRow = 0;
        this.pendingWrap = false;
        this.currentAttributes = CellAttributes.DEFAULT;
    }

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMaxScrollback() { return maxScrollback; }
    public int getScrollbackSize() { return sbCount; }

    // -------------------------------------------------------------------------
    // Current Attributes
    // -------------------------------------------------------------------------

    public void setAttributes(CellAttributes attributes) {
        // Replaces the full active attribute state used by future writes.
        this.currentAttributes = attributes;
    }

    public CellAttributes getAttributes() {
        return currentAttributes;
    }

    public void setForeground(TerminalColor color) {
        // Produces a new CellAttributes with only the foreground changed.
        currentAttributes = currentAttributes.withForeground(color);
    }

    public void setBackground(TerminalColor color) {
        // Produces a new CellAttributes with only the background changed.
        currentAttributes = currentAttributes.withBackground(color);
    }

    public void addStyle(TextStyle style) {
        // Adds one style flag; other flags are preserved.
        currentAttributes = currentAttributes.withStyle(style);
    }

    public void removeStyle(TextStyle style) {
        // Removes one style flag; other flags are preserved.
        currentAttributes = currentAttributes.withoutStyle(style);
    }

    public void resetAttributes() {
        // Restores fg=DEFAULT, bg=DEFAULT, and clears all style flags.
        currentAttributes = CellAttributes.DEFAULT;
    }

    // -------------------------------------------------------------------------
    // Cursor
    // -------------------------------------------------------------------------

    public CursorPosition getCursor() {
        // Returns a snapshot — callers cannot mutate internal cursor state.
        return new CursorPosition(cursorCol, cursorRow);
    }

    public void setCursor(int col, int row) {
        // Keep cursor always inside visible screen bounds.
        cursorCol = clampCol(col);
        cursorRow = clampRow(row);
        // Any explicit cursor move cancels a pending wrap.
        pendingWrap = false;
    }

    public void moveCursorRight(int n) {
        // Relative movement is clamped instead of throwing on overflow.
        cursorCol = clampCol(cursorCol + n);
        pendingWrap = false;
    }

    public void moveCursorLeft(int n) {
        cursorCol = clampCol(cursorCol - n);
        pendingWrap = false;
    }

    public void moveCursorDown(int n) {
        cursorRow = clampRow(cursorRow + n);
        pendingWrap = false;
    }

    public void moveCursorUp(int n) {
        cursorRow = clampRow(cursorRow - n);
        pendingWrap = false;
    }

    // -------------------------------------------------------------------------
    // Editing — cursor/attribute aware
    // -------------------------------------------------------------------------

    /**
     * Overwrites cells at cursor position with the text characters.
     * Implements xterm "pending wrap": if the cursor is at the last column after
     * writing, the next writeText call wraps to the beginning of the next line.
     * Wide characters (isWideChar) consume 2 columns; truncated if they don't fit.
     */
    public void writeText(String text) {
        if (text == null || text.isEmpty()) return;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            // Resolve pending wrap before writing the next character.
            if (pendingWrap) {
                pendingWrap = false;
                advanceCursorToNextLine();
            }

            if (isWideCodePoint(cp)) {
                // A wide glyph needs 2 columns; skip it if only 1 column remains.
                if (cursorCol >= width - 1) {
                    cursorCol = width - 1;
                    pendingWrap = true;
                    break;
                }
                // Clear any wide-pair halves that overlap the two target columns.
                clearWideOverlap(cursorCol);
                clearWideOverlap(cursorCol + 1);
                screen[cursorRow].setCell(cursorCol,
                        new Cell(cp, currentAttributes, Cell.CellType.WIDE_LEFT));
                screen[cursorRow].setCell(cursorCol + 1,
                        new Cell(0, currentAttributes, Cell.CellType.WIDE_RIGHT));
                cursorCol += 2;
            } else {
                // Narrow character: clear any wide pair at this column, then write.
                clearWideOverlap(cursorCol);
                screen[cursorRow].setCell(cursorCol,
                        new Cell(cp, currentAttributes));
                cursorCol++;
            }

            // If the cursor has moved past the last column, arm the pending-wrap flag.
            if (cursorCol >= width) {
                cursorCol = width - 1;
                pendingWrap = true;
            }
        }
    }

    /**
     * Inserts text at cursor position, shifting existing content to the right.
     * Content that overflows wraps to the next line. Content that reaches the
     * bottom screen row causes scrolling (top row → scrollback).
     */
    public void insertText(String text) {
        if (text == null || text.isEmpty()) return;

        // Honour any pending wrap the same way writeText does.
        if (pendingWrap) {
            pendingWrap = false;
            advanceCursorToNextLine();
        }

        // Build the full cell array for the new text, then delegate to the recursive helper.
        Cell[] toInsert = buildCells(text);
        insertCellsAt(cursorCol, cursorRow, toInsert);

        // Advance cursor past the inserted content, wrapping across rows if necessary.
        int advance = toInsert.length;
        int newCol = cursorCol + advance;
        if (newCol >= width) {
            int extraRows = newCol / width;
            newCol = newCol % width;
            cursorRow = Math.min(cursorRow + extraRows, height - 1);
        }
        cursorCol = Math.min(newCol, width - 1);
        pendingWrap = false;
    }

    /**
     * Fills the cursor's current row entirely with the given character using
     * current attributes. Cursor column is unchanged.
     */
    public void fillLine(char ch) {
        // A single Cell is constructed and shared; Row.fill copies it into every slot.
        Cell fill = new Cell(ch, currentAttributes);
        screen[cursorRow].fill(fill);
    }

    // -------------------------------------------------------------------------
    // Editing — cursor/attribute independent
    // -------------------------------------------------------------------------

    /**
     * Pushes the top screen row into scrollback, shifts all rows up, and
     * appends a new empty row at the bottom.
     */
    public void insertEmptyLine() {
        pushTopRowToScrollback();
        // Shift rows [1..height-1] one slot up in O(height) via arraycopy.
        System.arraycopy(screen, 1, screen, 0, height - 1);
        screen[height - 1] = new Row(width);
    }

    /** Clears all screen cells to Cell.EMPTY. Scrollback is untouched. */
    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen[i] = new Row(width);
        }
        // Reset pending wrap so the next write starts cleanly.
        pendingWrap = false;
    }

    /** Clears screen and all scrollback history. */
    public void clearAll() {
        clearScreen();
        // Reset both ring-buffer pointers to discard every stored row.
        sbHead = 0;
        sbCount = 0;
    }

    // -------------------------------------------------------------------------
    // Content Access
    // -------------------------------------------------------------------------

    /**
     * Returns the character at the given position.
     * Screen: row in [0, height-1]; Scrollback: row in [-scrollbackSize, -1].
     */
    public char getChar(int col, int row) {
        // Cast is safe for BMP characters; use getCell().getCodePoint() for full Unicode.
        return (char) resolveRow(row).getCell(col).getCodePoint();
    }

    /**
     * Returns the Cell at the given position (exposes CellType for wide-char inspection).
     */
    public Cell getCell(int col, int row) {
        return resolveRow(row).getCell(col);
    }

    /**
     * Returns the attributes at the given position.
     */
    public CellAttributes getAttributesAt(int col, int row) {
        return resolveRow(row).getCell(col).getAttributes();
    }

    /**
     * Returns the content of the given line with trailing spaces trimmed.
     * Screen: row in [0, height-1]; Scrollback: row in [-scrollbackSize, -1].
     */
    public String getLine(int row) {
        String raw = resolveRow(row).toContentString();
        // Strip trailing spaces so callers get a clean logical string.
        int end = raw.length();
        while (end > 0 && raw.charAt(end - 1) == ' ') end--;
        return raw.substring(0, end);
    }

    /**
     * Returns the full fixed-width content of the given line (always width chars).
     */
    public String getRawLine(int row) {
        // No trimming: every cell is included, padded with spaces where empty.
        return resolveRow(row).toContentString();
    }

    /**
     * Returns all screen lines joined by '\n'.
     */
    public String getScreenContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(screen[i].toContentString());
        }
        return sb.toString();
    }

    /**
     * Returns scrollback (oldest first) followed by screen lines, joined by '\n'.
     */
    public String getAllContent() {
        StringBuilder sb = new StringBuilder();
        // Iterate the circular buffer in chronological order (oldest → newest).
        for (int i = 0; i < sbCount; i++) {
            Row row = scrollback[(sbHead + i) % maxScrollback];
            sb.append(row.toContentString()).append('\n');
        }
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(screen[i].toContentString());
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Bonus: Resize
    // -------------------------------------------------------------------------

    /**
     * Resizes the screen to new dimensions.
     * - Rows narrowed: content truncated at right edge.
     * - Rows widened: padded with empty cells.
     * - Screen shortened: rows removed from top enter scrollback.
     * - Screen taller: new empty rows added at bottom.
     * - Cursor is clamped to new bounds.
     */
    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0) throw new IllegalArgumentException("width must be > 0");
        if (newHeight <= 0) throw new IllegalArgumentException("height must be > 0");

        // Adjust width of all existing screen and scrollback rows first.
        if (newWidth != width) {
            for (int i = 0; i < height; i++) {
                screen[i] = resizeRow(screen[i], newWidth);
            }
            // Also resize scrollback rows so getChar works consistently after resize.
            for (int i = 0; i < sbCount; i++) {
                int idx = (sbHead + i) % maxScrollback;
                scrollback[idx] = resizeRow(scrollback[idx], newWidth);
            }
        }

        // Adjust height after width so row objects already have the correct width.
        if (newHeight < height) {
            // Rows removed from the top enter scrollback so history is not lost.
            int rowsToRemove = height - newHeight;
            for (int i = 0; i < rowsToRemove; i++) {
                pushRowToScrollback(screen[i]);
            }
            Row[] newScreen = new Row[newHeight];
            System.arraycopy(screen, rowsToRemove, newScreen, 0, newHeight);
            screen = newScreen;
        } else if (newHeight > height) {
            // Grow: append blank rows at the bottom.
            Row[] newScreen = new Row[newHeight];
            System.arraycopy(screen, 0, newScreen, 0, height);
            for (int i = height; i < newHeight; i++) {
                newScreen[i] = new Row(newWidth != width ? newWidth : width);
            }
            screen = newScreen;
        }

        width = newWidth;
        height = newHeight;

        // Cursor may become out-of-bounds after resize, so clamp it again.
        cursorCol = clampCol(cursorCol);
        cursorRow = clampRow(cursorRow);
        pendingWrap = false;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private int clampCol(int col) {
        return Math.max(0, Math.min(col, width - 1));
    }

    private int clampRow(int row) {
        return Math.max(0, Math.min(row, height - 1));
    }

    private Row resolveRow(int row) {
        // Non-negative rows address visible screen, negative rows address scrollback.
        if (row >= 0 && row < height) {
            return screen[row];
        }
        if (row < 0 && row >= -sbCount) {
            // row -1 → newest (offset sbCount-1 from head), row -sbCount → oldest (head)
            int index = sbCount + row;
            return scrollback[(sbHead + index) % maxScrollback];
        }
        throw new IllegalArgumentException("Row out of bounds: " + row +
                " (screen height=" + height + ", scrollbackSize=" + sbCount + ")");
    }

    private void pushTopRowToScrollback() {
        // Convenience wrapper that always pushes the first visible screen row.
        pushRowToScrollback(screen[0]);
    }

    private void pushRowToScrollback(Row row) {
        if (maxScrollback == 0) return;
        // Store a snapshot so later screen mutations do not corrupt history.
        int tail = (sbHead + sbCount) % maxScrollback;
        scrollback[tail] = new Row(row);
        if (sbCount < maxScrollback) {
            sbCount++;
        } else {
            // Ring is full: advance head to evict the oldest entry.
            sbHead = (sbHead + 1) % maxScrollback;
        }
    }

    private void advanceCursorToNextLine() {
        // At bottom edge, advance implies scroll-up by inserting an empty line.
        if (cursorRow < height - 1) {
            cursorRow++;
            cursorCol = 0;
        } else {
            // Already on the last row: scroll the screen and keep the cursor there.
            insertEmptyLine();
            cursorCol = 0;
            // cursorRow stays at height-1
        }
    }

    /** If col holds a WIDE_LEFT, clear its right partner. If WIDE_RIGHT, clear the left owner. */
    private void clearWideOverlap(int col) {
        if (col < 0 || col >= width) return;
        Cell existing = screen[cursorRow].getCell(col);
        // Overwriting the left half of a wide pair: erase the right placeholder.
        if (existing.isWide() && col + 1 < width) {
            screen[cursorRow].setCell(col + 1, Cell.EMPTY);
        // Overwriting the right placeholder: erase the left half that owns the glyph.
        } else if (existing.isPlaceholder() && col - 1 >= 0) {
            screen[cursorRow].setCell(col - 1, Cell.EMPTY);
        }
    }

    /** Build an array of Cells from a string, honouring wide characters. */
    private Cell[] buildCells(String text) {
        // First pass: count total cells needed (wide chars cost 2 slots).
        int cellCount = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            cellCount += isWideCodePoint(cp) ? 2 : 1;
        }

        // Second pass: populate the cell array.
        Cell[] cells = new Cell[cellCount];
        int idx = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (isWideCodePoint(cp)) {
                // Wide glyph: left cell holds the codepoint, right cell is a zero placeholder.
                cells[idx++] = new Cell(cp, currentAttributes, Cell.CellType.WIDE_LEFT);
                cells[idx++] = new Cell(0, currentAttributes, Cell.CellType.WIDE_RIGHT);
            } else {
                cells[idx++] = new Cell(cp, currentAttributes);
            }
        }
        return cells;
    }

    /**
     * Inserts cells at (col, row), shifting existing content right. Overflow wraps
     * to the next line recursively; overflow at the last row triggers insertEmptyLine.
     *
     * Strategy: merge new cells with existing tail into a combined array. Write the
     * first (width-col) elements back into the row. Any remainder with actual content
     * is pushed to the beginning of the next row.
     */
    private void insertCellsAt(int col, int row, Cell[] cells) {
        if (cells.length == 0) return;

        Row currentRow = screen[row];
        int available = width - col;

        // Capture the tail of the current row that will be displaced by the insertion.
        Cell[] existing = new Cell[available];
        for (int i = 0; i < available; i++) {
            existing[i] = currentRow.getCell(col + i);
        }

        // Merged array: new cells first, then the displaced tail.
        Cell[] combined = new Cell[cells.length + available];
        System.arraycopy(cells, 0, combined, 0, cells.length);
        System.arraycopy(existing, 0, combined, cells.length, available);

        // Write the first `available` cells of combined back into the current row.
        for (int i = 0; i < available; i++) {
            currentRow.setCell(col + i, combined[i]);
        }

        // If the combined array is longer than the row tail, we have overflow to push down.
        if (combined.length > available) {
            Cell[] overflow = Arrays.copyOfRange(combined, available, combined.length);
            if (hasActualContent(overflow)) {
                // Recursively continue insertion on the next row.
                int nextRow = row + 1;
                if (nextRow >= height) {
                    // We've hit the bottom: scroll up to make room.
                    insertEmptyLine();
                    nextRow = height - 1;
                }
                insertCellsAt(0, nextRow, overflow);
            }
        }
    }

    /** Returns true if any cell contains non-space content or non-default attributes. */
    private static boolean hasActualContent(Cell[] cells) {
        // Suppresses propagation of overflow that is purely blank space.
        for (Cell c : cells) {
            if (c.getCodePoint() != ' ' || !c.getAttributes().equals(CellAttributes.DEFAULT)) {
                return true;
            }
        }
        return false;
    }

    private Row resizeRow(Row row, int newWidth) {
        Row newRow = new Row(newWidth);
        // Copy as many cells as fit in the new width; extras are left as Cell.EMPTY.
        int copyLen = Math.min(row.getWidth(), newWidth);
        for (int i = 0; i < copyLen; i++) {
            newRow.setCell(i, row.getCell(i));
        }
        return newRow;
    }

    private static boolean isWideCodePoint(int cp) {
        // East Asian Wide / Fullwidth characters — ranges alone are sufficient,
        // no need to filter by Unicode category type.
        return (cp >= 0x1100 && cp <= 0x115F)   // Hangul Jamo
                || (cp >= 0x2E80 && cp <= 0x303E)  // CJK Radicals
                || (cp >= 0x3041 && cp <= 0x33FF)  // Japanese (hiragana, katakana, CJK symbols)
                || (cp >= 0x3400 && cp <= 0x4DBF)  // CJK Extension A
                || (cp >= 0x4E00 && cp <= 0x9FFF)  // CJK Unified Ideographs
                || (cp >= 0xA000 && cp <= 0xA4CF)  // Yi
                || (cp >= 0xAC00 && cp <= 0xD7AF)  // Hangul Syllables
                || (cp >= 0xF900 && cp <= 0xFAFF)  // CJK Compatibility Ideographs
                || (cp >= 0xFE10 && cp <= 0xFE1F)  // Vertical Forms
                || (cp >= 0xFE30 && cp <= 0xFE4F)  // CJK Compatibility Forms
                || (cp >= 0xFF00 && cp <= 0xFF60)  // Fullwidth Forms
                || (cp >= 0xFFE0 && cp <= 0xFFE6)  // Fullwidth Signs
                || (cp >= 0x1F300 && cp <= 0x1F9FF) // Emoji
                || (cp >= 0x20000 && cp <= 0x2A6DF); // CJK Extension B
    }
}
