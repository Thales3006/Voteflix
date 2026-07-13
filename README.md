# Voteflix

A desktop movie review platform built on a custom client-server architecture over raw TCP.

<table>
  <tr>
    <td align="center">
      <strong>Login Screen</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/823a6787-7291-4809-976e-948910168c95" width="420" alt="login-screen"/>
    </td>
    <td align="center">
      <strong>Movie Edit Screen (Admin)</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/5edbcd8a-3830-4201-a7cc-22dc877043a4" width="420" alt="admin-movie-screen"/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <strong>Movie Screen</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/c55f8dd4-f417-42cb-98d5-bc8ec4eaf464" width="420" alt="movie-screen"/>
    </td>
    <td align="center">
      <strong>Users Screen (Admin)</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/81529840-e798-4813-a4d4-d55816726059" width="420" alt="admin-user-screen"/>
    </td>
  </tr>
</table>


## About

Voteflix is a full-stack Java application where users can browse a movie catalog, write reviews, and manage their profiles. It was built as the practical deliverable for a Distributed Systems university course, with the goal of designing and implementing a custom application-level protocol from scratch, without relying on HTTP or any existing web framework.

The protocol defines every message exchanged between client and server as a typed, validated JSON object sent over short-lived TCP connections. The server enforces JSON Schema validation on every incoming request, and the client validates all input before sending. Both sides share the same protocol definition through a common library.

---

## Features

**Regular users**
- Register and log in with a username and password
- Browse the full movie catalog with title, director, year, genres, and synopsis
- See the average rating and number of reviews for each movie
- Write a review (title, description, score from 1 to 5) for any movie
- View, edit, and delete their own reviews from a dedicated page
- Update or delete their own account

**Admin**
- All regular user features
- Create, update, and delete movies in the catalog
- View the full user list and manage other accounts

---

## Architecture

The project is organized as three Maven modules:

```
voteflix/
├── common/    # Shared protocol: models, serialization, validation
├── server/    # TCP server, business logic, SQLite persistence
└── client/    # JavaFX desktop GUI
```

### Communication

Each request opens a new TCP connection, sends a single JSON line, reads the response, and closes. The operation type is embedded in the JSON payload (`"operation": "LOGIN"`, `"operation": "CREATE_REVIEW"`, etc.), which lets the server dispatch each message to the right handler without any URL routing.

```
Client                          Server
  │── TCP connect ───────────────>│
  │── JSON request ──────────────>│
  │<─ JSON response ──────────────│
  │── TCP close ─────────────────>│
```

### Common module

The `common` module is shared by both sides and defines the full protocol surface:

- **`Request` / `Response`** — sealed interfaces with one record per operation, giving compile-time exhaustiveness in switch expressions
- **`Operation`** enum — maps every operation code string to the corresponding response shape
- **`Validator`** — singleton that loads JSON Schemas from the classpath and validates every message before deserialization
- **`RequestSerializer` / `ResponseBuilder`** — convert between typed Java objects and JSON strings
- JSON Schemas in `src/main/resources/schemas/` covering all 14 operations and their responses

### Server module

- **`ServerListener`** — opens a `ServerSocket`, logs its local IP and port, and spawns a new `ClientHandler` thread for every accepted connection
- **`ClientHandler`** — thread-per-connection: reads one request line, delegates to `ServerService`, writes the response, and closes
- **`ServerService`** — validates and deserializes the raw message, dispatches to the right service via a pattern-matching switch, and handles error serialization; also writes timestamped JSON logs to a configurable file
- **`JwtService`** — issues and verifies HMAC-256 signed JWT tokens; every request that requires authentication carries the token in the JSON body
- **`UserService` / `MovieService` / `ReviewService`** — implement `CrudService<T>` and enforce authorization rules (ownership checks, admin-only gates)
- **`UserRepository` / `MovieRepository` / `ReviewRepository`** — JDBC-based data access with prepared statements and manual transactions where needed
- **`SQLiteDatabase`** — wraps a JDBC `DriverManager` connection, enables WAL journal mode and a 5-second busy timeout on startup

### Client module

- **`ClientService`** — singleton that holds the active session state (JWT token, user ID, username, admin flag); each API call opens a `ClientSocket`, serializes the request, sends it, and parses the response
- **`ClientSocket`** — creates a new `Socket` per call, writes the request line, reads the response line, and closes
- **`SceneController`** — base class for all JavaFX controllers; handles `StatusException` display, automatic redirect to the login screen on `UNAUTHORIZED`, and `ValidationException` popups
- **`LoginController`** — server connection form (IP + port probe before marking as connected), register and sign-in forms with client-side input guards
- **`MovieController`** — movie grid with cards; clicking a card opens a slide-in overlay with the full movie details and its reviews; admin users see editable fields and CRUD buttons
- **`ReviewController`** — lists the current user's own reviews; clicking a card opens an overlay for editing or deleting
- **`UserController`** — profile page with avatar initial; admin users also see the full user list and can update or delete any account
- **`Validate`** utility — pre-flight checks (length, character set, numeric range, genre whitelist) that throw `ValidationException` before any network call is made

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 23 |
| GUI | JavaFX 24 (FXML + Controls) |
| Build | Maven (multi-module) |
| Database | SQLite via `sqlite-jdbc` |
| Authentication | Auth0 `java-jwt` (HMAC-256) |
| Password hashing | jBCrypt |
| JSON Schema validation | `everit-json-schema` |
| JSON | `org.json` |
| Boilerplate reduction | Lombok |
| Environment management | Nix (flakes) |

---

## Dependencies

All Java dependencies are declared in the root `pom.xml` and managed by Maven:

| Artifact | Version | Purpose |
|---|---|---|
| `org.openjfx:javafx-fxml` | 24.0.2 | JavaFX FXML support |
| `org.openjfx:javafx-controls` | 24.0.2 | JavaFX UI controls |
| `com.auth0:java-jwt` | 4.5.0 | JWT token generation and verification |
| `org.mindrot:jbcrypt` | 0.4 | BCrypt password hashing |
| `org.xerial:sqlite-jdbc` | 3.46.0.0 | SQLite JDBC driver |
| `com.github.erosb:everit-json-schema` | 1.14.6 | JSON Schema validation |
| `org.apache.logging.log4j:log4j-core` | 2.20.0 | Logging |
| `org.projectlombok:lombok` | 1.18.32 | Boilerplate generation (annotation processor) |

---

## Getting Started

### Prerequisites

**With Nix (recommended)**

Install [Nix](https://nixos.org/download/) with flakes enabled. Nix handles Java, Maven, and all native libraries (including the JavaFX native bindings) automatically.

**Without Nix**

- Java 23+
- Maven 3.9+
- On Linux, the JavaFX native libraries need to be available (`libGL`, `gtk3`, `libX11`, etc.)

---

### Configuration

The server reads a `.env` file from its module directory. Copy the example and fill in your values:

```bash
cp server/example.env server/.env
```

`server/.env`:
```
JWT_SECRET=your-256-bit-secret-key-here
DB_PATH=./data/voteflix.db
LOG_PATH=./data/log.txt
```

- **`JWT_SECRET`** — any string; used to sign and verify all session tokens. Change this before any real deployment.
- **`DB_PATH`** — path to the SQLite database file (relative to the `server/` directory or absolute).
- **`LOG_PATH`** — path to the message log file.

If `.env` is missing, the server falls back to built-in defaults (`./data/voteflix.db`, `./data/log.txt`, and a placeholder secret).

---

### Database Setup

The schema and seed data are plain SQL scripts in `server/src/main/resources/`. The server creates the database file automatically on first run by reading those scripts via `SQLiteDatabase.init()`.

To create the database manually (requires `sqlite3` in PATH):

```bash
# Create schema and populate with seed data
./server/scripts/create_db.sh

# Remove the database to start fresh
./server/scripts/delete_db.sh
```

The seed data includes 14 movies, 4 genres per movie on average, and a handful of sample reviews written in Brazilian Portuguese.

---

### Running

#### With Nix

```bash
# Start the server
nix run .#server

# Start the client (in a separate terminal)
nix run .#client
```

#### With Maven

Build the shared library first, then run each module separately:

```bash
# Install the common module
mvn -pl common install -DskipTests

# Start the server
mvn -pl server exec:java

# Start the client (in a separate terminal)
mvn -pl client javafx:run
```

The server listens on port `20737` by default. To use a different port, pass it as an argument or set the `PORT` environment variable:

```bash
PORT=8080 mvn -pl server exec:java
# or
mvn -pl server exec:java -Dexec.args="8080"
```

---

### Connecting the Client

On the login screen, enter the server's IP address and port, then click **Connect**. Once connected, you can register a new account or sign in with an existing one.

---

## Default Accounts

The seed data ships with the following pre-created accounts (password matches the username):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | Administrator |
| `sapo` | `sapo` | Regular user |
| `carlos` | `carlos` | Regular user |
| `marina` | `marina` | Regular user |
| `lucas` | `lucas` | Regular user |
