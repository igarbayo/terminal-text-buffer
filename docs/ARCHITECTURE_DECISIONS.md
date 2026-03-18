# Architecture Decisions

This document records the significant decisions made during the development of
Terminal Text Buffer — the trade-offs considered, the options rejected, and the
reasons behind the choices that were made. It is a living document: new entries
are added as decisions are taken.

The goal is not to justify every line of code, but to give future contributors
(and future-me) enough context to understand *why* things are the way they are.

---

## ADR-001: Language and build tool

**Decision:** Java with Gradle.

**Context:** The project was specified as a Jetbrains coding challenge with Java
as the required language. Gradle was chosen over Maven because it is the default
build tool in the IntelliJ/Jetbrains ecosystem and produces a more concise build
file for a project of this scope.

**Consequences:** The Gradle wrapper (`gradlew`) is committed to the repository
so that anyone can build without installing Gradle separately. The wrapper is
licensed under Apache-2.0 (see [COMPONENTS_LICENSE.md](COMPONENTS_LICENSE.md)).

---

## ADR-002: No external runtime dependencies

**Decision:** The compiled library has zero runtime dependencies beyond the
Java standard library.

**Context:** This is a data structure library. Pulling in external libraries
for a self-contained data structure would add complexity and licensing
considerations with no benefit.

**Consequences:** The library can be dropped into any Java project without
dependency conflicts.

---

## ADR-003: Test framework — JUnit 5

**Decision:** JUnit Jupiter (JUnit 5) for all tests.

**Context:** JUnit 5 is the current standard for Java testing. It is available
as a Gradle test dependency and does not affect the runtime artefact.

**Consequences:** EPL-2.0 license (JUnit's license) applies to the test code at
build time. It does not affect the MIT-licensed library artefact.

---

## ADR-004: Cell immutability, Row mutability

**Decision:** `Cell` and `CellAttributes` are immutable value objects. `Row` is
mutable.

**Context:** Two competing concerns:
- **Safety:** Scrollback rows must not change after they are pushed off-screen.
- **Performance:** Writing one character should not create a new row array.

**Chosen trade-off:** `Row` is mutable (O(1) writes), but when a row is pushed
to scrollback a snapshot is taken via a copy constructor (`new Row(original)`).
`Cell.EMPTY` is shared as a singleton — safe because `Cell` is immutable.

**Rejected alternative:** Making `Row` immutable would require copying the
entire row array on every character write — O(width) per character.

---

## ADR-005: Scrollback storage — circular buffer

**Decision:** A fixed-size `Row[]` circular buffer with two int fields:
`sbHead` (index of the oldest live entry) and `sbCount` (number of live
entries). The newest entry lives at index `(sbHead + sbCount - 1) % maxScrollback`;
the `n`-th entry relative to the current scrollback tail is resolved as
`scrollback[(sbHead + sbCount + n) % maxScrollback]` (where `n` is negative).

**Context:** Scrollback needs O(1) push (when a row scrolls off the screen),
O(1) eviction (when the scrollback is full), and O(1) random access for
`resolveRow()`.

**Trade-off:** All three operations are O(1). The implementation is slightly
more complex than a `ArrayDeque`, but the indices are straightforward once the
modular arithmetic is understood.

**Supersedes:** An earlier version used `ArrayDeque<Row>` which gave O(1) push
and eviction but O(n) random access.

---

## ADR-006: Coordinate system — signed integer for row

**Decision:** Screen rows use indices `[0, height-1]`. Scrollback rows use
negative indices: `-1` is the most recently scrolled-off line, `-scrollbackSize`
is the oldest.

**Context:** All content-access methods (`getChar`, `getLine`, `getAttributes`)
need to address both screen and scrollback. Options:
1. Separate method signatures for screen and scrollback.
2. A wrapper type (enum tag + index).
3. A single signed integer (negative = scrollback).

**Chosen trade-off:** Option 3. It makes the unified `resolveRow(int row)` method
the single gateway, keeps method signatures simple, and is intuitive once
documented.

**Rejected alternative:** Separate methods would double the API surface. A
wrapper type would require callers to construct it on every access.

---

## ADR-007: Wide character support — placeholder cell strategy

**Decision:** A wide glyph at column `c` occupies two cells: `cells[c]` holds
the character with type `WIDE_LEFT`, and `cells[c+1]` stores `'\0'` with type
`WIDE_RIGHT`. `WIDE_RIGHT` cells are skipped in string rendering.

**Context:** This is the same strategy used by xterm, VTE, and most real
terminal emulators. The alternatives were:
1. Store wide chars in a separate side-channel structure.
2. Use `char[]` pairs (surrogate-style) within a single cell.

**Chosen trade-off:** The placeholder strategy integrates naturally with the
existing fixed-width `Row` model. Writing over either half of a wide pair
automatically clears both halves.

**Note on supplementary-plane codepoints:** `Cell.character` is stored as
`int` (a Unicode codepoint, not a UTF-16 `char`), so emoji and other
supplementary-plane characters above U+FFFF are stored without loss.
`Row.toContentString()` uses `StringBuilder.appendCodePoint(int)` to render
them correctly. The public `getChar(col, row)` method still returns `char`
for convenience — callers that need the full codepoint can use
`Cell.getCodePoint()` directly.

---

## ADR-008: writeText — pending wrap flag

**Decision:** When the last character is written to column `width-1`, the cursor
stays there and an internal `pendingWrap` flag is set. The next `writeText` call
resolves the wrap first.

**Context:** This matches xterm's behaviour. The alternative (advancing the
cursor immediately) would place the cursor out of bounds at column `width`.

**Consequence:** The cursor stays in-bounds at all times. The wrap is deferred
until the next write, which allows the end-of-line position to be read without
immediately triggering a scroll.

---

## ADR-009: MIT license

**Decision:** MIT.

**Rationale:** See [COMPONENTS_LICENSE.md](COMPONENTS_LICENSE.md) for the full
justification.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
