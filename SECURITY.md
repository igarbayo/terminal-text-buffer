# Security Policy

## Academic project notice

This is an academic project with no production deployment and no commercial use.
Under the EU Cyber Resilience Act (CRA), non-commercial free and open-source
software is exempt from the mandatory requirements that apply to commercial
manufacturers. This project voluntarily adopts CRA-aligned security practices
anyway — not because it is required, but because it is the right thing to do.

---

## Supported versions

Only the latest version on the `master` branch is supported. There are no
long-term support versions.

| Version | Supported |
|---|---|
| Latest (`master`) | Yes |
| Older commits | No |

---

## Known limitations

These are known, accepted limitations — not bugs to report:

- **Wide character rendering in Windows PowerShell / CMD:** PowerShell and CMD
  replace CJK ideographs and many emoji with `?` before the characters reach
  the JVM. The library handles wide characters correctly (verified by the
  automated test suite), but interactive testing from those terminals is not
  possible. Use IntelliJ's built-in terminal or Windows Terminal with
  `chcp 65001`.

- **Supplementary-plane emoji (U+1F300 and above):** `Cell` stores characters
  as `char` (a UTF-16 code unit). Code points above U+FFFF are detected
  correctly but stored with loss of the high surrogate. This is a known
  trade-off documented in the README.

- **No input validation at network or process boundaries:** This is an
  in-process data structure library. It does not handle untrusted external
  input and does not perform sanitization. Callers are responsible for
  validating input before passing it to the buffer.

- **Scrollback access is O(n):** Random access by index iterates the internal
  `ArrayDeque`. This is a performance characteristic, not a security issue.

---

## Scope

### In scope

- Memory safety issues in buffer operations (e.g. `resize`, `insertText`)
- Incorrect data isolation between screen and scrollback (e.g. mutation of
  scrollback rows after they are pushed off-screen)
- Logic errors that could cause data loss or silent corruption of buffer state

### Out of scope

- Wide character rendering in Windows PowerShell / CMD (see Known Limitations)
- Performance issues that do not affect correctness or data safety
- Theoretical vulnerabilities with no realistic attack vector given the
  library's use case (in-process data structure, no network, no I/O)

---

## Reporting a vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Send an email to **ignacio.garbayo@rai.usc.es** with:

- Subject: `[SECURITY] terminal-text-buffer — <brief description>`
- A description of the vulnerability and its potential impact
- Steps to reproduce or a minimal reproducing example
- Any suggested fix, if you have one

### Example report

> **Subject:** `[SECURITY] terminal-text-buffer — resize allows negative width`
>
> Calling `buf.resize(-1, 24)` throws an unchecked `NegativeArraySizeException`
> instead of the documented `IllegalArgumentException`, causing the buffer to
> enter an inconsistent state if the exception is caught upstream.
>
> **Reproducer:**
> ```java
> TerminalBuffer buf = new TerminalBuffer(80, 24, 100);
> try { buf.resize(-1, 24); } catch (Exception e) { /* ignored */ }
> // buf is now broken
> ```

---

## Response timeline

| Stage | Target time |
|---|---|
| Acknowledgement | 72 hours |
| Initial assessment | 14 days |
| Fix or mitigation | Best-effort (academic project, no SLA) |

These timelines are targets, not guarantees. This is a student project — exams
and coursework take priority. If you do not hear back within 72 hours, a
follow-up email is welcome.

---

## Disclosure policy

This project follows **coordinated disclosure**:

1. You report privately.
2. The maintainer confirms and works on a fix.
3. A fixed version is released.
4. The vulnerability is disclosed publicly, with credit to the reporter if desired.

Public disclosure should not happen before **90 days** from the initial report,
unless both parties agree otherwise.

---

## No bug bounty

This is a student academic project. There is no bug bounty programme.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
