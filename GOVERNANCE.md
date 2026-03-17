# Governance

## Project status

Terminal Text Buffer is an active academic project. There is no formal roadmap,
no release schedule, and no commitment to any particular feature or timeline.

---

## Contact

| Name | Email | GitHub | Role |
|---|---|---|---|
| Ignacio Garbayo Fernández | ignacio.garbayo@rai.usc.es | @igarbayo | Sole maintainer |

For questions or discussions, open an issue or send an email with subject
`[terminal-text-buffer] your topic`.

---

## Roles

**Maintainer** — currently Ignacio Garbayo Fernández. The maintainer:

- Reviews and merges pull requests
- Manages releases and version tags
- Enforces the Code of Conduct
- Makes final decisions on technical direction
- Signs commits and release tags with a PGP key

There is no steering committee and no formal RFC process.

---

## Decision making

### Routine decisions

The maintainer can act alone on routine matters without prior discussion:

- Bug fixes and correctness improvements
- Documentation updates
- CI/CD configuration changes
- Dependency version bumps (within the same major version)
- Code style and refactoring

### Significant decisions

The following decisions require the maintainer to document the rationale in
[docs/ARCHITECTURE_DECISIONS.md](docs/ARCHITECTURE_DECISIONS.md) before or
alongside the change:

- Adding a new external dependency
- Breaking changes to the public API
- Releasing a new major version
- Changing the license or REUSE compliance strategy
- Changing the branch protection rules or CI requirements

This is a single-maintainer project, so there is no second-approval requirement.
The documentation requirement serves as a public record and forces deliberate
thinking before significant changes.

### Adding new maintainers

New maintainers are added at the current maintainer's discretion, when the
project genuinely needs it. Any new maintainer must agree to the Code of
Conduct and the governance rules in this document.

### Response times

Response times are **best-effort** — expect weeks or months. This is a student
project competing with coursework and exams.

---

## Conflict resolution

Community members are encouraged to raise concerns via issues. If a contributor
disagrees with a decision, they are welcome to discuss it openly in the issue
tracker. The maintainer commits to explaining decisions transparently.

In the case of conduct violations, the process described in
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) takes precedence.

---

## Branch strategy (Git Flow)

| Branch | Purpose | Direct commits allowed |
|---|---|---|
| `master` | Stable releases only | No — PR required |
| `develop` | Integration of finished features | No — PR required |
| `feature/*` | Individual features, branched from `develop` | Yes |
| `hotfix/*` | Urgent fixes on `master` | Yes |

### Rules

- **No direct commits to `master` or `develop`.** All changes go through a
  pull request, even from the maintainer.
- Feature branches are created from `develop`:
  ```bash
  git checkout develop
  git checkout -b feature/my-feature
  ```
- When a feature is ready, open a PR targeting `develop`.
- Releases are merged from `develop` to `master` and tagged with a semver tag.

### Branch protection (configured in GitHub)

- `master`: require PR, no direct push, CI must pass (build + REUSE).
- `develop`: require PR, no direct push, CI must pass.

To configure: GitHub → Settings → Branches → Branch protection rules.

---

## Release process

1. All planned features for the release are merged into `develop`.
2. `develop` is merged into `master` via PR.
3. A tag is created on `master`:
   ```bash
   git tag -s v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
   Tags are signed with the maintainer's PGP key.
4. A GitHub Release is created from the tag with release notes.

Versioning follows [Semantic Versioning](https://semver.org/):
`MAJOR.MINOR.PATCH`.

---

## Commit signing

All commits from the maintainer are signed with a PGP key. Contributors are
encouraged (but not required) to sign their commits. See
[docs/GPG_KEY.md](docs/GPG_KEY.md) for setup instructions.

---

## Amendments

This document may be updated by the maintainer at any time. Significant changes
to governance will be noted in the commit message.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
