package br.com.jschmidt.bucket_manager_bff.controllers;

import br.com.jschmidt.bucket_manager_bff.enums.CloudProviderEnum;
import br.com.jschmidt.bucket_manager_bff.models.BucketFileModel;
import br.com.jschmidt.bucket_manager_bff.services.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/providers/{provider}/buckets/{bucketName}/files")
    public @ResponseBody List<BucketFileModel> getFilesFromStorage(@PathVariable CloudProviderEnum provider,
                                                     @PathVariable String bucketName) {
        return storageService.getFilesFromBucket(provider, bucketName);
    }

    @GetMapping(value = "/providers/{provider}/buckets/{bucketName}/files/{fileName}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable CloudProviderEnum provider,
                                                 @PathVariable String bucketName,
                                                 @PathVariable String fileName) {
        var resourceDownloadModel = storageService.downloadFile(provider, bucketName, fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resourceDownloadModel.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resourceDownloadModel.getContentType()))
                .body(resourceDownloadModel.getResource());
    }

    @PutMapping(value = "/providers/{provider}/buckets/{bucketName}/upload")
    public void uploadFile(@PathVariable CloudProviderEnum provider,
                           @PathVariable String bucketName,
                           @RequestParam("file") MultipartFile file) {
        storageService.uploadFile(provider, bucketName, file);
    }

    @GetMapping("/providers/{provider}/buckets/{bucketName}/files/{fileName}/presigned-url")
    public @ResponseBody Map<String, String> getPresignedUrl(@PathVariable CloudProviderEnum provider,
                                                             @PathVariable String bucketName,
                                                             @PathVariable String fileName) {
        return Map.of("presignedUrl", storageService.generatePresignedUrl(provider, bucketName, fileName));
    }
}
