package com.dong.springboot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    // 鉁?淇杩欓噷锛侊紒锛?
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uuidFileName = UUID.randomUUID() + suffix;

            String dirPath = System.getProperty("user.dir") + File.separator + "upload";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, uuidFileName);
            file.transferTo(dest);

            return "/upload/" + uuidFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "涓婁紶澶辫触";
        }
    }
}