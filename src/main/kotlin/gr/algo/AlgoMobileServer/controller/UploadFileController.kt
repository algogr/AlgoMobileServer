package gr.algo.AlgoMobileServer.controller

import gr.algo.AlgoMobileServer.filestorage.FileStorage
import gr.algo.AlgoMobileServer.service.CommunicationService


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile;

@RestController
class UploadFileController {

    @Autowired
    lateinit var fileStorage: FileStorage
    @Autowired
    lateinit var cs: CommunicationService

    @GetMapping("/")
    fun index(): String {
        return "multipartfile/uploadform.html"
    }

    @PostMapping("/upload")
    fun uploadMultipartFile(@RequestParam("uploadfile") file: MultipartFile, model: Model): String {
        fileStorage.deleteFile(file.originalFilename!!)
        fileStorage.deleteFile("algo.sqlite.LATEST")

        fileStorage.store(file)
        fileStorage.backupFile(file.originalFilename!!)
        fileStorage.copyLatest(file.originalFilename!!)

        model.addAttribute("message", "File uploaded successfully! -> filename = " + file.getOriginalFilename())

        //cs.AndroidtoAtlantis()
        //cs.AtlantistoAndroid()
        cs.CapitaltoAndroid()


        //model.addAttribute("message", "File uploaded successfully! -> filename = " + file.getOriginalFilename())
        fileStorage.latestToOriginal(file.originalFilename!!)
        return "H ενημέρωση ήταν επιτυχής. Μπορείτε τώρα να κάνετε download"
    }

    }
