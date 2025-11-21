rootProject.name = "kotlin-validator"

// Core modules
include(":kotlin-validator-annotations")
include(":kotlin-validator-runtime")
include(":kotlin-validator-engine")
include(":kotlin-validator-processor")

// Framework adapters
include(":kotlin-validator-spring-webflux")
include(":kotlin-validator-spring-mvc")
include(":kotlin-validator-ktor")

// Testing
include(":kotlin-validator-testing")

// Map logical paths to physical directories
project(":kotlin-validator-annotations").projectDir = file("core/annotations")
project(":kotlin-validator-runtime").projectDir = file("core/runtime")
project(":kotlin-validator-engine").projectDir = file("core/engine")
project(":kotlin-validator-processor").projectDir = file("core/processor")
project(":kotlin-validator-spring-webflux").projectDir = file("adapters/spring-webflux")
project(":kotlin-validator-spring-mvc").projectDir = file("adapters/spring-mvc")
project(":kotlin-validator-ktor").projectDir = file("adapters/ktor")
project(":kotlin-validator-testing").projectDir = file("testing/integration-tests")
