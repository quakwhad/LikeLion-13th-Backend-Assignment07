package com.likelion.basecode.common.s3;

import com.amazonaws.SdkClientException; // 기존 AWS SDK v1 예외
import com.amazonaws.services.s3.AmazonS3; // 기존 AWS SDK v1 AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest; // S3 파일 삭제를 위해 추가 (v1)
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 어노테이션 사용을 위함
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream; // upload 메서드에서 InputStream 사용을 위함 (try-with-resources)
import java.net.URI; // URL 파싱을 위해 추가
import java.net.URISyntaxException; // URI 파싱 시 발생할 수 있는 예외를 위해 추가
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();

        try {
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAIL, ErrorCode.S3_UPLOAD_FAIL.getMessage());
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 S3 파일 URL이 null이거나 비어있습니다. 삭제를 건너뜁니다.");
            return;
        }

        try {
            // S3 URL에서 객체 키(key) 추출
            String fileKey = getFileKeyFromUrl(fileUrl);

            if (fileKey == null || fileKey.isEmpty()) {
                log.warn("S3 URL에서 파일 키를 추출할 수 없습니다: {}", fileUrl);
                return;
            }

            // S3에서 객체 삭제 요청 생성 및 실행
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
            log.info("S3 버킷에서 파일 삭제 성공: {}", fileKey); // 성공 로그

        } catch (SdkClientException e) {
            // S3 클라이언트 관련 예외 발생 시
            log.error("S3 버킷 파일 삭제 실패: {}. URL: {}", e.getMessage(), fileUrl, e);
        } catch (Exception e) { // 다른 예상치 못한 예외 처리
            log.error("예상치 못한 오류로 S3 파일 삭제 실패: {}. URL: {}", e.getMessage(), fileUrl, e);
        }
    }


    // S3 URL에서 파일 키(Key)를 추출
    private String getFileKeyFromUrl(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                return path.substring(1); // 첫 번째 '/' 문자 제거
            }
            return path;
        } catch (URISyntaxException e) {
            log.error("잘못된 S3 URL 형식입니다. URL: {}", fileUrl, e);
            return null;
        }
    }
}
