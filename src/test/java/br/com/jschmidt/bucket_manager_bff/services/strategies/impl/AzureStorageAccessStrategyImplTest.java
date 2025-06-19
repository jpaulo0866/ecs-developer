package br.com.jschmidt.bucket_manager_bff.services.strategies.impl;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.exceptions.BucketNotFoundException;
import br.com.jschmidt.bucket_manager_bff.exceptions.FileNotFoundException;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.models.ResourceDownloadModel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureStorageAccessStrategyImplTest {

    @Mock
    private BlobServiceClient blobServiceClient;
    @Mock
    private BlobContainerClient blobContainerClient;
    @Mock
    private BlobClient blobClient;
    @Mock
    private PagedIterable<BlobItem> pagedIterable;

    @InjectMocks
    private AzureStorageAccessStrategyImpl azureStrategy;

    @BeforeEach
    void setUp() {
        lenient().when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
    }

    @Test
    void getCloudProviderType() {
        assertEquals(CloudProviderEnum.AZURE, azureStrategy.getCloudProviderType());
    }

    @Test
    void getFilesFromBucket_Success() {
        String bucketName = "test-container";
        BlobItem blobItem = mock(BlobItem.class);
        BlobItemProperties properties = mock(BlobItemProperties.class);

        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);
        when(pagedIterable.iterator()).thenReturn(Collections.singletonList(blobItem).iterator());
        when(blobItem.getName()).thenReturn("test.txt");
        when(blobItem.getProperties()).thenReturn(properties);
        when(properties.getLastModified()).thenReturn(OffsetDateTime.now());
        when(properties.getContentLength()).thenReturn(123L);
        when(properties.getETag()).thenReturn("etag");

        List<BucketFileModel> files = azureStrategy.getFilesFromBucket(bucketName);

        assertFalse(files.isEmpty());
        assertEquals("test.txt", files.getFirst().getFileName());
    }

    @Test
    void getFilesFromBucket_WhenBucketNotFound_ThrowsException() {
        when(blobContainerClient.exists()).thenReturn(false);
        assertThrows(BucketNotFoundException.class, () -> azureStrategy.getFilesFromBucket("non-existent"));
    }

    @Test
    void generatePresignedUrl_Success() {
        when(blobContainerClient.getBlobClient("test.txt")).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.getBlobUrl()).thenReturn("http://storage.blob.core.windows.net/container/test.txt");
        when(blobClient.generateSas(any())).thenReturn("sas-token");

        String url = azureStrategy.generatePresignedUrl("container", "test.txt");

        assertEquals("http://storage.blob.core.windows.net/container/test.txt?sas-token", url);
    }

    @Test
    void generatePresignedUrl_WhenFileNotFound_ThrowsException() {
        when(blobContainerClient.getBlobClient("not-found.txt")).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);
        assertThrows(FileNotFoundException.class, () -> azureStrategy.generatePresignedUrl("container", "not-found.txt"));
    }

    @Test
    void downloadFile_Success() throws IOException {
        String fileName = "test.txt";
        byte[] content = "data".getBytes();
        BlobProperties properties = mock(BlobProperties.class);

        when(blobContainerClient.getBlobClient(fileName)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.getProperties()).thenReturn(properties);
        when(properties.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
        doAnswer(invocation -> {
            ByteArrayOutputStream stream = invocation.getArgument(0);
            stream.write(content);
            return null;
        }).when(blobClient).downloadStream(any(ByteArrayOutputStream.class));

        ResourceDownloadModel model = azureStrategy.downloadFile("container", fileName);

        assertEquals(fileName, model.getFileName());
        assertEquals(MediaType.TEXT_PLAIN_VALUE, model.getContentType());
        assertArrayEquals(content, ((ByteArrayResource) model.getResource()).getByteArray());
    }

    @Test
    void downloadFile_WhenFileNotFound_ThrowsException() {
        when(blobContainerClient.getBlobClient("not-found.txt")).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);
        assertThrows(FileNotFoundException.class, () -> azureStrategy.downloadFile("container", "not-found.txt"));
    }

    @Test
    void uploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.getBlobClient("test.txt")).thenReturn(blobClient);

        azureStrategy.uploadFile(file, "container");

        verify(blobClient).upload(any(InputStream.class), eq(file.getSize()), eq(true));
        verify(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void uploadFile_WhenBucketNotFound_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        when(blobContainerClient.exists()).thenReturn(false);
        assertThrows(BucketNotFoundException.class, () -> azureStrategy.uploadFile(file, "non-existent"));
    }

    @Test
    void uploadFile_WhenFileNameIsNull_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", null, "text/plain", "content".getBytes());
        assertThrows(IllegalArgumentException.class, () -> azureStrategy.uploadFile(file, "container"));
    }
}