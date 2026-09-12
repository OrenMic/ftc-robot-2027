# AI-assisted development workflow (based on Team 254's *The Next Revolution: AI in FRC*)

This repo is set up so AI coding agents can be productive **and accountable**. The rules
below mirror the workflow FRC Team 254 (The Cheesy Poofs) presented at the **2026 FIRST
Championship Conference** (*"[The Next Revolution: AI in FRC](https://www.chiefdelphi.com/t/2026-championship-conference-presentation-the-next-revolution-ai-in-frc-by-254/519529)"*,
by Jared Russell, Tom Botiglieri & schang). Team 254's headline idea: **AI agents that can
run the simulation and read the logs let you go from a plain-language request to a change
that is verified in simulation** — e.g. *"extend the simulation to handle driving over the
bump"* or *"add a second pass in auto and optimize the number of balls it scores."*

The committed rule files that encode this are:
- **`AGENTS.md`** — the tool-agnostic AI contract + repo map (read by any agent).
- **`.clinerules`** — local, machine-specific Cline notes (git-ignored).

---

## 1. The closed-loop rule (the important one)

> **Behavior changes are not "done" until they are verified in simulation.**

Workflow for an agent:

1. Describe intent in plain language.
2. Edit code.
3. `.\tools\sim.ps1` (runs `.\gradlew.bat simulateJava`) → writes `logs/sim/akit_*.wpilog`.
4. Read the newest log and confirm the expected outcome before accepting the change.
5. Report the actual result — the sim is ground truth, not "it should work."

Logs are binary. To read them, use a `.wpilog`-aware tool (see MCP below) or replay with
`.\tools\sim.ps1`'s sibling `.\gradlew.bat replayWatch` (AdvantageScope GUI).

## 2. Verification gate (non-behavioral changes)

Any change must at minimum pass:

```powershell
powershell -File .\tools\verify.ps1   # spotlessApply + compileJava + test
```

CI enforces the same (`Build` → `spotlessCheck` → `test`) on every push/PR.

## 3. Tools

| Tool | Purpose | Command |
|---|---|---|
| `tools/verify.ps1` | Spotless + compile + tests | `powershell -File tools\verify.ps1` |
| `tools/sim.ps1` | Run AdvantageKit sim, report newest `logs/sim/*.wpilog` | `powershell -File tools\sim.ps1` |
| `py/log_summary.py` | Locate + print metadata of newest `.wpilog` | `python py\log_summary.py [--log <path>]` |
| `.vscode/tasks.json` | "Verify…" and "Run simulation" tasks | VS Code Tasks |

`py/log_summary.py` is intentionally thin: `.wpilog` is a binary format, and full parsing
is delegated to `.wpilog`-aware tooling (below) or AdvantageScope replay.

## 4. Optional but recommended: FRC MCP servers

Community MCP servers that slot straight into an agent client (Cline/Codex/Cursor/Claude
all speak MCP). These are third-party, community-grade projects — evaluate before adopting.

| Server | What it gives an agent |
|---|---|
| [`GabrielNakamoto/artemis`](https://github.com/GabrielNakamoto/artemis) | Reads **`.wpilog` logs** + live **NT4** (pid tuning in loop). This is the "read the logs" piece of the closed loop. |
| [`ramalamadingdong/frc-rag-mcpserver`](https://github.com/ramalamadingdong/frc-rag-mcpserver) | **Version-accurate WPILib docs** (RAG). Note: ships WPILib 2025.3.2; for this 2026 repo you'd regenerate/fork it with WPILib 2026 docs. |
| [`o-bots7160/AdvantageScope-mcp`](https://github.com/o-bots7160/AdvantageScope-mcp) | Build/edit **AdvantageScope** layout JSON (useful for `AdvantageScope Simulation.json`). |
| [`withinfocus/tba-mcp-server`](https://github.com/withinfocus/tba-mcp-server) | The Blue Alliance data (scouting/strategy pillar). |

### Example agent MCP config (Cline style, `tools/mcp.example.json`)

```json
{
  "mcpServers": {
    "frc-wpilog": {
      "command": "python",
      "args": ["<path-to-artemis>/apollo"]
    },
    "frc-wpilib-docs": {
      "command": "uv",
      "args": ["--directory", "<path-to-frc-rag-mcpserver>", "run", "server.py"]
    }
  }
}
```

Point these at your local checkout of the servers (see each repo's README for the exact
entrypoint/args — the details above are a template to be filled in, not copy-paste).

## 5. Vibe-coding responsibly — what AI should vs shouldn't own

| Usually fine to delegate | Always **human-review** |
|---|---|
| Boilerplate, enums, config structs | Kalman / swerve / **localization** |
| Refactors, extract helpers, dead-code removal | **Vision** (limeLightVision) |
| Unit tests, docs, dashboard/layout JSON | Safety, CAN/motor I/O (`mecsIOs`, `motorIOs`) |
| Debugging, log-parsing glue | Anything touching `vendordeps/` or `build.gradle` |

Tag AI-assisted commits with `#ai` so they're easy to audit. Prefer adding
`Logger.recordOutput(...)` so logs are rich enough for the closed loop.

## 6. Sources / background
- Team 254 presentation thread: <https://www.chiefdelphi.com/t/2026-championship-conference-presentation-the-next-revolution-ai-in-frc-by-254/519529>
- OpenAI Codex-for-FRC call-to-action (ecosystem context): <https://www.chiefdelphi.com/t/openai-codex-for-frc/520008>
- WPILog/NT4 MCP (`artemis`) thread: <https://www.chiefdelphi.com/t/wip-model-context-protocol-for-frc-wpilog-and-nt4/517780>
- Team 254 public code conventions: <https://github.com/Team254/FRC-2025-Public> (`README.md`)
