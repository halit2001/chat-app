package com.chatapp.server_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "media-service", path = "/api/media")
public interface MediaServiceClient {

    @PostMapping(value = "upload-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> uploadIcon(@RequestPart("file") MultipartFile file);

    @PutMapping(value = "update-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> updateIcon(@RequestPart("file") MultipartFile file,
                                   @RequestParam("publicId") String publicId,
                                   @RequestParam(value = "invalidate", defaultValue = "true") boolean invalidate);
}
