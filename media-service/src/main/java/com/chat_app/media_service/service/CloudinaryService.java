package com.chat_app.media_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public String uploadFile(MultipartFile file, String publicId, boolean invalidate) throws IOException{
        if (publicId == null || publicId.trim().isEmpty()) throw new IllegalArgumentException("Public ID cannot be null or empty when explicitly uploading with a public ID.");
        Map<String, Object> options = new HashMap<>();
        options.put("public_id", publicId);
        options.put("overwrite", true);
        options.put("invalidate", invalidate);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return uploadResult.get("secure_url").toString();
    }
}
