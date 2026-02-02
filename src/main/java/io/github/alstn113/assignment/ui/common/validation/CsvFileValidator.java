package io.github.alstn113.assignment.ui.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class CsvFileValidator implements ConstraintValidator<ValidCsv, MultipartFile> {

    private static final String CSV_EXTENSION = ".csv";
    private static final String CSV_CONTENT_TYPE = "text/csv";

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(CSV_EXTENSION)) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType == null
                || contentType.equalsIgnoreCase(CSV_CONTENT_TYPE)
                || contentType.equalsIgnoreCase("application/vnd.ms-excel");
    }
}
