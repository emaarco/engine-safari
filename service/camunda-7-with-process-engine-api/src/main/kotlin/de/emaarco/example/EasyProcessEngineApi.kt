package de.emaarco.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class EasyProcessEngineApi

fun main(args: Array<String>) {
    runApplication<EasyProcessEngineApi>(*args)
}
