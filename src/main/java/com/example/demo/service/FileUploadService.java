package com.example.demo.service;

import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {
    private static final String FILE_SERVER = "https://fileupload.rick-and-friends.site/";
    
    @Autowired
    private ProductRepository productRepository;
    
    private final RestTemplate restTemplate;
    
    public FileUploadService() {
        this.restTemplate = new RestTemplate();
    }

    public void upload(MultipartFile file, Long productId) {
//       curl --location 'https://fileupload.rick-and-friends.site/' \
//       --form 'file=@"/Users/kl68884/Downloads/redis-stack-server-7.2.0-v10/bin/redis-server"'

    }
}
