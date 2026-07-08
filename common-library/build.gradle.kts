plugins {
    java
    id("io.spring.dependency-management")
}

dependencies {
    // Basic Spring web and context for validation exceptions and controller advice
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context")
    
    // JJWT libraries for token utilities
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Validation API for DTO verification
    implementation("jakarta.validation:jakarta.validation-api")

    // Jackson serialization
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Logging API
    implementation("org.slf4j:slf4j-api:2.0.12")
}
