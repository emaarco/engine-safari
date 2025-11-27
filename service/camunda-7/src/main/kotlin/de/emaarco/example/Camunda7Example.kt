package de.emaarco.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class Camunda7Example

fun main(args: Array<String>) {
    runApplication<Camunda7Example>(*args)
}
