package com.terminal.buffer;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumSet;

public class TerminalBuffer {

    private int width;
    private int height;
    private final int maxScrollback;

    private Row[] screen;
    private final ArrayDeque<Row> scrollback;

    private int cursorCol;
    private int cursorRow;
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

        this.screen = new Row[height];
        for (int i = 0; i < height; i++) {
            screen[i] = new Row(width);
        }
        this.scrollback = new ArrayDeque<>();
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
    public int getScrollbackSize() { return scrollback.size(); }

    // -------------------------------------------------------------------------
    // Current Attributes
    // -------------------------------------------------------------------------

    public void setAttributes(CellAttributes attributes) {
        this.currentAttributes = attributes;
    }

    public CellAttributes getAttributes() {
        return currentAttributes;
    }

    public void setForeground(TerminalColor color) {
        currentAttributes = currentAttributes.withForeground(color);
    }

    public void setBackground(TerminalColor color) {
        currentAttributes = currentAttributes.withBackground(color);
    }

    public void addStyle(TextStyle style) {
        currentAttributes = currentAttributes.withStyle(style);
    }

    public void removeStyle(TextStyle style) {
        currentAttributes = currentAttributes.withoutStyle(style);
    }

    public void resetAttributes() {
        currentAttributes = CellAttributes.DEFAULT;
    }

    // -------------------------------------------------------------------------
    // Cursor
    // -------------------------------------------------------------------------

    public CursorPosition getCursor() {
        return new CursorPosition(cursorCol, cursorRow);
    }

    public void setCursor(int col, int row) {
        cursorCol = clampCol(col);
        cursorRow = clampRow(row);
        pendingWrap = false;
    }

    public void moveCursorRight(int n) {
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

            // Resolve pending wrap before writing
            if (pendingWrap) {
                pendingWrap = false;
                advanceCursorToNextLine();
            }

            if (isWideCodePoint(cp)) {
                // Need 2 columns; truncate if only 1 column left
                if (cursorCol >= width - 1) {
                    // No room for both halves — skip character, stay at edge
                    cursorCol = width - 1;
                    pendingWrap = true;
                    break;
                }
                clearWideOverlap(cursorCol);
                clearWideOverlap(cursorCol + 1);
                char ch = (char) cp; // safe for BMP
                screen[cursorRow].setCell(cursorCol,
                        new Cell(ch, currentAttributes, Cell.CellType.WIDE_LEFT));
                screen[cursorRow].setCell(cursorCol + 1,
                        new Cell('\0', currentAttributes, Cell.CellType.WIDE_RIGHT));
                cursorCol += 2;
            } else {
                clearWideOverlap(cursorCol);
                screen[cursorRow].setCell(cursorCol,
                        new Cell((char) cp, currentAttributes));
                cursorCol++;
            }

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

        if (pendingWrap) {
            pendingWrap = false;
            advanceCursorToNextLine();
        }

        // Collect all cells to insert
        Cell[] toInsert = buildCells(text);
        insertCellsAt(cursorCol, cursorRow, toInsert);

        // Advance cursor past inserted content
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
        System.arraycopy(screen, 1, screen, 0, height - 1);
        screen[height - 1] = new Row(width);
    }

    /** Clears all screen cells to Cell.EMPTY. Scrollback is untouched. */
    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen[i] = new Row(width);
        }
        pendingWrap = false;
    }

    /** Clears screen and all scrollback history. */
    public void clearAll() {
        clearScreen();
        scrollback.clear();
    }

    // -------------------------------------------------------------------------
    // Content Access
    // -------------------------------------------------------------------------

    /**
     * Returns the character at the given position.
     * Screen: row in [0, height-1]; Scrollback: row in [-scrollbackSize, -1].
     */
    public char getChar(int col, int row) {
        return resolveRow(row).getCell(col).getCharacter();
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
        int end = raw.length();
        while (end > 0 && raw.charAt(end - 1) == ' ') end--;
        return raw.substring(0, end);
    }

    /**
     * Returns the full fixed-width content of the given line (always width chars).
     */
    public String getRawLine(int row) {
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
        for (Row row : scrollback) {
            sb.append(row.toContentString());
            sb.append('\n');
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

        // Adjust width of all existing rows
        if (newWidth != width) {
            for (int i = 0; i < height; i++) {
                screen[i] = resizeRow(screen[i], newWidth);
            }
            Row[] newScrollback = new Row[scrollback.size()];
            int idx = 0;
            for (Row r : scrollback) {
                newScrollback[idx++] = resizeRow(r, newWidth);
            }
            scrollback.clear();
            for (Row r : newScrollback) scrollback.addLast(r);
        }

        // Adjust height
        if (newHeight < height) {
            // Rows removed from the top enter scrollback
            int rowsToRemove = height - newHeight;
            for (int i = 0; i < rowsToRemove; i++) {
                pushRowToScrollback(screen[i]);
            }
            Row[] newScreen = new Row[newHeight];
            System.arraycopy(screen, rowsToRemove, newScreen, 0, newHeight);
            screen = newScreen;
        } else if (newHeight > height) {
            Row[] newScreen = new Row[newHeight];
            System.arraycopy(screen, 0, newScreen, 0, height);
            for (int i = height; i < newHeight; i++) {
                newScreen[i] = new Row(newWidth != width ? newWidth : width);
            }
            screen = newScreen;
        }

        width = newWidth;
        height = newHeight;

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
        if (row >= 0 && row < height) {
            return screen[row];
        }
        int sbSize = scrollback.size();
        if (row < 0 && row >= -sbSize) {
            // row -1 → index sbSize-1 (most recent), row -sbSize → index 0 (oldest)
            int index = sbSize + row;
            int i = 0;
            for (Row r : scrollback) {
                if (i == index) return r;
                i++;
            }
        }
        throw new IllegalArgumentException("Row out of bounds: " + row +
                " (screen height=" + height + ", scrollbackSize=" + sbSize + ")");
    }

    private void pushTopRowToScrollback() {
        pushRowToScrollback(screen[0]);
    }

    private void pushRowToScrollback(Row row) {
        if (maxScrollback == 0) return;
        scrollback.addLast(new Row(row));
        if (scrollback.size() > maxScrollback) {
            scrollback.removeFirst();
        }
    }

    private void advanceCursorToNextLine() {
        if (cursorRow < height - 1) {
            cursorRow++;
            cursorCol = 0;
        } else {
            insertEmptyLine();
            cursorCol = 0;
            // cursorRow stays at height-1
        }
    }

    /** If col holds a WIDE_LEFT, clear its right partner. If WIDE_RIGHT, clear the left owner. */
    private void clearWideOverlap(int col) {
        if (col < 0 || col >= width) return;
        Cell existing = screen[cursorRow].getCell(col);
        if (existing.isWide() && col + 1 < width) {
            screen[cursorRow].setCell(col + 1, Cell.EMPTY);
        } else if (existing.isPlaceholder() && col - 1 >= 0) {
            screen[cursorRow].setCell(col - 1, Cell.EMPTY);
        }
    }

    /** Build an array of Cells from a string, honouring wide characters. */
    private Cell[] buildCells(String text) {
        // Count total cells needed
        int cellCount = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            cellCount += isWideCodePoint(cp) ? 2 : 1;
        }

        Cell[] cells = new Cell[cellCount];
        int idx = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (isWideCodePoint(cp)) {
                cells[idx++] = new Cell((char) cp, currentAttributes, Cell.CellType.WIDE_LEFT);
                cells[idx++] = new Cell('\0', currentAttributes, Cell.CellType.WIDE_RIGHT);
            } else {
                cells[idx++] = new Cell((char) cp, currentAttributes);
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

        // Save existing content from col to end of row
        Cell[] existing = new Cell[available];
        for (int i = 0; i < available; i++) {
            existing[i] = currentRow.getCell(col + i);
        }

        // Combined: new cells first, then displaced existing content
        Cell[] combined = new Cell[cells.length + available];
        System.arraycopy(cells, 0, combined, 0, cells.length);
        System.arraycopy(existing, 0, combined, cells.length, available);

        // Write first `available` slots of combined back into the row
        for (int i = 0; i < available; i++) {
            currentRow.setCell(col + i, combined[i]);
        }

        // Overflow: everything past the first `available` elements
        if (combined.length > available) {
            Cell[] overflow = Arrays.copyOfRange(combined, available, combined.length);
            if (hasActualContent(overflow)) {
                int nextRow = row + 1;
                if (nextRow >= height) {
                    insertEmptyLine();
                    nextRow = height - 1;
                }
                insertCellsAt(0, nextRow, overflow);
            }
        }
    }

    /** Returns true if any cell contains non-space content or non-default attributes. */
    private static boolean hasActualContent(Cell[] cells) {
        for (Cell c : cells) {
            if (c.getCharacter() != ' ' || !c.getAttributes().equals(CellAttributes.DEFAULT)) {
                return true;
            }
        }
        return false;
    }

    private Row resizeRow(Row row, int newWidth) {
        Row newRow = new Row(newWidth);
        int copyLen = Math.min(row.getWidth(), newWidth);
        for (int i = 0; i < copyLen; i++) {
            newRow.setCell(i, row.getCell(i));
        }
        return newRow;
    }

    private static boolean isWideCodePoint(int cp) {
        int type = Character.getType(cp);
        // East Asian Wide / Fullwidth characters
        return type == Character.OTHER_LETTER &&
                ((cp >= 0x1100 && cp <= 0x115F)   // Hangul Jamo
                || (cp >= 0x2E80 && cp <= 0x303E)  // CJK Radicals
                || (cp >= 0x3041 && cp <= 0x33FF)  // Japanese
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
                || (cp >= 0x20000 && cp <= 0x2A6DF) // CJK Extension B
                );
    }
}
