package com.ecommerce.Project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        //FILE NAME OF CURRENT FILE/ORIGINAL FILE
        String originalFileName = file.getOriginalFilename();


        //GENERATE  A UNIQUE FILE NAME
        String randomId = UUID.randomUUID().toString() ;
        // mat.jpg --> 1234 --> 1234.jpg
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName ;


        // CHECK IF PATH EXIST AND CREATE
        File folder = new File(path);
        if(!folder.exists())
            folder.mkdir();


        //UPLOAD TO SERVER

        Files.copy(file.getInputStream() , Paths.get(filePath));


        // RETURN THE FILE
        return fileName ;
    }


}

