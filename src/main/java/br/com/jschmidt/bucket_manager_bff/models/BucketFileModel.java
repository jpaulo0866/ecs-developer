package br.com.jschmidt.bucket_manager_bff.models;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BucketFileModel {
    private String fileName;
    private Long fileSize;
    private String bucketName;
    private CloudProviderEnum cloudProvider;
    private Instant lastModified;
    private String etag;

}
