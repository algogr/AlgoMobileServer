package gr.algo.AlgoMobileServer.controller

import gr.algo.AlgoMobileServer.context
import gr.algo.AlgoMobileServer.filestorage.FileStorage
import gr.algo.AlgoMobileServer.service.CommunicationService
import gr.algo.AlgoMobileServer.service.CommunicationServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
//import gr.algo.AlgoMobileServer.config.DBConfig
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment


@Controller
class DownloadFileController {

    @Autowired
    lateinit var fileStorage: FileStorage

    @Autowired
    lateinit var commService:CommunicationServiceImpl



    @GetMapping("/files/{filename}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<Resource> {
        println(filename)
        val file = fileStorage.loadFile(filename)
        //commService.AndroidtoAtlantis()

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}