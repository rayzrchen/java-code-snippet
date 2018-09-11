package com.example.backuph2.demo;

import com.example.backuph2.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ListFileController {

    @Value("${app_data}")
    String appData;

    @GetMapping("/api/list-files")
    public List<Path> listFiles(@RequestParam String filePath) {

        // GET http://localhost:8080/list-files?filePath=d%3A%2Ftemp
        try {
            return Files.list(Paths.get(filePath)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new NotFoundException("record not found");
        }

    }


    @GetMapping(value = "/api/download")
    public ResponseEntity<?> backup(@RequestParam String filePath) throws Exception {

        // GET http://localhost:8080/download?filePath=d%3A%5Ctemp%5C2.txt
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + new File(filePath).getName())
                .body(bytes);
    }

    @PostMapping("/api/upload")
    public List<Path> uploadFiles(@RequestParam MultipartFile file) throws IOException {

        // note: please do not specify the content-type in the request header to avoid exception.

        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetPath = Paths.get(appData).resolve(fileName);

        // Copy file to the target location (Replacing existing file with the same name)
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return listFiles(appData);
    }


}
