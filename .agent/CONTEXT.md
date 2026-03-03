# Agent Project Context

This document provides the necessary background and "grounding" for an agent to work effectively on Apiden.

## 🏢 Project Identity
- **Name**: Apiden
- **Core Value**: Providing a bulletproof, standardized API foundation for JVM applications.
- **State**: Infrastructure complete; ready for domain module expansion.

## 🛠 Technical Grounding
- **Language**: Java 21 (LTS). Use modern features (Records, Switch Expressions, Virtual Threads).
- **Framework**: Micronaut 4.x. (Note: NOT Spring Boot. Do not hallucinate Spring annotations).
- **Indentation**: Explicitly **2 spaces** as per `.editorconfig`.
- **Core Library**: `micronaut-serde` (faster, safer serialization).

## 🧩 Architectural Constraints
- **Shared API**: Contains the base framework (Filters, Envelopes, Exceptions). This is the "Engine".
- **Modules**: Contains business logic. This is the "Car".
- **Communication**: Modules communicate with the outside world ONLY through the `shared.api` components.

## 🛡 Security & Safety
- **Information Leakage**: Stacktraces must not be exposed to clients by default.
- **Immutability**: Crucial for thread safety and predictable state management within the Netty event loops.
- **Visibility**: Minimal visibility (Package-Private) for constructors to encourage standard Dependency Injection.

## 📂 Key Source Paths
- **API Engine**: `src/main/java/com/example/apiden/shared/api/`
- **Domain Modules**: `src/main/java/com/example/apiden/module/`
- **Resources**: `src/main/resources/` (Check `application.properties` for active flags).
