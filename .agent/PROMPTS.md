# Standardized Agent Prompts

Use these prompts to guide your reasoning and interactions when working on specific Apiden tasks.

## ✍️ Coding Prompts

### Implementing a New Controller
> "I need to create a new Micronaut controller for the 'User' domain. It must follow the Apiden standards: use the `ApiObject` envelope, all fields and parameters must be `final`, the constructor must be package-private, and I must use Java 21 Records for the DTO."

### Adding a Domain Service
> "Create a service class for business logic. Ensure it uses constructor injection for dependencies, mark all fields as `final`, and include Javadocs for every public method. Ensure the class itself is `final`."

## 🔍 Code Review Prompts

### Compliance Check
> "Review this class for Apiden compliance. Check for: 2-space indentation, `final` keywords on every variable/field, package-private constructors, and proper SLF4J logging patterns."

### Exception Handling Review
> "Analyze this method's error handling. Does it throw a custom `ApiException`? Does it log the error using a parameter-safe format? Does it ensure NO raw stacktrace leaks?"

## 🛠 Troubleshooting Prompts

### API Envelope Debugging
> "A client is receiving a malformed JSON response. Check the `ApiFilter` and `ApiObject` classes to see how the envelope is being constructed during egress. Verify if any custom binders are interfering."

### Startup Failures
> "The Micronaut application fails to start. Check `Main.java` and `application.properties`. Is there an issue with the AOT context or a missing dependency in `build.gradle.kts`?"
