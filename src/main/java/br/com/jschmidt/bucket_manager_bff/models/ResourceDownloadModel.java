package br.com.jschmidt.bucket_manager_bff.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class ResourceDownloadModel {
    private Resource resource;
    private String fileName;
    private String contentType;
}
