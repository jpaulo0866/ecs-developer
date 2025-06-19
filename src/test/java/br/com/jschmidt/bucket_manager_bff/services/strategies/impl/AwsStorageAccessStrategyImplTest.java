package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsStorageAccessStrategyImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private AwsStorageAccessStrategyImpl awsStorageAccessStrategy;

    @Test
    void getCloudProviderType() {
        assertEquals(CloudProviderEnum.AWS, awsStorageAccessStrategy.getCloudProviderType());
    }

    @Test
    void getFilesFromBucket() {
        String bucketName = "test-bucket";
        S3Object s3Object = S3Object.builder()
                .key("test-file.txt")
                .lastModified(Instant.now())
                .size(123L)
                .eTag("test-etag")
                .build();
        ListObjectsV2Response response = ListObjectsV2Response.builder().contents(s3Object).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<BucketFileModel> files = awsStorageAccessStrategy.getFilesFromBucket(bucketName);

        assertFalse(files.isEmpty());
        assertEquals(1, files.size());
        assertEquals("test-file.txt", files.getFirst().getFileName());
        assertEquals(123L, files.getFirst().getFileSize());
        assertEquals("test-etag", files.getFirst().getEtag());
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void downloadFile() throws IOException {
        String bucketName = "test-bucket";
        String fileName = "test-file.txt";
        byte[] content = "file content".getBytes();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().contentType(MediaType.TEXT_PLAIN_VALUE).build();
        ResponseInputStream<GetObjectResponse> s3ObjectStream = new ResponseInputStream<>(getObjectResponse, new java.io.ByteArrayInputStream(content));

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3ObjectStream);

        ResourceDownloadModel result = awsStorageAccessStrategy.downloadFile(bucketName, fileName);

        assertNotNull(result);
        assertEquals(fileName, result.getFileName());
        assertEquals(MediaType.TEXT_PLAIN_VALUE, result.getContentType());
        assertArrayEquals(content, ((ByteArrayResource) result.getResource()).getByteArray());
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    void uploadFile() throws IOException {
        String bucketName = "test-bucket";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        awsStorageAccessStrategy.uploadFile(file, bucketName);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}