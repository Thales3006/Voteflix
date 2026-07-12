---
name: run-voteflix
description: Run, start, launch, screenshot, drive, test the Voteflix JavaFX desktop app (client + server). Use when asked to run the app, verify UI changes, take a screenshot, or interact with any of the pages (movies, reviews, users).
---

# run-voteflix

Voteflix is a JavaFX desktop client-server application. The server is a plain Java process; the client renders a JavaFX GUI. Both are built and run via **Maven inside `nix develop`**, which injects the correct JDK 23 and GTK/glib library paths.

The interaction harness is `driver.sh` (in this skill directory). It wraps Xvfb, Maven, xdotool, and ImageMagick `import` into shell helpers. Source it, then call the helpers.

---

## Prerequisites

All dependencies come from Nix — no `apt-get` required. The host must have:

- `nix` with flakes enabled (already present on this machine)
- `Xvfb` — `which Xvfb` → `/run/current-system/sw/bin/Xvfb` ✓
- `import` (ImageMagick) — `which import` → `/run/current-system/sw/bin/import` ✓
- `xdotool` fetched on demand via `nix shell nixpkgs#xdotool`

The `flake.nix` in the repo root specifies the exact JDK and native libs. **Do not override `JAVA_HOME` manually** — let `nix develop` set it.

---

## Build

Run once per session (subsequent runs skip recompilation):

```bash
cd /home/thales/Projects/Voteflix
nix develop --command bash -c "./mvnw -pl common install -DskipTests -q"
```

---

## Run — agent path (primary)

```bash
cd /home/thales/Projects/Voteflix
source .claude/skills/run-voteflix/driver.sh

# 1. Start virtual display
vfx_start_display       # Xvfb on :99

# 2. Start server (blocks until "Server Online!" log line appears)
vfx_start_server /tmp/vfx-server.log

# 3. Start client
vfx_start_client /tmp/vfx-client.log

# 4. Wait for the JavaFX window to appear
vfx_wait_window         # sets $VFX_WIN_ID

# 5. Screenshot the login page
vfx_ss /tmp/vfx-login.png

# 6. Log in (admin/admin or sapo/sapo — see test users below)
vfx_login admin admin

# 7. Navigate to a page
vfx_nav Movies
vfx_ss /tmp/vfx-movies.png

vfx_nav Reviews
vfx_ss /tmp/vfx-reviews.png

vfx_nav Users
vfx_ss /tmp/vfx-users.png

# 8. Raw click / type for ad-hoc interaction
vfx_click 640 339       # x y in root-window pixels
vfx_type  "hello"

# 9. Stop everything
vfx_stop
```

### Driver helper reference

| Helper | What it does |
|---|---|
| `vfx_start_display` | Starts Xvfb on `:99` if not already running |
| `vfx_start_server [log]` | Launches server via nix develop; waits for "Server Online!" |
| `vfx_start_client [log]` | Launches client via nix develop in background |
| `vfx_wait_window` | Polls until the java window appears; sets `$VFX_WIN_ID` |
| `vfx_ss <path>` | Screenshots the full `:99` display with ImageMagick `import` |
| `vfx_click <x> <y>` | Mouse-move + left-click via xdotool |
| `vfx_type <text>` | Types into the focused widget via xdotool |
| `vfx_login <user> <pass>` | Full login flow: Connect → fill credentials → Sign In |
| `vfx_nav <tab>` | Clicks nav bar: `Reviews` \| `Movies` \| `Users` \| `Logout` |
| `vfx_stop` | Kills client, server, Xvfb |

### Nav bar click coordinates (window ≈ 120–1160 px in 1280-wide display)

| Tab | x | y |
|---|---|---|
| Reviews | 169 | 81 |
| Movies | 255 | 81 |
| Users | 335 | 81 |
| Logout | 1113 | 81 |

### Login page coordinates

| Element | x | y |
|---|---|---|
| Connect button | 495 | 658 |
| Username field | 640 | 339 |
| Password field | 640 | 415 |
| Sign In button | 562 | 466 |

---

## Test users

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | admin — verified ✓ |
| `sapo` | unknown | regular user — 2 reviews pre-seeded |
| `carlos` | unknown | regular user — 4 reviews pre-seeded |

**Only `admin/admin` has been verified in this environment.** The other bcrypt hashes in `server/src/main/resources/populate_database.sql` do not correspond to the username as password. To test review cards as a non-admin: log in as `admin`, navigate to Movies, open a movie, and post a review via the New Review form. Then view it under Reviews.

---

## Run — human path

```bash
# Terminal 1 — server
nix run /home/thales/Projects/Voteflix#server

# Terminal 2 — client (opens a GUI window)
nix run /home/thales/Projects/Voteflix#client
```

This opens a real window. Useless in headless containers.

---

## Gotchas

- **JavaFX 24 requires JDK 23+ AND the matching native GTK libs.** JDK 21 (which also lives in the nix store) has the GTK natives but cannot load JavaFX 24 jars (wrong class-file version). JDK 23 in the store lacks the GTK natives. The `nix develop` environment wires both correctly via `LD_LIBRARY_PATH` — do **not** bypass it.

- **`xdotool getactivewindow` fails** — Xvfb has no window manager, so `_NET_ACTIVE_WINDOW` is unset. Use `xdotool search --class "java"` instead. `vfx_wait_window` does this.

- **`xdotool windowactivate` fails** for the same reason. Clicks work fine without activation; just `mousemove` then `click`.

- **`nix shell nixpkgs#xdotool` is fetched on first use** (~80 KiB download, cached after that). Expect a one-time delay.

- **`./mvnw javafx:run` without `nix develop`** fails with `no glassgtk3 in java.library.path`. Always wrap Maven in `nix develop --command bash -c "..."`.

- **`-pl common,client javafx:run` does not work** — the javafx-maven-plugin only applies to the current project, not multi-module. Build common first (`install`), then run client separately.

- **Server port is 20737** (hard-coded default). The client's Connect bar defaults to `localhost:20737`.

- **`Prism ES2 Error - nInitialize: glXChooseFBConfig failed`** — harmless warning. JavaFX falls back to software rendering automatically. The app renders fine.

- **Screenshots are of the full 1280×800 virtual display.** The JavaFX window sits at roughly x=120–1160, y=60–680 inside it. Crop with `magick /tmp/vfx-*.png -crop 1040x620+120+60 +repage out.png` if you want just the window.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Error: --add-modules requires modules to be specified` | You ran Maven outside `nix develop`. Wrap with `nix develop --command bash -c "..."`. |
| `Caused by: java.lang.UnsatisfiedLinkError: no glassgtk3` | Same — missing LD_LIBRARY_PATH. Use nix develop. |
| `libgthread-2.0.so.0: wrong ELF class: ELFCLASS32` | You manually set LD_LIBRARY_PATH to a 32-bit glib. Let nix develop handle it. |
| Blank screenshot after `vfx_start_client` | Client is still launching. Call `vfx_wait_window` before `vfx_ss`. |
| `xdotool search --class "java"` returns nothing | Client hasn't opened its window yet. Increase sleep or retry in a loop. |
| Login → stays on login page | Wrong credentials, or server not running. Check `/tmp/vfx-server.log`. |
| `No plugin found for prefix 'javafx'` | You ran `./mvnw javafx:run` from the repo root instead of `-pl client`. |
