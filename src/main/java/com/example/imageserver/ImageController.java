package com.example.imageserver;

import com.azure.storage.blob.BlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/images")
class ImageController {

    private final AzureBlobStorageService storageService;
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Value("${azure.storage.container-name}")
    private String containerName;

    public ImageController(AzureBlobStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("request - uploadImage | (URL - /upload ) | file = {}", file);
        try {
            String containerName = "images";
            String blobName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            storageService.uploadImage(containerName, blobName, file.getInputStream());
            logger.info("response - uploadImage | (URL - /upload ) | res = {}", "Image uploaded successfully!");
            return "Image uploaded successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("response - uploadImage | (URL - /upload ) | res = {}", "Image upload failed.");
            return "Image upload failed.";
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllImages() {
        List<String> imageUrls = storageService.getAllImageUrls(containerName);
        logger.info("response - getAllImages | (URL - /all ) | res = {}", imageUrls);
        return ResponseEntity.ok().body(imageUrls);
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        BlobClient blobClient = storageService.getImage(containerName, imageName);
        try {
            InputStream imageStream = blobClient.openQueryInputStream(imageName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(imageStream));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{imageName}")
    public ResponseEntity<String> deleteImage(@PathVariable String imageName) {
        try {
            storageService.deleteImage(containerName, imageName);
            return ResponseEntity.ok("Image deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image deletion failed.");
        }
    }

    @DeleteMapping("/delete-multiple")
    public ResponseEntity<String> deleteImages(@RequestParam("imageNames") List<String> imageNames) {
        try {
            storageService.deleteImages(containerName, imageNames);
            return ResponseEntity.ok("Images deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image deletion failed.");
        }
    }

}