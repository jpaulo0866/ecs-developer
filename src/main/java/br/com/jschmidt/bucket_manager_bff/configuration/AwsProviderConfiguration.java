package br.com.jschmidt.bucket_manager_bff.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnBooleanProperty(name = "cloud.aws.enabled", havingValue = true, matchIfMissing = false)
public class AwsProviderConfiguration {

    @Bean
    public S3Client s3Client(@Value("${cloud.aws.region}") String region,
                             @Value("${cloud.aws.credentials.access-key}") String accessKey,
                             @Value("${cloud.aws.credentials.secret-key}") String accessSecretKey) {
        return S3Client
                .builder()
                .region(Region.of(region))
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, accessSecretKey))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${cloud.aws.region}") String region,
                                 @Value("${cloud.aws.credentials.access-key}") String accessKey,
                                 @Value("${cloud.aws.credentials.secret-key}") String accessSecretKey) {
        return S3Presigner
                .builder()
                .region(Region.of(region))
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, accessSecretKey))
                .build();
    }
}
