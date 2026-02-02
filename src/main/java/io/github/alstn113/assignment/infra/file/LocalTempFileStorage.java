package io.github.alstn113.assignment.infra.file;

import io.github.alstn113.assignment.application.FileStorage;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class LocalTempFileStorage implements FileStorage {

    @Override
    public File save(MultipartFile file) {
        try {
            String tempFileName = "upload-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            File tempFile = File.createTempFile(tempFileName, null);
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save temporary file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(File file) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.warn("Failed to delete temporary file: {}", file.getAbsolutePath(), e);
        }
    }
}
