package br.com.jschmidt.bucket_manager_bff.services.strategies;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageAccessStrategy {

    CloudProviderEnum getCloudProviderType();
    List<BucketFileModel> getFilesFromBucket(String bucketName);
    String generatePresignedUrl(String bucketName, String fileName);
    ResourceDownloadModel downloadFile(String bucketName, String fileName);
    void uploadFile(MultipartFile file, String bucketName);
}
