# AGENTS.md — AI development contract for robot-2026-hamseason

> This file is the repository-wide contract for AI coding agents (Cline, Codex, Cursor,
> Claude Code, Copilot, …). It complements the local, machine-specific notes in
> `.clinerules` (listed in `.gitignore` — meant to stay local, not shared). Read this
> before editing code.

## What this project is

An **FRC 2026** Java/Kotlin robot built on:
- **GradleRIO 2026.2.1** (Gradle 8.11), Java source/target **17**
- **AdvantageKit (Junction) 26.0.2** — the log/replay/simulation framework. Everything
  important is logged with `Logger.recordOutput(...)` and can be replayed with
  AdvantageScope (`replayWatch`) and re-run in the simulator (`simulateJava`).
- PathPlanner, REVLib, Phoenix 5/6, WPILibNewCommands (see `vendordeps/`).

### Source layout
- `src/main/java/frc/robot/` — the robot program: `Main`, `commands/`, `subsystems/`,
  `util/`, generated `BuildConstants`.
- `src/main/java/miscar/` — team library code: `kalman/` (EKF/UKF localization),
  `swerve/`, `limeLightVision/`, `mecsIOs/`, `motorIOs/`, `servos/`, `configs/`, `util/`.
- `src/test/java/tests/` — JUnit tests (currently under `tests/miscar/...`).
- `src/main/deploy/` — deployed files incl. `autos/` and `pathplanner/` autos/paths.
- `vendordeps/` — vendor (`config`-style JSON) dependencies.
- Simulation sim logs: `logs/sim/*.wpilog` (AdvantageKit). Robot logs: `logs/*.wpilog`.
- AdvantageScope layouts: `AdvantageScope Simulation.json`, `advantagesScope/`.

## Build environment (required before every Gradle command)
`java`/`javac` are **not** on PATH and `JAVA_HOME` is not global. The only correct JDK is
the WPILib-2026 Temurin OpenJDK 17.0.16:

```powershell
$env:JAVA_HOME = 'C:\Users\Public\wpilib\2026\jdk'
```

Do **not** try to find another JDK (2025 / 2027_alpha JDKs exist on the machine but must
not be used for this branch). `gradlew.bat` will not start without `JAVA_HOME` set.

## Canonical commands (run from repo root, with the above `JAVA_HOME` set)
| Task | Command |
|---|---|
| Compile + auto-format | `.\gradlew.bat compileJava` (runs `spotlessApply`) |
| Full build (compiles + tests) | `.\gradlew.bat build` |
| Run tests | `.\gradlew.bat test` |
| Format-only check | `.\gradlew.bat spotlessCheck` |
| Fix formatting in place | `.\gradlew.bat spotlessApply` |
| Run the AdvantageKit simulator | `.\gradlew.bat simulateJava` |
| Replay a log in AdvantageScope | `.\gradlew.bat replayWatch` |

## Rules for AI agents

### 1. Verification first — never claim "done" without proof
A change is **not complete** until it compiles, satisfies Spotless, and passes `test`
(plus is verified in sim for any behavioral change, see rule 2). Always run the relevant
command and report the actual output. Do **not** say "this should work" and skip the run.

```powershell
$env:JAVA_HOME = 'C:\Users\Public\wpilib\2026\jdk'
.\gradlew.bat compileJava test
```

### 2. Closed-loop simulation rule (the "AI agents run the sim and read logs" workflow)
Mirroring Team 254's *"The Next Revolution: AI in FRC"* approach, behavior changes must be
**verified in simulation**:

1. **Describe the intent in plain language** (e.g. *"extend the simulation to handle
   driving over the bump"*).
2. **Edit code** to implement it.
3. **Run the simulator**: `.\gradlew.bat simulateJava` (writes `logs/sim/akit_*.wpilog`).
4. **Read the resulting log** and confirm the expected outcome before accepting the change.
   Logs are analyzed with the `.wpilog` MCP / `tools` described in `docs/ai-workflow.md`
   (e.g. the `artemis` MCP reads AdvantageKit `.wpilog` + live NT4), or with
   `py/log_summary.py` as a quick check.

The simulator is the **ground truth**. Never accept an AI change to robot behavior that has
not been logged-and-verified. If you cannot run the sim, say so explicitly instead of
overstating verification.

### 3. Vibe-coding responsibly
- Delegating boilerplate, refactors, tests, debugging, and documentation to the agent is
  encouraged ("let AI do the work").
- **Control-critical logic** (Kalman/swerve localization, vision, safety, CAN/motor I/O)
  must be **human-reviewed** even when AI-assisted.
- Logging/observability matter: prefer adding `Logger.recordOutput(...)` so logs are rich
  enough for the closed-loop workflow. Do not strip existing logs.
- Tag AI-assisted commits with `#ai` in the message so they are easy to audit.

### 4. Correctness over proxies
- Use **version-accurate WPILib/AdvantageKit docs** (the WPILib docs MCP or the docs in the
  local Maven caches) rather than model memory — this codebase targets WPILib 2026 /
  AdvantageKit 26.0.2. AI trained on older seasons frequently produces breaking-changes
  code that does not compile; always compile to prove it.
  *Note:* `settings.gradle` still says `frcYear = '2025'` (line 7). It only affects the
  plugin-resolution repo path and is intentionally left; do **not** "fix" it.

### 5. Conventions to preserve
- Follow the existing package split (`frc.robot.*` vs `miscar.*`) — do not invent new
  top-level packages for existing concerns.
- Match surrounding style: Spotless (Eclipse formatter config `eclipse-formatter.xml`),
  constants as `kCamelCase` finals, no trailing whitespace, newline at EOF.
- Kotlin is in use; keep new files consistent with the nearest sibling file's language.
- **Do not** edit `vendordeps/`, `build.gradle` configuration, or the JDK toolchain without
  explicit instruction.

## MCP tooling (optional but recommended)
See `docs/ai-workflow.md` for installation. Community FRC MCP servers that fit this stack:
- `GabrielNakamoto/artemis` — reads `.wpilog` logs + live NT4 (the "read the logs" tool).
- `ramalamadingdong/frc-rag-mcpserver` — version-accurate WPILib docs (RAG).
- `o-bots7160/AdvantageScope-mcp` — builds AdvantageScope layout JSON.
