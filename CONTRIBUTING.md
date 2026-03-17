# Contributing to Terminal Text Buffer

First of all — thank you for taking the time to read this and for your interest
in the project. Whether you are here out of curiosity, to report a bug, or to
propose a change, it genuinely means a lot.

---

## A note before you start

This is an **academic project** maintained by a single student. Please set your
expectations accordingly:

- Response times may be **weeks or months**. This is not negligence — it is
  reality. Coursework, exams, and life happen.
- There is **no roadmap** and no commitment to implement any particular feature.
- The project may go quiet for extended periods.

None of this means your contribution is not valued. It means you should not
block on a response from me.

---

## How to report a bug

1. Check that the bug has not already been reported in the
   [issue tracker](https://github.com/igarbayo/terminal-text-buffer/issues).
2. Open a new issue using the **Bug Report** template.
3. Include:
   - What you did (exact commands or code).
   - What you expected to happen.
   - What actually happened (paste the output or stack trace).
   - Your environment: OS, Java version (`java -version`), shell.

**Example of a useful bug report:**

> **Steps to reproduce:**
> ```java
> TerminalBuffer buf = new TerminalBuffer(10, 5, 100);
> buf.setCursor(9, 0);
> buf.writeText("AB");   // A fits, B should wrap
> ```
> **Expected:** cursor at (0, 1), 'B' written on row 1.
> **Got:** cursor at (9, 0), 'B' lost.

---

## How to propose a feature or improvement

Open an issue using the **Feature Request** template. Describe:

- The problem you are trying to solve (not just the solution).
- Your proposed approach, if you have one.
- Any alternatives you considered.

---

## How to contribute code

### 1. Fork and set up

```bash
git clone https://github.com/igarbayo/terminal-text-buffer.git
cd terminal-text-buffer
./gradlew test   # make sure everything passes before you start
```

### 2. Create a feature branch from `develop`

```bash
git checkout develop
git checkout -b feature/your-feature-name
```

Never branch from `master`. Never commit directly to `master` or `develop`.

### 3. Write your code

- Follow the existing code style (no formatter config yet, just be consistent).
- Write tests first if possible — this project was built with TDD.
- Add SPDX headers to any new file you create:

```java
// SPDX-FileCopyrightText: <YEAR> <Your Name> <your@email.com>
// SPDX-License-Identifier: MIT
```

### 4. Commit with Conventional Commits

All commit messages must follow the
[Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>: <short description in present tense, lowercase>
```

Valid types:

| Type | Use it for |
|---|---|
| `feat` | A new feature or behaviour |
| `fix` | A bug fix |
| `test` | Adding or fixing tests |
| `docs` | Documentation only |
| `refactor` | Code change with no feature or fix |
| `chore` | Tooling, dependencies, build |

**Examples:**

```
feat: add SGR escape sequence parser
fix: correct cursor clamping when width is 1
test: add scrollback eviction edge case
docs: clarify wide character limitations in README
chore: bump JUnit to 5.11.0
```

Breaking changes: add `!` after the type and a `BREAKING CHANGE:` footer.

```
feat!: change scrollback row indexing to 0-based

BREAKING CHANGE: getLine() now uses 0 for the most recent scrollback row
instead of -1.
```

### 5. Open a Pull Request to `develop`

- Fill in the PR template completely.
- The PR title must itself be a valid Conventional Commit message.
- CI must be green (build + tests + REUSE compliance).

---

## PGP signed commits

The maintainer signs all commits with a PGP key. You are not required to sign
your commits, but it is encouraged. If you do, please include your public key
fingerprint in the PR description.

---

## Code of Conduct

This project follows the
[Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).
By participating you agree to abide by its terms.

---

## Contact

If you have a question that does not fit an issue, you can reach the maintainer
at **ignacio.garbayo@rai.usc.es**. Please use the subject line
`[terminal-text-buffer] your topic` so it does not get lost.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
