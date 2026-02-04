# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is an Agent OS configuration repository for Claude Code. It provides a structured, multi-phase workflow system for software development through specialized agents and commands that handle everything from product planning to feature implementation.

## Architecture

### Core Components

**`.claude/` Directory**
- `commands/agent-os/` - Contains skill definitions for Claude Code commands (`/shape-spec`, `/write-spec`, etc.)
- `agents/agent-os/` - Agent definitions for specialized subagents (spec-writer, implementer, etc.)

**`agent-os/` Directory**
- `config.yml` - Configuration file tracking Agent OS version and compilation settings
- `standards/` - Coding standards and conventions organized by domain:
  - `global/` - Project-wide standards (tech stack, coding style, conventions, error handling, validation)
  - `backend/` - Backend-specific standards (API, models, queries, migrations)
  - `frontend/` - Frontend-specific standards (components, CSS, accessibility, responsive design)
  - `testing/` - Testing standards and practices
- `specs/` - Feature specifications (created dynamically, structure: `YYYY-MM-DD-spec-name/`)
- `product/` - Product documentation (mission.md, roadmap.md, tech-stack.md)

### Workflow System

The Agent OS provides a structured development workflow:

1. **Product Planning** (`/plan-product`) - Creates mission, roadmap, and tech stack documentation
2. **Spec Shaping** (`/shape-spec`) - Initializes spec folder, gathers requirements through questions
3. **Spec Writing** (`/write-spec`) - Creates formal specification document from requirements
4. **Task Creation** (`/create-tasks`) - Breaks down specs into actionable task groups
5. **Implementation** - Two approaches:
   - `/implement-tasks` - Simple, direct implementation by implementer agent
   - `/orchestrate-tasks` - Advanced orchestration with multiple specialized agents and per-task-group standards

### Spec Folder Structure

Each spec follows this structure:
```
agent-os/specs/YYYY-MM-DD-spec-name/
├── planning/
│   ├── requirements.md
│   └── visuals/
├── spec.md
├── tasks.md
├── orchestration.yml (if using /orchestrate-tasks)
└── verifications/
    └── final-verification.md
```

### Configuration Settings

The `agent-os/config.yml` tracks:
- `profile` - Configuration profile in use
- `claude_code_commands` - Enables Claude Code command skills
- `use_claude_code_subagents` - Enables specialized subagent usage
- `agent_os_commands` - Legacy agent-os command support
- `standards_as_claude_code_skills` - Exposes standards as skills

## Standards System

Standards files define project conventions and are used during implementation:

- **All standards** - Include entire standards directory
- **Domain wildcard** - e.g., `frontend/*` includes all frontend standards
- **Specific files** - e.g., `backend/api.md` includes only that file

Standards are referenced in `orchestration.yml` when using `/orchestrate-tasks`.

## Key Principles

**Multi-Phase Processes**: Commands follow sequential phases with clear handoffs between specialized agents.

**Verification**: The implementation-verifier agent produces final verification reports after all tasks complete.

**Task Tracking**: Tasks are marked with `- [x]` in tasks.md as they're completed.

**Visual Assets**: Specs can include visual assets in `planning/visuals/` for reference during implementation.

## Important Notes

- This repository contains configuration and standards, not application code
- The actual codebase being developed lives elsewhere (location specified in product/tech-stack.md)
- Agent OS is updated via `~/agent-os/scripts/project-update.sh`
- Standards serve as guidelines for maintaining consistency across development
