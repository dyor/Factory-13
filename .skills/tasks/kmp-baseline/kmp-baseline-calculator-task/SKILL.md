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
| Phase 3: Hardware / Native Integrations | 30 | 30 | 0 | 100% |
| Phase 4: The Final Cut (Cleanup & Optimization) | 7 | 7 | 0 | 100% |
| Phase 5: Factory-Specific Polish | 13 | 4 | 9 | 30.7% |

## Actionable Insights
*   **Current Phase:** Phase 5: Factory-Specific Polish
*   **Next Task:** `- [ ] **Agent Action**: Refactor the Editing Studio timeline to dynamically generate exactly one block per second of the recorded video using \`MediaMetadataRetriever\` (Android) and \`AVURLAsset\` (iOS) to fetch the actual video duration.`
*   **Pending Breakdown:** 0 User Actions, 8 Agent Actions, 1 Validations remaining.

---

## Overall Project Progress
*   **Total Tasks**: 74
*   **Completed Tasks**: 65
*   **Pending Tasks**: 9
*   **Overall Completion**: 87.8%