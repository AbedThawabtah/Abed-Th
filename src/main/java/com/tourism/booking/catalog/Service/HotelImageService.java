package com.tourism.booking.catalog.Service;

import com.tourism.booking.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class HotelImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Path uploadRoot;
    private final long maxBytes;

    public HotelImageService(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.max-image-bytes:3145728}") long maxImageBytes) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxBytes = maxImageBytes;
    }

    /**
     * Stores image file on disk and returns a relative DB path like: hotels/{hotelId}/{fileName}.
     */
    public String storeHotelImage(Long hotelId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("Image is required");
        }
        if (image.getSize() > maxBytes) {
            throw new BusinessException("Image is too large. Max bytes: " + maxBytes);
        }
        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("Invalid image type. Allowed: jpeg, png, webp, gif");
        }

        String ext = extensionFor(contentType);
        String fileName = UUID.randomUUID() + ext;
        Path hotelDir = uploadRoot.resolve("hotels").resolve(String.valueOf(hotelId)).normalize();
        if (!hotelDir.startsWith(uploadRoot)) {
            throw new BusinessException("Invalid upload path");
        }

        try {
            Files.createDirectories(hotelDir);
            Path target = hotelDir.resolve(fileName).normalize();
            if (!target.startsWith(hotelDir)) {
                throw new BusinessException("Invalid file path");
            }
            image.transferTo(target);
        } catch (IOException ex) {
            log.error("Failed to store hotel image | hotelId={}", hotelId, ex);
            throw new BusinessException("Failed to store image");
        }

        return Paths.get("hotels", String.valueOf(hotelId), fileName).toString().replace('\\', '/');
    }

    public Resource loadAsResource(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        Path path = uploadRoot.resolve(relativePath).normalize();
        if (!path.startsWith(uploadRoot)) {
            throw new BusinessException("Invalid image path");
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (MalformedURLException ex) {
            throw new BusinessException("Invalid image path", ex);
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}

