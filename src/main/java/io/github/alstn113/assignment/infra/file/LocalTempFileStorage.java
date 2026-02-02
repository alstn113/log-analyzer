package io.github.alstn113.assignment.infra.file;

import io.github.alstn113.assignment.application.FileStorage;
import java.io.File;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalTempFileStorage implements FileStorage {

    @Override
    public File save(MultipartFile file) {
        return null;
    }

    @Override
    public void delete(File file) {

    }
}
