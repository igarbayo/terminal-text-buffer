# Terminal Text Buffer

A terminal text buffer implementation in Java — the core data structure used by terminal emulators to store and render text output.

## Building and Running Tests

```bash
./gradlew test
```

Requires Java 8+. No external libraries except JUnit 5 for testing.

---

## Architecture

### Class Overview

```
TerminalColor (enum)       — 16 standard colors + DEFAULT (17 values)
TextStyle (enum)           — BOLD, ITALIC, UNDERLINE
CellAttributes (immutable) — fg color + bg color + EnumSet<TextStyle>
Cell (immutable)           — char + CellAttributes + CellType (NORMAL/WIDE_LEFT/WIDE_RIGHT)
Row (package-private)      — mutable fixed-width array of Cells
CursorPosition (immutable) — (col, row) value object
TerminalBuffer (public)    — the main API class
```

### Key Design Decisions

#### Immutability strategy: Cell immutable, Row mutable

`Cell` and `CellAttributes` are immutable value objects. This makes it safe to share `Cell.EMPTY` everywhere without defensive copying, and ensures that scrollback history cannot be accidentally modified after a row is pushed there.

`Row` is mutable (package-private) — writing a character is O(1) instead of creating a new row array on every keystroke. When a row is pushed into scrollback, a copy-constructor snapshot is taken (`new Row(original)`), so future mutations to the screen row don't corrupt history.

#### Color and style representation

`TerminalColor` is a 17-value enum (DEFAULT + 16 standard ANSI colors). `TextStyle` flags use `EnumSet<TextStyle>`, which is backed by a long bitmask internally — compact, type-safe, and requires no custom bitfield arithmetic.

#### Scrollback storage

`ArrayDeque<Row>` with front=oldest, back=newest. Gives O(1) push (addLast) and O(1) eviction (removeFirst). When the deque exceeds `maxScrollback`, the oldest entry is evicted.

#### Coordinate system

- **Screen**: row `[0, height-1]`, where row 0 is the top (first visible line).
- **Scrollback**: row `[-scrollbackSize, -1]`, where row `-1` is the most recently scrolled-off line (visually just above the screen) and row `-scrollbackSize` is the oldest.

This signed-integer convention unifies all content-access methods under a single `int row` parameter without needing a separate type or overloaded flag.

#### Wide character support (bonus)

Uses the **placeholder cell strategy** (the same approach as xterm, VTE, and most real terminal emulators):

- A wide glyph (CJK ideograph, emoji) at column `c` sets `cells[c] = WIDE_LEFT` and `cells[c+1] = WIDE_RIGHT`.
- `WIDE_RIGHT` stores `'\0'` as its character and is skipped in `toContentString()`.
- Writing over a `WIDE_LEFT` automatically clears its `WIDE_RIGHT` partner (and vice versa) to prevent half-wide artifacts.
- A wide char that doesn't fit at the right edge is truncated (the column is left empty).

Wide character detection uses Unicode block ranges for CJK ideographs, Hangul, fullwidth forms, and common emoji (BMP range). Supplementary-plane emoji (U+1F300+) are detected via the `int codepoint` API.

#### `writeText` — pending wrap flag

`writeText` implements xterm's **"pending wrap"** behavior rather than immediate truncation:

- When the last character is written to column `width-1`, the cursor stays at `width-1` and an internal `pendingWrap = true` flag is set.
- The _next_ call to `writeText` resolves the pending wrap first: it advances the cursor to column 0 of the next row (scrolling if at the last row), then writes the new character there.
- Any explicit `setCursor` call clears `pendingWrap`.

This matches real terminal behavior and allows consecutive writes to flow naturally across lines.

#### `insertText` — push-down wrapping

`insertText` inserts characters at the cursor position, shifting existing row content right. Overflow flows to subsequent rows recursively:

1. Capture existing content from cursor column to end of row.
2. Merge: `combined = [new cells] + [existing]`.
3. Write the first `(width - cursorCol)` elements of `combined` back into the row.
4. If `combined` has more elements and they contain actual content (non-space, non-default), insert them at column 0 of the next row using the same algorithm.
5. If this overflow reaches the last screen row, `insertEmptyLine()` is called — the top row scrolls into scrollback, and the new bottom row receives the overflow.

Trailing spaces from displaced empty cells are detected by `hasActualContent()` and discarded to avoid propagating blank overflow infinitely.

#### `getLine` vs `getRawLine`

- `getLine(row)` — returns the line content with trailing spaces trimmed. Convenient for tests and display.
- `getRawLine(row)` — returns the fixed-width string (always `width` characters). Useful for width-sensitive rendering.

#### Resize (bonus)

`resize(newWidth, newHeight)` handles four cases:

- **Wider**: rows padded with `Cell.EMPTY` at the right.
- **Narrower**: rows truncated at the new width.
- **Taller**: new empty rows appended at the bottom.
- **Shorter**: rows removed from the top of the screen enter scrollback (same as scrolling).

The cursor is clamped to the new bounds after resize.

---

## Trade-offs and Known Limitations

- **`char` vs `int` for wide chars**: The implementation uses `char` (UTF-16 code unit) for cell storage, which covers BMP characters including all CJK ideographs and most common emoji. Supplementary-plane emoji above U+FFFF are detected via `codePointAt` but stored as `(char) codePoint`, which loses the high surrogate. Changing `Cell.character` to `int` (Unicode codepoint) would fix this without changing the public API of `TerminalBuffer`, and is the recommended future improvement.

- **`insertText` cursor position after overflow**: The cursor advancement after `insertText` uses arithmetic based on the total character count relative to the cursor start position. If `insertEmptyLine()` is triggered during overflow (causing rows to shift up), the cursor position is calculated relative to the final state. This is consistent in the current implementation but could be refined if complex multi-line insert scenarios are needed.

- **`insertText` hasActualContent check**: Trailing spaces from empty rows are suppressed to avoid infinite blank-cell propagation. However, if a user explicitly inserts spaces into a full row and wants those spaces to flow to the next line, they would be dropped. This is a pragmatic trade-off — meaningful overflow content is always preserved.

- **Scrollback access is O(n)**: `resolveRow()` for scrollback iterates the `ArrayDeque` to find the row at a given index. For production use, replacing `ArrayDeque` with a fixed-size circular buffer (`Row[]` + head/tail indices) would give O(1) random access. For the scope of this task, the simplicity of `ArrayDeque` is preferred.

---

## Potential Improvements

- Replace `ArrayDeque<Row>` with a `Row[]` circular buffer for O(1) scrollback access.
- Change `Cell.character` from `char` to `int` (Unicode codepoint) for full supplementary-plane emoji support.
- Add `Bidi` (bidirectional text) support for RTL languages.
- Add `SGR` (Select Graphic Rendition) parsing to directly process ANSI escape sequences into attribute changes.
- Add alternate screen buffer support (used by fullscreen TUI apps like vim).

---

## Development Process

This implementation was built using **TDD (Test-Driven Development)** in a bottom-up order:

1. Value objects (`CellAttributes`, `Cell`) — tested in isolation first
2. `Row` — the storage primitive
3. `TerminalBuffer` skeleton with cursor and dimensions
4. Attribute state management
5. `clearScreen` / `clearAll` + basic content access
6. `writeText` with pending-wrap flag
7. `fillLine`
8. `insertEmptyLine` + scrollback
9. `insertText` with push-down wrapping
10. Content access edge cases (`getLine`, `getAllContent`)
11. Wide character support
12. Resize
