# Manual Testing Guide — Terminal Text Buffer REPL

This guide covers how to run the interactive REPL and test every feature of
`TerminalBuffer` by hand, including edge cases and boundary conditions.

## Starting the REPL

```bash
./gradlew run
```

The REPL starts with an 80×24 buffer and 200-line scrollback. Type `help` at any
point to list all available commands.

> **Note on wide characters (CJK/emoji):** PowerShell and CMD replace CJK characters
> with `?` before they reach Java, so wide character input is only verifiable through
> the automated tests (`./gradlew test`) or via IntelliJ's built-in terminal /
> Windows Terminal with `chcp 65001`.

---

## 1. Basic `writeText`

```
write Hello world
```
Expected: text appears at row 0 col 0; cursor moves to (11, 0).

```
cursor 5 2
write Centered text
```
Expected: text starts at col 5 row 2.

### Edge case — right-edge wrap (pending wrap flag)

```
clear
cursor 75 0
write 123456789012345
```
Expected: `12345` fits in cols 75–79; the remaining `678901234` wraps to row 1 col 0.
Cursor stays at the position after the last written character.

---

## 2. Colors and styles

```
clear
fg RED
bold
write Red bold text
cursor 0 1
fg BLUE
italic
write Blue italic text
reset
cursor 0 2
write Normal text
```
Expected: row 0 in red+bold, row 1 in blue+italic, row 2 in default style.

Available colors: `DEFAULT RED GREEN YELLOW BLUE MAGENTA CYAN WHITE BLACK`
and their `BRIGHT_*` variants.

Available styles: `bold`, `italic`, `underline`. Deactivate with `nostyle BOLD` etc.

---

## 3. Cursor movement

```
cursor 0 0
move right 5
move down 3
info
```
Expected: cursor at (5, 3).

### Edge case — cursor clamped at screen bounds

```
cursor 0 0
move up 10
info
```
Expected: cursor stays at (0, 0) — cannot move above row 0.

```
cursor 79 23
move right 5
move down 5
info
```
Expected: cursor stays at (79, 23) — cannot move beyond last col/row.

---

## 4. `fillLine`

```
clear
cursor 0 1
fill -
cursor 0 3
fill =
```
Expected: row 1 filled with `-`, row 3 filled with `=`.

```
cursor 0 5
fill
```
Expected: row 5 filled with spaces (clears it).

---

## 5. `insertEmptyLine` and scrollback

```
clear
cursor 0 0
write Line 1
newline
write Line 2
newline
write Line 3
scrollback
```
Expected: after each `newline`, the top row scrolls into scrollback.
`scrollback` shows `[-2] Line 1` and `[-1] Line 2` (Line 3 is still on screen).

### Fill scrollback

```
clearall
cursor 0 0
write AAA
newline
write BBB
newline
write CCC
newline
scrollback
```
Expected:
```
[-3] AAA
[-2] BBB
[-1] CCC
```

---

## 6. `insertText` — shift right

```
clear
cursor 0 0
write ABCDE
cursor 2 0
insert XXX
```
Expected: row 0 becomes `ABXXXCDE` — existing content shifts right.

### Edge case — overflow to next line

```
clear
cursor 0 0
write 12345678901234567890123456789012345678901234567890123456789012345678901234567890
cursor 0 0
insert OVERFLOW
```
Expected: `OVERFLOW` is inserted at col 0, shifting the 80 existing chars right.
The last 8 characters fall to row 1 col 0.

---

## 7. Wide characters

> Requires IntelliJ terminal or Windows Terminal with UTF-8 (see note above).

```
clear
cursor 0 0
write 日本語
info
```
Expected: cursor at (6, 0) — each CJK character occupies 2 cells.

### Edge case — wide char at right edge

```
clear
cursor 79 0
write 日
```
Expected: no room for both halves; column 79 is left empty, cursor stays at 79.

### Verify code points (debug command)

```
codepoints 日本語
```
Expected: `U+65E5`, `U+672C`, `U+8A9E` — all detected as `isWide=true`.

---

## 8. Resize

```
clear
write Text that may get truncated here
resize 20 5
print
```
Expected: screen is 20×5. The text is **truncated at col 20** in the row that contained
it. Rows removed from the top (shrinking from 24 to 5 rows) enter scrollback.
Run `scrollback` to confirm the text is preserved there.

```
resize 80 24
```
Restores original dimensions. Previous scrollback entries are still accessible.

---

## 9. `clearScreen` vs `clearAll`

```
clearall
cursor 0 0
write First line
newline
write Second line
newline
scrollback
```
Expected: `[-2] First line`, `[-1] Second line`.

```
clear
scrollback
```
Expected: screen is blank, but scrollback **still shows 2 entries** — `clear` does not
touch scrollback.

```
clearall
scrollback
```
Expected: `--- Scrollback empty ---` — `clearall` wipes everything including scrollback.

---

## 10. Full demo sequence

Combines all features: colors, cursor movement, fill, insert, scrollback, and resize.

```
clearall
fg GREEN
bold
write === Terminal Buffer Demo ===
reset
cursor 0 2
write Normal line
cursor 0 3
fg YELLOW
write Yellow line
reset
cursor 0 4
fill -
newline
newline
newline
scrollback
cursor 0 0
insert INSERTED AT THE START
resize 40 10
print
resize 80 24
```

---

## Automated tests

All features and edge cases are also covered by the JUnit 5 test suite:

```bash
./gradlew test
```

HTML report: `build/reports/tests/test/index.html`

| Test class | What it covers |
|---|---|
| `CellAttributesTest` | Immutability, fluent builders, equality |
| `CellTest` | Wide cell types, EMPTY sentinel |
| `RowTest` | get/set, fill, copy constructor |
| `CursorTest` | setCursor clamping, move in all directions |
| `AttributeStateTest` | setForeground/Background, addStyle/removeStyle, reset |
| `ClearScreenTest` | clearScreen preserves scrollback; clearAll wipes both |
| `WriteTextTest` | Overwrite, pending wrap, attribute storage |
| `FillLineTest` | Fill entire row, cursor row selection |
| `InsertEmptyLineTest` | Row push to scrollback, deque ordering |
| `ScrollbackTest` | Max scrollback eviction, negative row indexing |
| `InsertTextTest` | Shift right, overflow to next line |
| `ContentAccessTest` | getLine trimming, getRawLine fixed width, getAllContent |
| `WideCharTest` | 2-cell rendering, right-edge truncation |
| `ResizeTest` | Truncate/pad rows, height shrink → scrollback |

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
