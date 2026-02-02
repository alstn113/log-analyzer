package io.github.alstn113.assignment.application;

import java.io.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    /**
     * @param file 업로드된 파일
     * @return 저장된 파일의 식별자 또는 경로
     */
    String save(MultipartFile file);

    File load(String key);

    void delete(String key);
}
