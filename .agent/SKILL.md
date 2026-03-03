# Skill: Apiden Developer Agent

## Description
A specialized agentic skill for developing, maintaining, and refactoring the **Apiden** codebase. This skill ensures strict adherence to **Java 21**, **Micronaut 4**, and project-specific **Immutability & Visibility** standards.

## 🎯 Primary Directives

### 1. The Immutability Mandate
- **All fields** in classes, records, and services MUST be `final`.
- **Method parameters** MUST be `final`.
- **Local variables** MUST be `final` unless loops require mutation (prefer streams).

### 2. Visibility & Interaction Rules
- **Constructors** for all DI-managed units (Services, Controllers, Handlers) MUST be **Package-Private**.
- **Classes** SHOULD be `final` by default.
- **Envelopes**: NEVER return raw objects. Always ensure the `ApiObject` wrapper is applied.

### 3. Documentation Excellence
- Use **Javadoc** for all logic-bearing methods (parameters, returns, exceptions).
- Use **SLF4J** with parameterized logging (`log.info("id={}", id)`). No string concatenation.

## 🛠 Required Capabilities

- **Code Generation**: Writing Java 21 code using Records and Pattern Matching.
- **Architectural Guarding**: Preventing leaks across the `shared.api` and `module` boundaries.
- **Validation**: Checking against `.editorconfig` (2 spaces) and `TECHNICAL_SPEC.md` requirements.

## 🚨 Anti-Hallucination Protocol

1. **Verify Dependencies**: Do not assume libraries exist. Check `build.gradle.kts` first.
2. **Context First**: Always read `.agent/CONTEXT.md` before suggesting structural changes.
3. **Convention First**: Follow the existing patterns in `src/main/java/com/example/apiden/shared/api/`.
