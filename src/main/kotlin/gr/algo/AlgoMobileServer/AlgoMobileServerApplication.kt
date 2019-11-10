package gr.algo.AlgoMobileServer

import gr.algo.AlgoMobileServer.filestorage.FileStorage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

import org.springframework.context.annotation.Bean



@SpringBootApplication
class AlgoMobileServerApplication{
	@Autowired
	lateinit var fileStorage: FileStorage




	fun restart() {
		val args = context?.getBean(ApplicationArguments::class.java)
		val thread = Thread({
			context?.close()
			context = SpringApplication.run(AlgoMobileServerApplication::class.java, *args?.sourceArgs)
		})
		thread.setDaemon(false)
		thread.start()
	}
}

lateinit var context: ConfigurableApplicationContext



fun main(args: Array<String>) {
	//runApplication<AlgoMobileServerApplication>(*args)
	context=SpringApplication.run(AlgoMobileServerApplication::class.java, *args)

}
