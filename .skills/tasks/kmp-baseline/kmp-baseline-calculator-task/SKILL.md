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
| Phase 1: Foundation & Infrastructure | 16 | 1 | 15 | 6.25% |
| Phase 2: Core Features & Logic | 8 | 0 | 8 | 0% |
| Phase 3: Hardware / Native Integrations | 30 | 0 | 30 | 0% |
| Phase 4: The Final Cut (Cleanup & Optimization) | 7 | 0 | 7 | 0% |
| Phase 5: Factory-Specific Polish | 13 | 0 | 13 | 0% |

## Actionable Insights
*   **Current Phase:** Phase 1: Foundation & Infrastructure
*   **Next Task:** `- [ ] **User Action**: Change the Android Studio project view from "Android View" to "Project View"`
*   **Pending Breakdown:** 10 User Actions, 53 Agent Actions, 10 Validations remaining.

---

## Overall Project Progress
*   **Total Tasks**: 74
*   **Completed Tasks**: 1
*   **Pending Tasks**: 73
*   **Overall Completion**: 1.35%