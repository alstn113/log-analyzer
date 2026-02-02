package io.github.alstn113.assignment.application;

import java.io.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    File save(MultipartFile file);

    void delete(File file);
}
