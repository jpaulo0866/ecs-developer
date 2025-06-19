package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.exceptions.BucketNotFoundException;
import br.com.jschmidt.bucket_manager_bff.exceptions.FileNotFoundException;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import br.com.jschmidt.bucket_manager_bff.services.strategies.StorageAccessStrategy;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnBooleanProperty(name = "cloud.azure.enabled", havingValue = true, matchIfMissing = false)
public class AzureStorageAccessStrategyImpl implements StorageAccessStrategy {

    private final BlobServiceClient blobServiceClient;

    public AzureStorageAccessStrategyImpl(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public CloudProviderEnum getCloudProviderType() {
        return CloudProviderEnum.AZURE;
    }

    @Override
    public List<BucketFileModel> getFilesFromBucket(String bucketName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);

        if (!containerClient.exists()) {
            throw new BucketNotFoundException(bucketName);
        }

        List<BucketFileModel> files = new ArrayList<>();
        for (BlobItem it : containerClient.listBlobs()) {
            files.add(
                    BucketFileModel.builder()
                            .fileName(it.getName())
                            .lastModified(Optional.ofNullable(it.getProperties().getLastModified())
                                    .map(OffsetDateTime::toInstant)
                                    .orElse(null)
                            )
                            .fileSize(it.getProperties().getContentLength())
                            .cloudProvider(getCloudProviderType())
                            .bucketName(bucketName)
                            .etag(it.getProperties().getETag())
                            .build()
            );
        }

        return files;
    }

    @Override
    public String generatePresignedUrl(String bucketName, String fileName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        if (!blobClient.exists()) {
            throw new FileNotFoundException(fileName);
        }

        BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(15);
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission);
        String sasToken = blobClient.generateSas(sasSignatureValues);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    @Override
    public ResourceDownloadModel downloadFile(String bucketName, String fileName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        if (!blobClient.exists()) {
            throw new FileNotFoundException(fileName);
        }

        BlobProperties properties = blobClient.getProperties();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        byte[] content = outputStream.toByteArray();

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResourceDownloadModel.builder()
                .resource(resource)
                .fileName(fileName)
                .contentType(Optional.ofNullable(properties.getContentType())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .build();
    }

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String bucketName) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);

        if (!containerClient.exists()) {
            throw new BucketNotFoundException(bucketName);
        }

        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        if (file.getContentType() != null) {
            blobClient.setHttpHeaders(new BlobHttpHeaders()
                    .setContentType(file.getContentType()));
        }
    }
}
