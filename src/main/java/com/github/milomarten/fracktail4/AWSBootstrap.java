package com.github.milomarten.fracktail4;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "aws", name = "access-key")
public class AWSBootstrap {
    @Bean
    public AmazonS3 amazonS3(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.token}") String secret,
            @Value("${aws.region}") Regions regions) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        accessKey,
                        secret
                )))
                .withRegion(regions)
                .build();
    }
}
