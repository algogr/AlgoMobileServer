package gr.algo.AlgoMobileServer.filestorage

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File
import java.time.LocalDateTime

@Service
class FileStorageImpl: FileStorage{

    val log = LoggerFactory.getLogger(this::class.java)
    val rootLocation = Paths.get("filestorage")


    override fun store(file: MultipartFile){
        Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()))
    }

    override fun loadFile(filename: String): Resource{
        println(rootLocation.toString())
        val file = rootLocation.resolve(filename)
        val resource = UrlResource(file.toUri())

        if(resource.exists() || resource.isReadable()) {
            return resource
        }else{
            throw RuntimeException("FAIL!")
        }
    }

    override fun deleteAll(){
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    override fun backupFile(filename: String) {
        val file= File(rootLocation.resolve(filename).toString())
        file.copyTo(File(rootLocation.resolve(filename).toString()+"."+ LocalDateTime.now().toString()))
    }

    override fun copyLatest(filename: String) {
        val file= File(rootLocation.resolve(filename).toString())
        file.copyTo(File(rootLocation.resolve(filename).toString()+".LATEST"))
    }

    override fun latestToOriginal(filename: String) {
        val file= File(rootLocation.resolve(filename).toString())
        file.delete()
        val file1= File(rootLocation.resolve(filename).toString()+".LATEST")
        file1.copyTo(File(rootLocation.resolve(filename).toString()))
        file1.delete()
    }

    override fun deleteFile(filename: String) {
        val file= File(rootLocation.resolve(filename).toString())
        file.delete()
    }

    override fun init(){
        Files.createDirectory(rootLocation)
    }

    override fun loadFiles(): Stream<Path>{
        return Files.walk(this.rootLocation, 1)
                .filter{path -> !path.equals(this.rootLocation)}
                .map(this.rootLocation::relativize)
    }
}