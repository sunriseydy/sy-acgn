package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.StringAppConfig

/**
 * S3 存储配置常量
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
object S3AppConfig {

    /** S3 端点 URL */
    object S3Endpoint : StringAppConfig, CommonModule

    /** S3 访问密钥 */
    object S3AccessKey : StringAppConfig, CommonModule

    /** S3 私有密钥 */
    object S3SecretKey : StringAppConfig, CommonModule

    /** S3 存储桶名称 */
    object S3BucketName : StringAppConfig, CommonModule

    /** S3 区域 */
    object S3Region : StringAppConfig, CommonModule
}
