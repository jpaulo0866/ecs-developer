package br.com.jschmidt.bucket_manager_bff.configuration;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBooleanProperty(name = "cloud.azure.enabled", havingValue = true, matchIfMissing = false)
public class AzureProviderConfiguration {

    @Bean
    public BlobServiceClient blobServiceClient(@Value("${cloud.azure.connection-string}") String connectionString) {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
