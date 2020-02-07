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
import javax.servlet.http.HttpServletResponse
import javax.xml.ws.Response


@Controller
class DownloadFileController {

    @Autowired
    lateinit var fileStorage: FileStorage

    @Autowired
    lateinit var cs:CommunicationServiceImpl


    @GetMapping("/files/{filename}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val env: Environment = context.environment
        val ready:Int=env.getProperty("algo.global.isReadyFile")!!.toInt()
        val application:String=env.getProperty("algo.kavoukis.application")!!

        if (application=="atlantis")
            cs.AtlantistoAndroid()
        else if (application=="capital")
            cs.CapitaltoAndroid()


        if (ready==1)
        {
            val file = fileStorage.loadFile(filename + ".LATEST")
            //commService.AndroidtoAtlantis()


            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename()?.replace(".LATEST", "") + "\"")
                    .body(file);
        }
        else
        {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment: filenama=").body(null)


        }


    }




}