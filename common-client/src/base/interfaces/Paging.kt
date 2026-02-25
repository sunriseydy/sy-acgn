package dev.sunriseydy.acgn.client.base.interfaces

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author SunriseYDY
 * @date 2024-08-14 19:50
 */
interface Paging<T> {
    val page: MutableState<Long>
    val size: Int
    val data: MutableState<List<T>>
    val loading: MutableState<Boolean>
    val finished: MutableState<Boolean>
    val scope: CoroutineScope
    suspend fun getData(paging: Paging<T>): List<T>

    fun onError(e: Exception)

    fun load(init: Boolean) {
        if (loading.value) return
        if (finished.value) return
        loading.value = true
        if (init) page.value = 1L
        scope.launch {
            try {
                getData(this@Paging).also {
                    if (it.isEmpty()) finished.value = true
                    if (init) data.value = it else data.value += it
                }
            } catch (e: Exception) {
                onError(e)
            } finally {
                loading.value = false
            }
        }
    }

    fun loadNext() {
        page.value++
        load(init = false)
    }

    fun loadInit() {
        finished.value = false
        load(init = true)
    }
}

/**
 * 创建分页器实例
 *
 * @param page 当前页码状态
 * @param size 每页大小
 * @param data 数据列表状态
 * @param loading 加载中状态
 * @param finished 是否已加载完毕状态
 * @param scope 协程作用域
 * @param onError 加载出错时的回调
 * @param getData 获取数据的函数
 * @return Paging 实例
 */
fun <T> getPager(
    page: MutableState<Long>,
    size: Int,
    data: MutableState<List<T>>,
    loading: MutableState<Boolean>,
    finished: MutableState<Boolean>,
    scope: CoroutineScope,
    onError: (Exception) -> Unit,
    getData: suspend (Paging<T>) -> List<T>,
): Paging<T> = object : Paging<T> {
    override val page = page
    override val size = size
    override val data = data
    override val loading = loading
    override val finished = finished
    override val scope = scope

    override fun onError(e: Exception) = onError(e)

    override suspend fun getData(paging: Paging<T>): List<T> = getData(paging)
}