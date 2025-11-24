package com.chat_app.channel_service.config;

import com.chat_app.common_library.exceptions.AccessDeniedException;
import com.chat_app.common_library.exceptions.ResourceNotFoundException;
import com.chat_app.common_library.exceptions.ServiceCommunicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;

import java.io.IOException;
import java.util.Map;

public class CustomFeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultErrorDecoder = new Default();

    public CustomFeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String errorMessage = "Something goes wrong";
        String responseBody = null;
        try {
            if (response.body() != null) {
                responseBody = new String(response.body().asInputStream().readAllBytes());
            }
        } catch (IOException e) {
            System.err.println("Error reading Feign response body: " + e.getMessage());
        }

        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                // Hata yanıtının yapısına göre Map.class veya özel bir ErrorResponse DTO kullanabilirsin
                Map<String, String> errorMap = objectMapper.readValue(responseBody, Map.class);
                if (errorMap.containsKey("message")) {
                    errorMessage = errorMap.get("message");
                } else if (errorMap.containsKey("error")) {
                    errorMessage = errorMap.get("error");
                }
            } catch (IOException e) {
                // JSON parse edilemezse, tüm body'yi mesaj olarak kullan
                errorMessage = responseBody;
            }
        }
        switch (status) {
            case 404: // Kaynak bulunamadı (örn. ServerNotFoundException)
                return new ResourceNotFoundException(errorMessage);
            case 403: // Yetkilendirme hatası (örn. AccessDeniedException veya ServerAccessDeniedException)
                return new AccessDeniedException(errorMessage); // Veya new AccessDeniedException(errorMessage);
            case 400: // Bad Request (eğer server-service'ten 400 geliyorsa, bunu da özelleyebiliriz)
                return new BadRequestException(errorMessage);
            case 500: // Internal Server Error - Veritabanı hataları genellikle buraya düşer
                return new ServiceCommunicationException("Internal error in downstream service: " + errorMessage);
            default:
                // Tanınmayan veya işlenmeyen diğer HTTP durum kodları için Feign'in varsayılan işleyicisine düş
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
