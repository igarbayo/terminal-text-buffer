# Contributing to Terminal Text Buffer

Thank you so much for taking the time to read this. It genuinely means a lot.
Terminal Text Buffer is a small project born from a Jetbrains coding challenge,
and the fact that you are here considering contributing to it is something we do
not take for granted.

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

### 1. Set up your development environment

**Prerequisites:**

- **JDK 11 or later.** Make sure `java -version` and `javac -version` both
  report the same JDK (not a JRE).
- **Git** (any recent version).
- No other tools required — the Gradle wrapper (`gradlew`) is bundled.

**Steps:**

```bash
# 1. Fork the repository on GitHub, then clone your fork
git clone https://github.com/<your-username>/terminal-text-buffer.git
cd terminal-text-buffer

# 2. Verify the build passes before making any changes
./gradlew test

# The HTML test report is at build/reports/tests/test/index.html
```

> **Windows note:** if Gradle complains about `tools.jar`, set
> `org.gradle.java.home` in `gradle.properties` to your JDK directory
> (not a JRE). See [README — Troubleshooting](README.md#troubleshooting).

### 2. Create a feature branch from `develop`

```bash
git checkout develop
git checkout -b feature/your-descriptive-name
```

Never branch from `master`. Never commit directly to `master` or `develop`.

### 3. Write your code

#### Code style

This project uses the **Allman brace style** with **4-space indentation**
(no tabs).

```java
// Allman style — opening brace on its own line
if (condition)
{
    doSomething();
}
else
{
    doSomethingElse();
}
```

- Keep lines under 120 characters where possible.
- Prefer `final` for local variables and parameters that are not reassigned.
- Write tests first (this project was built with TDD — see the development
  process in the README).
- Add SPDX license headers to any new source file you create:

```java
// SPDX-FileCopyrightText: <YEAR> <Your Name> <your@email.com>
// SPDX-License-Identifier: MIT
```

For non-source files (Markdown, YAML, etc.) use an HTML or `#`-style comment
as appropriate, or create a `.license` sidecar file.

### 4. Commit with Conventional Commits

All commit messages must follow the
[Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<optional scope>): <short description in present tense, lowercase>

<optional body — explain why, not what>

<optional footer — issue references, BREAKING CHANGE>
```

Valid types:

| Type | Use it for |
|---|---|
| `feat` | A new feature or behaviour |
| `fix` | A bug fix |
| `refactor` | Code change with no feature or fix |
| `perf` | Performance improvement (special case of `refactor`) |
| `style` | Whitespace, formatting — no behaviour change |
| `test` | Adding or fixing tests |
| `docs` | Documentation only |
| `build` | Build tools, dependencies, project version |
| `ops` | CI/CD, infrastructure, deployment scripts |
| `chore` | Initial commit, `.gitignore`, other housekeeping |

**Examples:**

```
feat: add SGR escape sequence parser
fix: correct cursor clamping when width is 1
test: add scrollback eviction edge case
docs: clarify wide character limitations in README
perf: replace ArrayDeque with circular buffer for O(1) scrollback access
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
- At least one maintainer approval is required before merging.

---

## Definition of done

A contribution is considered complete when all of the following are true:

- [ ] `./gradlew test` passes locally with no failures or errors
- [ ] New or changed source files have SPDX license headers
- [ ] New public behaviour is covered by at least one test
- [ ] Documentation is updated if the change affects behaviour or the API
- [ ] The PR title and all commit messages follow the Conventional Commits format

---

## PGP signed commits

The maintainer signs all commits with a PGP key. You are not required to sign
your commits, but it is encouraged. If you do, please include your public key
fingerprint in the PR description. See [docs/GPG_KEY.md](docs/GPG_KEY.md) for
setup instructions.

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
