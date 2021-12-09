import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.7"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
	id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "ru.apache-camel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.apache.camel.springboot:camel-spring-boot-starter:3.13.0")

	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework:spring-jdbc")

	implementation("org.liquibase:liquibase-core:4.4.3")

	implementation("org.apache.camel:camel-jdbc:3.4.2")

	 implementation("org.apache.camel:camel-jackson:3.13.0")

	implementation("org.apache.camel:camel-quartz:3.13.0")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

	implementation("commons-logging:commons-logging:1.2")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")


}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	from {
		image = "azul/zulu-openjdk:11"
	}
	to {
		setImage(provider { "dreg.citc.ru/ru.citc.education/$name:$version" })
	}
	container {
		environment = mapOf(
			"JAVA_TOOL_OPTIONS" to listOf(
				"-XX:MaxRAMPercentage=60",
				"-XX:+UseStringDeduplication",
			).joinToString(" ")
		)
		user = "nobody"
	}
}

tasks {
	withType<Test> {
		useJUnitPlatform()
		jvmArgs("-noverify", "-XX:TieredStopAtLevel=1")
		minHeapSize = "256M"
	}
	named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
		jvmArgs("-Xmx512M", "-Dspring.profiles.active=development", "-noverify", "-XX:TieredStopAtLevel=1")
	}
}
