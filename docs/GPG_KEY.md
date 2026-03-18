# GPG Commit Signing

This document explains how to set up GPG signing for your Git commits, how to
verify that a commit is signed, and how to configure GitHub to require signed
commits on protected branches.

All commits from this project's maintainer are signed. Contributors are
encouraged (but not required) to sign their commits as well.

---

## Why sign commits?

A GPG signature on a commit proves that the commit was made by the person who
holds the corresponding private key — not just someone who knows the committer's
name and email. Without signatures, anyone who gains access to a repository can
push commits that impersonate another author.

GitHub displays a green **Verified** badge on signed commits.

---

## Step 1: Generate a GPG key

If you do not already have a GPG key, generate one:

```bash
gpg --full-generate-key
```

When prompted:

- **Key type:** `RSA and RSA` (default) or `ED25519` (recommended — shorter and faster)
- **Key size:** `4096` bits (for RSA)
- **Expiration:** Choose an expiration date (e.g. `2y` for 2 years) — an
  expiring key is safer than one that never expires
- **Name and email:** Use the same email you use for your GitHub account

### List your keys

```bash
gpg --list-secret-keys --keyid-format=long
```

Example output:

```
sec   rsa4096/3AA5C34371567BD2 2024-01-01 [SC]
      ABCDEF0123456789ABCDEF0123456789ABCDEF01
uid   [ultimate] Your Name <your@email.com>
ssb   rsa4096/4BB6D45482678CE3 2024-01-01 [E]
```

The key ID is the part after the slash on the `sec` line: `3AA5C34371567BD2`.

---

## Step 2: Tell Git to use your key

```bash
git config --global user.signingkey 3AA5C34371567BD2
git config --global commit.gpgsign true
git config --global tag.gpgsign true
```

From now on, every commit and tag you create will be signed automatically.

> **Windows users:** If you get `error: gpg failed to sign the data`, Git may
> not be finding the right `gpg` binary. Set it explicitly:
>
> ```bash
> git config --global gpg.program "C:/Program Files (x86)/GnuPG/bin/gpg.exe"
> ```
> Adjust the path to match your GnuPG installation.

---

## Step 3: Add your public key to GitHub

Export your public key in ASCII-armored format:

```bash
gpg --armor --export 3AA5C34371567BD2
```

Copy the entire output (including the `-----BEGIN PGP PUBLIC KEY BLOCK-----`
and `-----END PGP PUBLIC KEY BLOCK-----` lines).

Then in GitHub:

1. Go to **Settings → SSH and GPG keys**
2. Click **New GPG key**
3. Paste the exported key
4. Click **Add GPG key**

GitHub will now show a **Verified** badge on your signed commits.

---

## Step 4: Verify a signed commit

To check the signature on any commit:

```bash
git log --show-signature -1
```

Example output for a valid signature:

```
commit abc123def456...
gpg: Signature made Mon 17 Mar 2026 12:00:00 UTC
gpg:                using RSA key 3AA5C34371567BD2
gpg: Good signature from "Your Name <your@email.com>" [ultimate]
```

A `Good signature` message confirms the commit is authentic.

---

## Step 5 (optional): Require signed commits on protected branches

To enforce signing for all contributions to `master` and `develop`:

1. Go to the repository on GitHub
2. **Settings → Branches → Branch protection rules**
3. Edit the rule for `master` (and separately for `develop`)
4. Enable **Require signed commits**
5. Save changes

With this setting, GitHub will reject any push or pull request merge that
includes unsigned commits on those branches.

---

## Troubleshooting

### `error: gpg failed to sign the data`

Make sure the GPG key's email matches the email in your Git config:

```bash
git config user.email        # should match the GPG key's UID
gpg --list-secret-keys --keyid-format=long
```

Also check that `gpg-agent` is running:

```bash
gpgconf --launch gpg-agent
```

### `There is no assurance this key belongs to the named user`

Your key's trust level is not set to `ultimate`. Set it:

```bash
gpg --edit-key 3AA5C34371567BD2
> trust
> 5   (ultimate)
> quit
```

<!-- SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es> -->
<!-- SPDX-License-Identifier: MIT -->
