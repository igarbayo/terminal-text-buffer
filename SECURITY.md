# Security Policy

## Academic project notice

This is an academic project with no production deployment and no commercial use.
It is not subject to the mandatory requirements of the EU Cyber Resilience Act
(CRA). However, this project voluntarily adopts CRA-aligned security practices
as a matter of good hygiene.

---

## Supported versions

Only the latest version on the `master` branch is supported. There are no
long-term support versions.

| Version | Supported |
|---|---|
| Latest (`master`) | Yes |
| Older commits | No |

---

## Scope

### In scope

- Memory safety issues in buffer operations (e.g. `resize`, `insertText`)
- Incorrect data isolation between screen and scrollback (e.g. mutation of
  scrollback rows after push)
- Logic errors that could cause data loss or silent corruption of buffer state

### Out of scope

- Wide character rendering in Windows PowerShell / CMD (known OS-level
  limitation, not a library bug — see README)
- Performance issues that do not affect correctness
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
| Acknowledgement | 7 days |
| Initial assessment | 30 days |
| Fix or mitigation | Best-effort (academic project, no SLA) |

---

## Disclosure policy

This project follows **coordinated disclosure**:

1. You report privately.
2. The maintainer confirms and works on a fix.
3. A fixed version is released.
4. The vulnerability is disclosed publicly, with credit to the reporter if desired.

Public disclosure should not happen before 90 days from the initial report,
unless both parties agree otherwise.

---

## No bug bounty

This is a student academic project. There is no bug bounty programme.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
