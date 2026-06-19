package com.memoreel.backend.recommendation.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3StorageConfig {

  @Bean
  public S3Client s3Client(@Value("${memoreel.storage.s3.region}") String region) {
    return S3Client.builder().region(Region.of(region)).build();
  }

  @Bean
  public S3Presigner s3Presigner(@Value("${memoreel.storage.s3.region}") String region) {
    return S3Presigner.builder().region(Region.of(region)).build();
  }
}
