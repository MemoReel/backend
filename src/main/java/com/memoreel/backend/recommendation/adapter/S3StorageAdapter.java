package com.memoreel.backend.recommendation.adapter;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.recommendation.port.StoragePort;
import com.memoreel.backend.recommendation.port.StoredPhoto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * S3 기반 2단계 저장소 구현. {@code temp/}에 먼저 저장하고, 저장 확정 시 {@code record/}로 이동한다.
 *
 * <p>버킷은 private로 유지하며, 클라이언트는 S3에 직접 접근하지 않는다. 화면 표시가 필요한 경우에만 만료 시간이 있는 presigned URL을 발급한다.
 */
@Component
public class S3StorageAdapter implements StoragePort {

  private static final String TEMP_DIR = "temp";
  private static final String RECORD_DIR = "record";
  private static final Duration VIEW_URL_TTL = Duration.ofMinutes(15);
  private static final Pattern FILENAME_PATTERN =
      Pattern.compile("^[A-Za-z0-9-]+\\.[A-Za-z0-9]{1,10}$");
  private static final Pattern EXTENSION_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,10}$");

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;

  public S3StorageAdapter(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${memoreel.storage.s3.bucket}") String bucket) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
  }

  @Override
  public StoredPhoto storeTemp(MultipartFile file) {
    String filename = UUID.randomUUID() + extension(file.getOriginalFilename());
    String key = TEMP_DIR + "/" + filename;
    putObject(key, file);
    return new StoredPhoto(key, presignedViewUrl(key));
  }

  @Override
  public StoredPhoto promote(String tempPhotoUrl) {
    String filename = filenameOf(tempPhotoUrl, TEMP_DIR);
    String sourceKey = TEMP_DIR + "/" + filename;
    String targetKey = RECORD_DIR + "/" + filename;
    moveObject(sourceKey, targetKey);
    return new StoredPhoto(targetKey, presignedViewUrl(targetKey));
  }

  @Override
  public String viewUrl(String photoUrl) {
    return presignedViewUrl(photoUrl);
  }

  private void putObject(String key, MultipartFile file) {
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType())
              .build(),
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new UncheckedIOException("사진 업로드에 실패했습니다.", e);
    }
  }

  private void moveObject(String sourceKey, String targetKey) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(sourceKey).build());
    } catch (NoSuchKeyException e) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "임시 저장된 사진을 찾을 수 없습니다.", null);
    }
    s3Client.copyObject(
        CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(sourceKey)
            .destinationBucket(bucket)
            .destinationKey(targetKey)
            .build());
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(sourceKey).build());
  }

  private String presignedViewUrl(String key) {
    GetObjectRequest getRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(VIEW_URL_TTL)
            .getObjectRequest(getRequest)
            .build();
    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  private String filenameOf(String key, String expectedDir) {
    String prefix = expectedDir + "/";
    if (!key.startsWith(prefix)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 photo_url 입니다.", null);
    }
    String filename = key.substring(prefix.length());
    if (!FILENAME_PATTERN.matcher(filename).matches()) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 photo_url 입니다.", null);
    }
    return filename;
  }

  private String extension(String originalFilename) {
    if (originalFilename == null) {
      return ".jpg";
    }
    try {
      String basename = Path.of(originalFilename).getFileName().toString();
      int dot = basename.lastIndexOf('.');
      String ext = dot >= 0 ? basename.substring(dot + 1) : "";
      return EXTENSION_PATTERN.matcher(ext).matches() ? "." + ext.toLowerCase() : ".jpg";
    } catch (RuntimeException e) {
      return ".jpg";
    }
  }
}
