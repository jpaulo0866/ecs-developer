package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import br.com.jschmidt.bucket_manager_bff.services.strategies.StorageAccessStrategy;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnBooleanProperty(name = "cloud.aws.enabled", havingValue = true, matchIfMissing = false)
public class AwsStorageAccessStrategyImpl implements StorageAccessStrategy {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public AwsStorageAccessStrategyImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public CloudProviderEnum getCloudProviderType() {
        return CloudProviderEnum.AWS;
    }

    @Override
    public List<BucketFileModel> getFilesFromBucket(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
        return s3Client.listObjectsV2(request)
                .contents()
                .stream()
                .map(it -> BucketFileModel.builder()
                        .fileName(it.key())
                        .lastModified(it.lastModified())
                        .fileSize(it.size())
                        .cloudProvider(getCloudProviderType())
                        .bucketName(bucketName)
                        .etag(it.eTag())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String generatePresignedUrl(String bucketName, String fileName) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(req -> req.bucket(bucketName).key(fileName))
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    @SneakyThrows
    public ResourceDownloadModel downloadFile(String bucketName, String fileName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        var s3Object = s3Client.getObject(objectRequest);
        var objectResponse = s3Object.response();

        return ResourceDownloadModel.builder()
                .contentType(Optional.ofNullable(objectResponse.contentType())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .fileName(fileName)
                .resource(new ByteArrayResource(s3Object.readAllBytes()))
                .build();
    }

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file, String bucketName) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
    }
}
