plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.volodya262.telegram"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
//	implementation("org.springframework.boot:spring-boot-starter-flyway")
//	implementation("org.springframework.boot:spring-boot-starter-validation")
//	implementation("org.flywaydb:flyway-database-postgresql")
//	runtimeOnly("org.postgresql:postgresql")

	implementation("org.telegram:telegrambots-springboot-longpolling-starter:9.6.0")
	implementation("org.telegram:telegrambots-client:9.6.0")

	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.wiremock:wiremock-standalone:3.13.2")
//	testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
//	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
//	testImplementation("org.testcontainers:testcontainers-postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("runRealApiTests", System.getProperty("runRealApiTests") ?: "false")
}
