package br.com.jschmidt.bucket_manager_bff.services;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import br.com.jschmidt.bucket_manager_bff.services.strategies.StorageAccessStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageAccessStrategy awsStrategy;

    @Mock
    private StorageAccessStrategy gcpStrategy;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        when(awsStrategy.getCloudProviderType()).thenReturn(CloudProviderEnum.AWS);
        when(gcpStrategy.getCloudProviderType()).thenReturn(CloudProviderEnum.GCP);
        storageService = new StorageService(Set.of(awsStrategy, gcpStrategy));
    }

    @Test
    void getFilesFromBucket() {
        String bucketName = "test-bucket";
        List<BucketFileModel> expectedFiles = Collections.singletonList(BucketFileModel.builder().build());
        when(awsStrategy.getFilesFromBucket(bucketName)).thenReturn(expectedFiles);

        List<BucketFileModel> actualFiles = storageService.getFilesFromBucket(CloudProviderEnum.AWS, bucketName);

        assertEquals(expectedFiles, actualFiles);
        verify(awsStrategy).getFilesFromBucket(bucketName);
        verify(gcpStrategy, never()).getFilesFromBucket(anyString());
    }

    @Test
    void downloadFile() {
        String bucketName = "test-bucket";
        String fileName = "test-file.txt";
        ResourceDownloadModel expectedModel = ResourceDownloadModel.builder().build();
        when(gcpStrategy.downloadFile(bucketName, fileName)).thenReturn(expectedModel);

        ResourceDownloadModel actualModel = storageService.downloadFile(CloudProviderEnum.GCP, bucketName, fileName);

        assertEquals(expectedModel, actualModel);
        verify(gcpStrategy).downloadFile(bucketName, fileName);
        verify(awsStrategy, never()).downloadFile(anyString(), anyString());
    }

    @Test
    void generatePresignedUrl() {
        String bucketName = "test-bucket";
        String fileName = "test-file.txt";
        String expectedUrl = "http://presigned.url";
        when(awsStrategy.generatePresignedUrl(bucketName, fileName)).thenReturn(expectedUrl);

        String actualUrl = storageService.generatePresignedUrl(CloudProviderEnum.AWS, bucketName, fileName);

        assertEquals(expectedUrl, actualUrl);
        verify(awsStrategy).generatePresignedUrl(bucketName, fileName);
        verify(gcpStrategy, never()).generatePresignedUrl(anyString(), anyString());
    }

    @Test
    void uploadFile() {
        String bucketName = "test-bucket";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        storageService.uploadFile(CloudProviderEnum.GCP, bucketName, file);

        verify(gcpStrategy).uploadFile(file, bucketName);
        verify(awsStrategy, never()).uploadFile(any(), anyString());
    }

    @Test
    void getStrategyInstance_whenProviderNotConfigured_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            storageService.getFilesFromBucket(CloudProviderEnum.AZURE, "any-bucket");
        });

        assertEquals("Cloud Provider AZURE not configured, review the README informations!", exception.getMessage());
    }
}