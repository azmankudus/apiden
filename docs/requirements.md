This Markdown document has been updated to remove the Java-specific code implementation while retaining all other architectural, hierarchical, and logical rules for the `APIDEN` system.

---

# APIDEN Project Traceability & Testing Guide

This document defines the relationship between the `APIDEN` YAML requirements and the technical unit test implementation.

## 1. Project Hierarchy & ID Traceability

The system uses a strict four-layer hierarchy. The ID of a child element always contains the ID of its parent to ensure 100% traceability.

| Level | ID Pattern | Example | Success Criteria |
| --- | --- | --- | --- |
| **Epic** | `APIDEN-E[XX]` | `APIDEN-E01` | Array (Multiple) |
| **Feature** | `APIDEN-F[XXXX]` | `APIDEN-F0101` | Array (Multiple) |
| **Story** | `APIDEN-S[XXXXXX]` | `APIDEN-S010101` | Array (Multiple) |
| **Task** | `APIDEN-T[XXXXXXXX]` | `APIDEN-T01010101` | **Single** (Laser-focused) |

---

## 2. Package & Class Structure

To maintain a "Mirror Image" between requirements and code, the directory structure follows the hierarchy. The Task ID and a short description are used as the filename.

### Package Convention:

`package com.apiden.e[XX].f[XXXX].s[XXXXXX];`

### Class Naming Convention:

`T[ID]_[Short_Description].java`

### Directory Example:

```text
src/test/java/com/apiden/
└── e01_auth/                          
    └── f0101_google_login/            
        └── s010101_oidc_flow/         
            ├── T01010101_JwtValidator.java    
            └── T01010102_EmailExtractor.java  

```

> **Note:** Use underscores (`_`) instead of dashes (`-`) or spaces to comply with standard programming language naming rules for files and classes.

---

## 3. Data-Driven Unit Testing Logic

Each Task Class implements the `unit_tests` defined in the YAML. This ensures that a single technical unit is tested against multiple scenarios (Nulls, Blanks, and Valid Data).

### Assertion Operators

The test runner logic maps the YAML `operator` to the following logical assertions:

| Operator | Logic Description |
| --- | --- |
| **EQUALS** | Exact match between actual and expected. |
| **STARTS_WITH** | Actual output begins with the expected string. |
| **ENDS_WITH** | Actual output ends with the expected string. |
| **CONTAINS** | Expected substring exists within the actual output. |
| **MATCHES_REGEX** | Actual output matches the provided Regular Expression. |
| **IS_NULL** | Actual output is strictly null. |
| **IS_NOT_NULL** | Actual output is anything other than null. |
| **IS_EMPTY** | Actual output has a length of zero. |
| **IS_BLANK** | Actual output is null, empty, or whitespace-only. |

---

## 4. Summary of Use

1. **Validation:** Use `project_schema.json` to ensure your YAML is structurally sound and follows the regex ID patterns.
2. **Organization:** Navigate the test folder using the descriptions in the filenames for quick access to specific logic.
3. **Execution:** A task is considered complete only when every `unit_tests` scenario in the YAML passes for that specific test class.
4. **Case Sensitivity:** Respect the `ignore_case` flag by normalizing both actual and expected strings to the same case before comparison.

Would you like me to provide the final, cleaned-up **JSON Schema** to match this documentation?