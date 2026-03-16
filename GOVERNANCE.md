# Governance

## Project status

Terminal Text Buffer is an active academic project. There is no formal roadmap,
no release schedule, and no commitment to any particular feature or timeline.

---

## Maintainer

| Name | Email | Role |
|---|---|---|
| Ignacio Garbayo Fernández | ignacio.garbayo@rai.usc.es | Sole maintainer |

This is a single-maintainer project. All decisions about direction, merges,
and releases are made by the maintainer. There is no steering committee,
no vote, and no formal RFC process.

For questions, open an issue or send an email with subject
`[terminal-text-buffer] your topic`.

---

## Decision making

The maintainer has final say on all technical and community decisions. Proposals
are welcome via issues or pull requests and will be considered seriously, but
there is no obligation to implement them or to respond within any given time.

Response times are **best-effort** — expect weeks or months. This is a
student project competing with coursework and exams.

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
encouraged (but not required) to sign their commits.

---

## Amendments

This document may be updated by the maintainer at any time. Significant changes
will be noted in the commit message and changelog.

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
