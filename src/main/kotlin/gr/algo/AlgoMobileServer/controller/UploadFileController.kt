package gr.algo.AlgoMobileServer.controller

import gr.algo.AlgoMobileServer.AlgoMobileServerApplication
import gr.algo.AlgoMobileServer.context
import gr.algo.AlgoMobileServer.filestorage.FileStorage
import gr.algo.AlgoMobileServer.service.CommunicationService


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse

@RestController
class UploadFileController {

    @Autowired
    lateinit var fileStorage: FileStorage
    @Autowired
    lateinit var cs: CommunicationService

    @Autowired
    lateinit var app: AlgoMobileServerApplication
    /*
    @GetMapping("/")
    fun index(): String {
        return "multipartfile/uploadform.html"
    }
    */


    @PostMapping("/upload")
    fun uploadMultipartFile(@RequestParam("uploadfile") file: MultipartFile, model: Model,response:HttpServletResponse){
        println("1")
        fileStorage.deleteFile(file.originalFilename!!)
        println("2-${file.originalFilename}")
        fileStorage.deleteFile("algo.sqlite.LATEST")
        println("3")
        fileStorage.store(file)
        println("4")
        fileStorage.backupFile(file.originalFilename!!)
        println("5-${file.originalFilename}")
        fileStorage.copyLatest(file.originalFilename!!)
        println("6-${file.originalFilename}")

        model.addAttribute("message", "File uploaded successfully! -> filename = " + file.getOriginalFilename())

        val env: Environment = context.environment
        val application:String=env.getProperty("algo.kavoukis.application")!!
        val code=HttpServletResponse.SC_OK
        val wr=response.writer
        response.status=code
        wr.print(response)
        wr.flush()
        wr.close()
        if (application=="capital") {
            cs.AndroidtoCapital()
            cs.CapitaltoAndroid()
        }
        else if (application=="atlantis")
        {
            cs.AndroidtoAtlantis()
            cs.AtlantistoAndroid()

        }
        //fileStorage.deleteFile(file.originalFilename!!)

        //model.addAttribute("message", "File uploaded successfully! -> filename = " + file.getOriginalFilename())
        //fileStorage.latestToOriginal(file.originalFilename!!)
        app.restart()
    }




    }
