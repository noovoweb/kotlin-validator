# kotlin-validator-spring-mvc

Spring MVC integration module for kotlin-validator with suspend function support.

## Features

- ✅ **Spring MVC Integration** - Seamless integration with Spring Boot MVC applications
- ✅ **Suspend Function Support** - Use suspend functions in validators for async I/O operations
- ✅ **Auto-Configuration** - Automatic setup with Spring Boot auto-configuration
- ✅ **Spring Validator Adapter** - Compatible with Spring's `@Valid` and `@Validated` annotations
- ✅ **Global Exception Handler** - Automatic conversion to structured REST API responses
- ✅ **Spring MessageSource** - Custom messages from `messages.properties`
- ✅ **Configuration Properties** - Externalized configuration via `application.properties`

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.noovoweb:kotlin-validator-spring-mvc:1.0.0")
}
```

## Usage

### 1. Define Validated Data Classes

```kotlin
@Validated
data class UserRegistration(
    @Required
    @Email
    val email: String?,

    @Required
    @MinLength(8)
    @CustomValidator("com.example.validators.PasswordValidator::validateStrength")
    val password: String?,

    @Required
    @Min(18.0)
    val age: Int?
)
```

### 2. Use in Spring MVC Controllers

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {

    @PostMapping
    fun register(@Valid @RequestBody user: UserRegistration): ResponseEntity<User> {
        // Validation happens automatically before this method
        // If validation fails, returns 422 with error details
        return ResponseEntity.ok(userService.create(user))
    }
}
```

### 3. Automatic Error Responses

When validation fails, the response is automatically formatted:

```json
{
  "status": 422,
  "error": "Validation Failed",
  "errors": {
    "email": ["Must be a valid email address"],
    "password": ["Password must contain uppercase, lowercase, digit, and special character"],
    "age": ["Must be at least 18"]
  }
}
```

## Suspend Function Support

Validators can use suspend functions for async I/O operations:

```kotlin
object UserValidators {
    suspend fun checkEmailUnique(email: String?, context: ValidationContext): Boolean {
        if (email == null) return true

        // Async database check
        return withContext(Dispatchers.IO) {
            !userRepository.existsByEmail(email)
        }
    }
}

@Validated
data class User(
    @CustomValidator("com.example.UserValidators::checkEmailUnique")
    val email: String?
)
```

The `SpringValidatorAdapter` uses `runBlocking` to bridge Spring MVC's blocking API with suspend functions.

## Configuration

Configure via `application.properties` or `application.yml`:

### application.properties
```properties
kotlin.validator.parallel-validation=true
kotlin.validator.locale=en_US
```

### application.yml
```yaml
kotlin:
  validator:
    parallel-validation: true
    locale: en_US
```

## Custom Configuration

Override defaults by defining your own beans:

```kotlin
@Configuration
class ValidatorConfig {

    @Bean
    fun validationContext(): ValidationContext {
        return ValidationContext(
            locale = Locale.FRENCH,
            parallelValidation = false,
            dispatcher = Dispatchers.IO
        )
    }

    @Bean
    fun userValidator(context: ValidationContext): Validator {
        return SpringValidatorAdapter(
            validator = UserValidator(),
            context = context,
            targetClass = User::class.java
        )
    }
}
```

## Custom Messages

Add custom messages to `messages.properties`:

```properties
# messages.properties
field.email=Please enter a valid email address
custom.password.strength=Password must be strong

# messages_fr.properties
field.email=Veuillez saisir une adresse e-mail valide
custom.password.strength=Le mot de passe doit être fort
```

The `SpringMessageProvider` automatically loads these messages and falls back to built-in validation messages.

## Components

### SpringValidatorAdapter
Adapts generated validators to Spring's `Validator` interface:
- Supports `@Valid` and `@Validated` annotations
- Executes suspend functions via `runBlocking`
- Converts `ValidationException` to Spring's `Errors`

### ValidationExceptionHandler
Global exception handler for REST APIs:
- Catches `ValidationException`
- Returns HTTP 422 Unprocessable Entity
- Structured JSON error responses

### SpringMessageProvider
Message provider integrating with Spring:
- Delegates to Spring's `MessageSource`
- Falls back to default validation messages
- Supports multiple locales

### ValidatorAutoConfiguration
Spring Boot auto-configuration:
- Automatically detects Spring MVC
- Configures default beans
- Enables configuration properties

## Example Project

```kotlin
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
@RequestMapping("/api")
class ApiController {

    @PostMapping("/register")
    fun register(@Valid @RequestBody user: UserRegistration): User {
        // Validation with suspend functions happens here
        return userService.register(user)
    }

    @GetMapping("/validate")
    fun validate(@Valid @ModelAttribute query: SearchQuery): List<Result> {
        // Works with @ModelAttribute, @RequestParam, etc.
        return searchService.search(query)
    }
}
```

## Differences from Spring WebFlux Module

| Feature | Spring MVC | Spring WebFlux |
|---------|------------|----------------|
| Programming Model | Blocking | Reactive (non-blocking) |
| Suspend Functions | Via `runBlocking` | Native support |
| Thread Model | Thread-per-request | Event loop |
| Use Case | Traditional REST APIs | Reactive streams, SSE |

## Testing

All components include comprehensive unit tests:
- `ValidatorPropertiesTest` - Configuration properties
- `SpringMessageProviderTest` - Message localization
- `ValidationExceptionHandlerTest` - Error handling
- `SpringValidatorAdapterTest` - Spring integration

Run tests:
```bash
./gradlew :kotlin-validator-spring-mvc:test
```

## License

Apache License 2.0
