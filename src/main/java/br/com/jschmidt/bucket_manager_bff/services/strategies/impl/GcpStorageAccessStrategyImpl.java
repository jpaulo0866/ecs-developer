package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.exceptions.BucketNotFoundException;
import br.com.jschmidt.bucket_manager_bff.exceptions.FileNotFoundException;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import br.com.jschmidt.bucket_manager_bff.services.strategies.StorageAccessStrategy;
import com.google.cloud.storage.*;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnBooleanProperty(name = "cloud.gcp.enabled", havingValue = true, matchIfMissing = false)
public class GcpStorageAccessStrategyImpl implements StorageAccessStrategy {

    private final Storage storage;

    public GcpStorageAccessStrategyImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CloudProviderEnum getCloudProviderType() {
        return CloudProviderEnum.GCP;
    }

    @Override
    public List<BucketFileModel> getFilesFromBucket(String bucketName) {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }

        List<BucketFileModel> files = new ArrayList<>();
        bucket.list().iterateAll().forEach(blob -> {
            BucketFileModel fileModel = BucketFileModel.builder()
                    .fileName(blob.getName())
                    .lastModified(Instant.ofEpochMilli(blob.getUpdateTime()))
                    .fileSize(blob.getSize())
                    .cloudProvider(getCloudProviderType())
                    .bucketName(bucketName)
                    .etag(blob.getEtag())
                    .build();
            files.add(fileModel);
        });

        return files;
    }

    @Override
    public String generatePresignedUrl(String bucketName, String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        return storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET)
        ).toString();
    }

    @Override
    public ResourceDownloadModel downloadFile(String bucketName, String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new FileNotFoundException(fileName);
        }

        byte[] content = blob.getContent();
        ByteArrayResource resource = new ByteArrayResource(content);

        return ResourceDownloadModel.builder()
                .resource(resource)
                .contentType(Optional.ofNullable(blob.getContentType())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .fileName(fileName)
                .build();
    }

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String bucketName) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());
    }
}
