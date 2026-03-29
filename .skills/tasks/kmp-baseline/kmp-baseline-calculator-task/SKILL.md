---
name: kmp-baseline-calculator-task
description: Tracks and calculates the overall completion percentage of the project based on the kmp-baseline-guide-task tasks.
---

# Progress Calculator

## When to use this skill
- Use this when needing to update or view the current completion percentage of the project.
- This is helpful for providing a summary of how many tasks are completed versus pending.

## How to use it
- Reference the latest kmp-baseline-guide-task status.
- Tally the `[x]` vs `[ ]` tasks to output an updated metric.

---

# Project Progress Calculation

## Methodology
Progress is calculated by evaluating the completed (`- [x]`) versus pending (`- [ ]`) tasks defined across all phases in the `.skills/tasks/kmp-baseline/kmp-baseline-guide-task/SKILL.md` file.

## Phase Breakdown

| Phase | Total Tasks | Completed | Pending | Completion % |
|-------|-------------|-----------|---------|--------------|
| Phase 1: Foundation & Infrastructure | 16 | 16 | 0 | 100% |
| Phase 2: Core Features & Logic | 8 | 8 | 0 | 100% |
| Phase 3: Hardware / Native Integrations | 30 | 11 | 19 | 36.7% |
| Phase 4: The Final Cut (Cleanup & Optimization) | 7 | 0 | 7 | 0% |
| Phase 5: Factory-Specific Polish | 13 | 0 | 13 | 0% |

## Actionable Insights
*   **Current Phase:** Phase 3: Hardware / Native Integrations
*   **Next Task:** `- [ ] **Agent Action**: Allow the user to navigate to the Recording Studio after they save a script.`
*   **Pending Breakdown:** 2 User Actions, 32 Agent Actions, 7 Validations remaining.

---

## Overall Project Progress
*   **Total Tasks**: 74
*   **Completed Tasks**: 35
*   **Pending Tasks**: 39
*   **Overall Completion**: 47.3%