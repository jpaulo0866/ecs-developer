package br.com.jschmidt.bucket_manager_bff.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@ConditionalOnBooleanProperty(name = "cloud.gcp.enabled", havingValue = true, matchIfMissing = false)
public class GcpProviderConfiguration {

    @Bean
    public Storage gcpStorage(@Value("${cloud.gcp.project-id}") String projectId,
                              @Value("${cloud.gcp.credentials.encoded-key}") String encodedCredentials) throws IOException {
        byte[] credentialsBytes = Base64.getDecoder().decode(encodedCredentials);

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(credentialsBytes));

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
