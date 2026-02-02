package io.github.alstn113.assignment.infra.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alstn113.assignment.application.exception.FileProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class LocalTempFileStorageTest {

    private final LocalTempFileStorage storage = new LocalTempFileStorage();

    @Test
    @DisplayName("파일을 저장하고 다시 불러올 수 있다")
    void saveAndLoadFile() throws IOException {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test content".getBytes());

        // when
        String path = storage.save(mockFile);
        File loaded = storage.load(path);

        // then
        assertThat(loaded).exists();
        assertThat(Files.readAllBytes(loaded.toPath())).isEqualTo("test content".getBytes());

        // cleanup
        storage.delete(path);
    }

    @Test
    @DisplayName("파일 삭제 후 불러오기를 시도하면 예외가 발생한다")
    void deleteAndLoadFile() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "test".getBytes());
        String path = storage.save(mockFile);

        // when
        storage.delete(path);

        // then
        assertThatThrownBy(() -> storage.load(path))
                .isInstanceOf(FileProcessingException.class);
    }

    @Test
    @DisplayName("존재하지 않는 경로를 로드하면 예외가 발생한다")
    void loadNonExistentFile() {
        assertThatThrownBy(() -> storage.load("/non/existent/path"))
                .isInstanceOf(FileProcessingException.class);
    }
}
