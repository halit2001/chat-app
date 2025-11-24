package com.chat_app.media_service.controller;

import com.chat_app.media_service.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public MediaController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/upload-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadIcon(@RequestPart("file") MultipartFile file) {
        try {
            String iconUrl = cloudinaryService.uploadFile(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("iconUrl", iconUrl));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", exception.getMessage()));
        }
    }

    @PutMapping(value = "update-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateIcon(@RequestPart("file") MultipartFile file,
                                                          @RequestParam("publicId") String publicId,
                                                          @RequestParam(value = "invalidate", defaultValue = "true") boolean invalidate) {
        try {
            String iconUrl = cloudinaryService.uploadFile(file, publicId, invalidate);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("iconUrl", iconUrl));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", exception.getMessage()));
        }
    }

}
