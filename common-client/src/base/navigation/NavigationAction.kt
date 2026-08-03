package dev.sunriseydy.acgn.client.base.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * 导航动作处理器
 *
 * 管理基于栈的导航，为每个顶层路由维护独立的导航栈。
 */
class NavigationAction<T : NavigationRoute>(startKey: T) {
    // 维护每个顶层路由的独立导航栈
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // 暴露当前的顶层路由 Key，供外部消费者使用
    var topLevelKey by mutableStateOf(startKey)
        private set

    // 暴露完整的后退栈，供 NavDisplay 渲染使用
    val backStack = mutableStateListOf(startKey)

    /**
     * 更新后退栈
     *
     * 清空当前后退栈，并将所有顶层路由栈的内容扁平化添加到后退栈中。
     */
    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    /**
     * 添加或切换到顶层路由
     *
     * 如果该顶层路由栈不存在，则创建并初始化。
     * 如果已存在，则将其移动到通过 LinkedHashMap 维护的顺序末尾。
     * 最后更新当前顶层 Key 和后退栈。
     *
     * @param key 要切换到的顶层路由 Key
     */
    fun addTopLevel(key: T) {

        // 如果顶层栈不存在，则添加
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            // 否则将其移到栈的末尾（为了保持正确的层级顺序）
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    /**
     * 向当前顶层路由栈添加新页面
     *
     * @param key 要添加的页面路由 Key
     */
    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    /**
     * 移除当前栈顶页面（后退）
     *
     * 移除当前顶层栈的最后一个页面。
     * 如果移除的页面是该顶层栈的根页面（即顶层路由本身），则移除整个顶层栈，
     * 并切换回上一个顶层路由。
     */
    fun removeLast() {
        topLevelStacks[topLevelKey]?.removeLastOrNull()
        // 如果移除的 Key 是顶层 Stack 的最后一个元素（即顶层路由），则移除关联的顶层 Stack
        if (topLevelStacks[topLevelKey]?.isEmpty() == true) {
            topLevelStacks.remove(topLevelKey)
            // 切换回上一个顶层路由（如果存在）
            if (topLevelStacks.isNotEmpty()) {
                topLevelKey = topLevelStacks.keys.last()
            }
        }
        updateBackStack()
    }
}
