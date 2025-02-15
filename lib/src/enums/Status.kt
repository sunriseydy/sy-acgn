package dev.sunriseydy.acgn.enums

import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable

/**
 * 通用状态枚举
 * @author SunriseYDY
 * @date 2025-02-15 20:26
 */
enum class Status : CommonModuleLocalizable, EnumLocalizable {
    /**
     * 未处理
     */
    UNPROCESS,

    /**
     * 处理中
     */
    PROCESSING,

    /**
     * 已处理
     */
    PROCESSED,
}