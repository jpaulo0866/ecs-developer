package br.com.jschmidt.bucket_manager_bff.services;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import br.com.jschmidt.bucket_manager_bff.services.strategies.StorageAccessStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageService {

    private final Map<CloudProviderEnum, StorageAccessStrategy> storageAccessStrategies;

    public StorageService(Set<StorageAccessStrategy> storageAccessStrategies) {
        this.storageAccessStrategies = storageAccessStrategies.stream()
                .collect(Collectors
                        .toUnmodifiableMap(StorageAccessStrategy::getCloudProviderType, Function.identity()));
    }

    public List<BucketFileModel> getFilesFromBucket(CloudProviderEnum cloudProvider, String bucketName) {
        log.info("Fetching files from bucket: {} for cloud provider: {}", bucketName, cloudProvider);
        return getStrategyInstance(cloudProvider).getFilesFromBucket(bucketName);
    }

    public ResourceDownloadModel downloadFile(CloudProviderEnum cloudProvider, String bucketName, String fileName) {
        log.info("Downloading file: {} from bucket: {} for cloud provider: {}", fileName, bucketName, cloudProvider);
        return getStrategyInstance(cloudProvider).downloadFile(bucketName, fileName);
    }

    public String generatePresignedUrl(CloudProviderEnum cloudProvider, String bucketName, String fileName) {
        log.info("Generating presigned URL for file: {} in bucket: {} for cloud provider: {}",
                fileName, bucketName, cloudProvider);
        return getStrategyInstance(cloudProvider).generatePresignedUrl(bucketName, fileName);
    }

    public void uploadFile(CloudProviderEnum cloudProvider, String bucketName, MultipartFile file) {
        log.info("Uploading file: {} to bucket: {} for cloud provider: {}",
                file.getOriginalFilename(), bucketName, cloudProvider);
        getStrategyInstance(cloudProvider).uploadFile(file, bucketName);
    }

    private StorageAccessStrategy getStrategyInstance(CloudProviderEnum cloudProvider) {
        StorageAccessStrategy storageProvider = storageAccessStrategies.get(cloudProvider);
        if (storageProvider == null) {
            throw new IllegalArgumentException(
                    "Cloud Provider %s not configured, review the README informations!".formatted(cloudProvider));
        }
        return storageProvider;
    }
}
