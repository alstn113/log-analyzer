package io.github.alstn113.assignment.infra.file;

import io.github.alstn113.assignment.application.FileStorage;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class LocalTempFileStorage implements FileStorage {

    @Override
    public String save(MultipartFile file) {
        try {
            String tempFileName = "upload-%s".formatted(file.getOriginalFilename());
            File tempFile = File.createTempFile(tempFileName, null);
            file.transferTo(tempFile);

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new FileProcessingException("파일을 저장하는데 실패했습니다", e);
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
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            File file = new File(key);
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", key, e);
        }
    }
}
