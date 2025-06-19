package br.com.jschmidt.bucket_manager_bff.exceptions;

import java.io.Serial;

public class FileNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileNotFoundException(String fileName) {
        super("File not found: " + fileName);
    }

    public FileNotFoundException(String fileName, Throwable cause) {
        super("File not found: " + fileName, cause);
    }
}
