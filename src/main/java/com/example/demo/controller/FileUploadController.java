package com.example.demo.controller;

import com.example.demo.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/product/{productId}")
    public void upload(@RequestBody MultipartFile file, @PathVariable Long productId){
        fileUploadService.upload(file, productId);
    }

}
