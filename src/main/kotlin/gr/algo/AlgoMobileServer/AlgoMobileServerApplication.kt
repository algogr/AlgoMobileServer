package gr.algo.AlgoMobileServer

import gr.algo.AlgoMobileServer.filestorage.FileStorage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication

import org.springframework.context.annotation.Bean



@SpringBootApplication
class AlgoMobileServerApplication{
	@Autowired
	lateinit var fileStorage: FileStorage


	@Bean
	fun run() = CommandLineRunner {
		//fileStorage.deleteAll()
		//fileStorage.init()
	}
}

fun main(args: Array<String>) {
	//runApplication<AlgoMobileServerApplication>(*args)
	SpringApplication.run(AlgoMobileServerApplication::class.java, *args)

}
