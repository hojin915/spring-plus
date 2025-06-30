package org.example.expert.config.S3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
public class S3Uploader {
    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;

    public S3Uploader(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucketName, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Presigner = s3Presigner;
    }

    public String upload(MultipartFile file, String dirName, Long userId) {
        String fileName = getS3Key(dirName, userId, file);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    //.acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            file.getInputStream(), file.getSize()
                    )
            );

            return getFileUrl(fileName);
        } catch(IOException e){
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    // imageUrl key 부분 리턴
    public String getS3Key(String dirName, Long userId, MultipartFile file) {
        return dirName + "/" + userId + getFileExtension(file.getOriginalFilename());
    }

    // 파일 확장자 리턴
    private String getFileExtension(String fileName) {
        if(fileName == null || fileName.contains(".")){
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }

    // 읽기 가능한 PresignedUrl 생성
    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Presigner.presignGetObject(
                b -> b.signatureDuration(Duration.ofMinutes(10)).getObjectRequest(getObjectRequest)
        ).url().toString();
    }
}
