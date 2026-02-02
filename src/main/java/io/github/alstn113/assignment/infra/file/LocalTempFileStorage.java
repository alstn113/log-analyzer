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
    public String save(MultipartFile file) {
        try {
            String tempFileName = "upload-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            File tempFile = File.createTempFile(tempFileName, null);
            file.transferTo(tempFile);

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save temporary file: " + e.getMessage(), e);
        }
    }

    @Override
    public File load(String path) {
        if (path == null || path.isBlank()) {
            throw new FileProcessingException("파일 경로가 비어있습니다.");
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new FileProcessingException("파일을 찾을 수 없습니다: " + path);
        }

        return file;
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
