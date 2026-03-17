# Components and Licensing

This document explains why this project uses the MIT License, what licences
its dependencies carry, and whether all of those licences are compatible with
each other. The goal is to let any reader — developer, legal team, or curious
student — understand the full licensing picture at a glance.

---

## Project license: MIT

This project is released under the **MIT License** (SPDX: `MIT`). The full
text is in [LICENSE](../LICENSE) and [LICENSES/MIT.txt](../LICENSES/MIT.txt).

### Why MIT?

MIT was chosen because:

1. **Maximum freedom for readers and adopters.** This project exists to be
   studied, adapted, and reused — for learning, for building terminal
   emulators, or for any other purpose. MIT places essentially no restrictions
   on any of that.

2. **Academic context.** This is a coding-challenge project, not a commercial
   product. There is no business reason to restrict use.

3. **Compatibility.** MIT is compatible with virtually every other open-source
   licence, including Apache-2.0 (used by the Gradle wrapper) and EPL-2.0
   (used by JUnit 5). A more restrictive licence (GPL, AGPL) would create
   compatibility problems for anyone wanting to link this library into a
   larger project.

4. **No-warranty clause.** Like all OSI-approved licences, MIT explicitly
   disclaims warranties. This is standard practice for open-source software.

### What MIT allows and requires

Anyone who receives this software may use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies — for any purpose, including
commercial — as long as they:

- Include the original copyright notice and the MIT licence text in any
  substantial copy or distribution of the software.

That is the only obligation. There is no copyleft requirement, no
network-use clause, and no patent retaliation clause.

---

## Dependency licence audit

### Runtime dependencies

This library has **no runtime dependencies**. The compiled JAR depends only
on the Java standard library (which is part of the JDK/JRE and is not
redistributed by this project).

### Test dependencies

| Dependency | Version | Licence (SPDX) | Compatibility with MIT |
|---|---|---|---|
| JUnit Jupiter (JUnit 5) | 5.x | `EPL-2.0` | Compatible — EPL-2.0 is a weak copyleft licence. It requires that modifications to EPL-licensed files be shared back under EPL, but it does not "infect" MIT-licensed code that merely calls JUnit as a test framework. JUnit is used only during testing and is never bundled into the distributed artefact. |

### Build tool

| Component | Licence (SPDX) | Notes |
|---|---|---|
| Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) | `Apache-2.0` | Included in the repository as a convenience. Apache-2.0 is a permissive licence fully compatible with MIT. REUSE metadata for these files is in [.reuse/dep5](../.reuse/dep5). |

---

## REUSE compliance

This project follows the [REUSE Specification](https://reuse.software/)
version 3.3. Every file in the repository has a corresponding licence
declaration, either through:

- An **SPDX header comment** in the file itself (all source and documentation
  files authored by this project), or
- The [.reuse/dep5](../.reuse/dep5) file (third-party files that cannot be
  modified, such as the Gradle wrapper).

Licence texts for every SPDX identifier used in this project are stored in
the [LICENSES/](../LICENSES/) directory:

| File | SPDX identifier |
|---|---|
| `LICENSES/MIT.txt` | `MIT` |
| `LICENSES/Apache-2.0.txt` | `Apache-2.0` |

---

## No warranty

As stated in the MIT License:

> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

This is standard for open-source software. Users and adopters are responsible
for evaluating the suitability of this software for their own use case.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
