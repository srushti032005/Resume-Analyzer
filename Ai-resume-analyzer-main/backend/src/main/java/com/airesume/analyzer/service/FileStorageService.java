package com.airesume.analyzer.service;

import com.airesume.analyzer.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.storage.location}")
    private String storageLocation;

    @Value("${app.storage.max-file-size-bytes}")
    private long maxFileSizeBytes;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("Resume storage initialised at {}", rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialise storage", e);
        }
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename();
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                || (name != null && name.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            throw new BadRequestException("Only PDF resumes are accepted");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException("File exceeds maximum size of "
                    + (maxFileSizeBytes / (1024 * 1024)) + " MB");
        }
    }

    public String store(MultipartFile file, Long userId) {
        validate(file);
        try {
            String safeName = UUID.randomUUID() + ".pdf";
            Path userDir = rootLocation.resolve("user-" + userId);
            Files.createDirectories(userDir);
            Path destination = userDir.resolve(safeName).normalize();

            if (!destination.startsWith(rootLocation)) {
                throw new BadRequestException("Invalid storage path");
            }
            Files.copy(file.getInputStream(), destination,
                    StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    public byte[] read(String storedPath) {
        try {
            return Files.readAllBytes(Paths.get(storedPath));
        } catch (IOException e) {
            throw new BadRequestException("Stored resume could not be read");
        }
    }

    public void delete(String storedPath) {
        try {
            Files.deleteIfExists(Paths.get(storedPath));
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", storedPath, e.getMessage());
        }
    }
}
