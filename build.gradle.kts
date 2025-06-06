import org.springframework.boot.gradle.tasks.bundling.BootWar

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	war
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.rutify"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	maven {
		url = uri("https://maven.google.com/")
	}
	mavenLocal()
	flatDir {
		dirs("libs")
	}
}



dependencies {
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.google.firebase:firebase-admin:9.4.3")
	implementation("com.google.firebase:firebase-firestore:25.1.3")

	implementation("org.mongodb:mongodb-driver-kotlin-sync:5.3.0")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.google.protobuf:protobuf-java:4.28.2")
	implementation("io.grpc:grpc-netty-shaded:1.63.0")
	implementation("io.grpc:grpc-protobuf:1.63.0")
	implementation("io.grpc:grpc-stub:1.63.0")

	implementation("com.cloudinary:cloudinary-http44:1.34.0")

	testImplementation("io.mockk:mockk:1.13.7")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage")
		exclude(module = "mockito-core")
	}
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.mockito:mockito-core:5.5.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("com.stripe:stripe-java:23.0.0")


}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
