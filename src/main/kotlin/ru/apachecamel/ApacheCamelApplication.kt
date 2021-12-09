package ru.apachecamel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApacheCamelApplication

fun main(args: Array<String>) {
    runApplication<ApacheCamelApplication>(*args)
}
