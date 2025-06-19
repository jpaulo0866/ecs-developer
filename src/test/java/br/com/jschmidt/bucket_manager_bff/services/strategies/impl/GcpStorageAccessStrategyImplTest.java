package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.exceptions.BucketNotFoundException;
import br.com.jschmidt.bucket_manager_bff.exceptions.FileNotFoundException;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GcpStorageAccessStrategyImplTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private GcpStorageAccessStrategyImpl gcpStrategy;

    @Test
    void getCloudProviderType() {
        assertEquals(CloudProviderEnum.GCP, gcpStrategy.getCloudProviderType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getFilesFromBucket_Success() {
        String bucketName = "test-bucket";
        Bucket bucket = mock(Bucket.class);
        Page<Blob> blobPage = mock(Page.class);
        Blob blob = mock(Blob.class);

        when(storage.get(bucketName)).thenReturn(bucket);
        when(bucket.list()).thenReturn(blobPage);
        when(blobPage.iterateAll()).thenReturn(Collections.singletonList(blob));
        when(blob.getName()).thenReturn("test.txt");
        when(blob.getUpdateTime()).thenReturn(System.currentTimeMillis());
        when(blob.getSize()).thenReturn(123L);
        when(blob.getEtag()).thenReturn("etag");

        List<BucketFileModel> files = gcpStrategy.getFilesFromBucket(bucketName);

        assertFalse(files.isEmpty());
        assertEquals("test.txt", files.getFirst().getFileName());
    }

    @Test
    void getFilesFromBucket_WhenBucketNotFound_ThrowsException() {
        when(storage.get("non-existent")).thenReturn(null);
        assertThrows(BucketNotFoundException.class, () -> gcpStrategy.getFilesFromBucket("non-existent"));
    }

    @Test
    void generatePresignedUrl_Success() throws Exception {
        String bucketName = "test-bucket";
        String fileName = "test.txt";
        URL expectedUrl = new URL("http://storage.googleapis.com/test-bucket/test.txt?signed");

        when(storage.signUrl(any(BlobInfo.class), eq(15L), eq(TimeUnit.MINUTES), any(Storage.SignUrlOption.class)))
                .thenReturn(expectedUrl);

        String actualUrl = gcpStrategy.generatePresignedUrl(bucketName, fileName);

        assertEquals(expectedUrl.toString(), actualUrl);
    }

    @Test
    void downloadFile_Success() throws IOException {
        String bucketName = "test-bucket";
        String fileName = "test.txt";
        byte[] content = "data".getBytes();
        Blob blob = mock(Blob.class);

        when(storage.get(any(BlobId.class))).thenReturn(blob);
        when(blob.getContent()).thenReturn(content);
        when(blob.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);

        ResourceDownloadModel model = gcpStrategy.downloadFile(bucketName, fileName);

        assertEquals(fileName, model.getFileName());
        assertEquals(MediaType.TEXT_PLAIN_VALUE, model.getContentType());
        assertArrayEquals(content, ((ByteArrayResource) model.getResource()).getByteArray());
    }

    @Test
    void downloadFile_WhenFileNotFound_ThrowsException() {
        when(storage.get(any(BlobId.class))).thenReturn(null);
        assertThrows(FileNotFoundException.class, () -> gcpStrategy.downloadFile("bucket", "not-found.txt"));
    }

    @Test
    void uploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        String bucketName = "test-bucket";
        byte[] bytes = file.getBytes();

        gcpStrategy.uploadFile(file, bucketName);

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, file.getOriginalFilename()))
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, bytes);
    }

    @Test
    void uploadFile_WhenFileNameIsNull_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", null, "text/plain", "content".getBytes());
        assertThrows(IllegalArgumentException.class, () -> gcpStrategy.uploadFile(file, "bucket"));
    }
}