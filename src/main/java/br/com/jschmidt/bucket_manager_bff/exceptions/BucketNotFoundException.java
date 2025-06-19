package br.com.jschmidt.bucket_manager_bff.exceptions;

import java.io.Serial;

public class BucketNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BucketNotFoundException(String bucketName) {
        super("Bucket not found: " + bucketName);
    }

    public BucketNotFoundException(String bucketName, Throwable cause) {
        super("Bucket not found: " + bucketName, cause);
    }
}
