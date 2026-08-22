package dev.ja.fammecatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FammeProductCatalogApplication

fun main(args: Array<String>) {
	runApplication<FammeProductCatalogApplication>(*args)
}
