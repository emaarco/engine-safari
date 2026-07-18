package de.emaarco.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CibSevenRemoteExample

fun main(args: Array<String>) {
    runApplication<CibSevenRemoteExample>(*args)
}
