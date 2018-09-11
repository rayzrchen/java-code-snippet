package com.example.backuph2.demo;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController

public class BackupController {


    private final DataSource dataSource;

    @Autowired
    public BackupController(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @GetMapping(value = "/backup")
    public ResponseEntity<?> backup(HttpServletResponse resp) throws Exception {

        StringBuilder sb = new StringBuilder();

        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("script schema PUBLIC");
            while (resultSet.next()) {
                sb.append(resultSet.getString(1));
                sb.append("\n");
            }
        }

        byte[] generateZip = generateZip(sb.toString().getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=backup" +
                                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".zip")
                .contentLength(generateZip.length)
                .body(generateZip);

    }


    private byte[] generateZip(byte[] bytes) throws IOException, ZipException {

        ZipParameters zipParams = new ZipParameters();
        zipParams.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParams.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipParams.setEncryptFiles(true);
        zipParams.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
        zipParams.setPassword("123");
        zipParams.setSourceExternalStream(true);
        zipParams.setFileNameInZip(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_sql.txt");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        net.lingala.zip4j.io.ZipOutputStream zout = new net.lingala.zip4j.io.ZipOutputStream(byteArrayOutputStream);
        zout.putNextEntry(
                new File(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_sql.txt"),
                zipParams
        );
        zout.write(bytes);

        zout.closeEntry();
        zout.finish();

        return byteArrayOutputStream.toByteArray();

    }

}
