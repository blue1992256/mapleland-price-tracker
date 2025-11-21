# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Language Preference

**IMPORTANT: All responses and communications should be in Korean (한글).**

## Project Overview

Spring Boot 3.5.7 application for price tracking, using Java 17 and Gradle as the build system. The project uses Lombok for reducing boilerplate code and is structured as a standard Spring Boot web application.

## Build System

This project uses Gradle with the Gradle wrapper. **Always use `./gradlew` (or `gradlew.bat` on Windows) instead of a globally installed Gradle.**

### Common Commands

**Build the project:**
```bash
./gradlew build
```

**Run the application:**
```bash
./gradlew bootRun
```

**Run all tests:**
```bash
./gradlew test
```

**Run a single test class:**
```bash
./gradlew test --tests com.nangoso.pricetracker.YourTestClass
```

**Run a single test method:**
```bash
./gradlew test --tests com.nangoso.pricetracker.YourTestClass.testMethodName
```

**Clean build artifacts:**
```bash
./gradlew clean
```

**Build without running tests:**
```bash
./gradlew build -x test
```

## Architecture

**Package structure:** `com.nangoso.pricetracker`

**Main application class:** `PriceTrackerApplication` - Standard Spring Boot entry point with `@SpringBootApplication` annotation.

**Framework:** Spring Boot 3.5.7 with Spring Web starter for RESTful web services.

**Java version:** 17 (configured via toolchain in build.gradle)

**Key dependencies:**
- Spring Boot Starter Web - for building REST APIs and web applications
- Lombok - for reducing boilerplate (getters, setters, constructors, etc.)
- JUnit Platform - for testing

## Development Notes

**Lombok:** This project uses Lombok annotations. Your IDE should have Lombok annotation processing enabled. Common annotations used: `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`, etc.

**Application properties:** Configuration is located in `src/main/resources/application.properties`. The application name is set to "price-tracker".

**Static resources:** Place static files in `src/main/resources/static/`

**Templates:** Place template files in `src/main/resources/templates/`
