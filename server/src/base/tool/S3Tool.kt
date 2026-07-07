package dev.sunriseydy.acgn.server.base.tool

import dev.sunriseydy.acgn.common.config.S3AppConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.InputStream
import java.net.URI

private val logger = KotlinLogging.logger { }

/**
 * S3 对象存储工具类
 *
 * 用于上传、下载、删除 S3 存储桶中的对象，适配 MinIO 等 S3 兼容存储。
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
class S3Tool {
    private val endpoint = S3AppConfig.S3Endpoint.configValue
    private val accessKey = S3AppConfig.S3AccessKey.configValue
    private val secretKey = S3AppConfig.S3SecretKey.configValue
    private val bucketName = S3AppConfig.S3BucketName.configValue
    private val regionStr = if (S3AppConfig.S3Region.configValue.isNullOrBlank()) "us-east-1" else S3AppConfig.S3Region.configValue

    private val s3Client: S3Client by lazy {
        checkNotNull(endpoint) { "S3 endpoint is null" }
        checkNotNull(accessKey) { "S3 accessKey is null" }
        checkNotNull(secretKey) { "S3 secretKey is null" }
        checkNotNull(bucketName) { "S3 bucketName is null" }

        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(regionStr))
            .forcePathStyle(true) // 启用 PathStyle 访问模式以支持 MinIO
            .build()
    }

    init {
        logger.info { "S3Tool initialized with endpoint: $endpoint, bucket: $bucketName" }
    }

    /**
     * 确保存储桶已创建
     */
    private fun ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())
        } catch (e: Exception) {
            logger.info { "Bucket $bucketName does not exist. Creating..." }
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                logger.info { "Bucket $bucketName created successfully." }
            } catch (ce: Exception) {
                logger.error(ce) { "Failed to create bucket $bucketName" }
                throw ce
            }
        }
    }

    /**
     * 上传对象到 S3
     */
    fun putObject(key: String, inputStream: InputStream, contentLength: Long, contentType: String) {
        ensureBucketExists()
        val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build()
        s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength))
        logger.info { "Successfully uploaded object to S3: $key" }
    }

    /**
     * 从 S3 下载对象
     */
    fun getObject(key: String): InputStream {
        val getRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()
        return s3Client.getObject(getRequest)
    }

    /**
     * 从 S3 删除对象
     */
    fun deleteObject(key: String) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()
        s3Client.deleteObject(deleteRequest)
        logger.info { "Successfully deleted object from S3: $key" }
    }
}
