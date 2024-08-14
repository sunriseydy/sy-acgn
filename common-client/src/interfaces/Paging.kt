package dev.sunriseydy.acgn.client.interfaces

import androidx.compose.runtime.MutableState

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
    suspend fun getData(paging: Paging<T>): List<T>

    fun onError(e: Exception)

    suspend fun load(init: Boolean) {
        if (loading.value) return
        loading.value = true
        if (init) page.value = 1L
        try {
            getData(this).also {
                if (init) data.value = it else data.value += it
                if (it.isEmpty()) finished.value = true
            }
        } catch (e: Exception) {
            onError(e)
        } finally {
            loading.value = false
        }
    }

    suspend fun loadNext() {
        if (finished.value) return
        page.value++
        load(init = false)
    }

    suspend fun loadInit() {
        finished.value = false
        load(init = true)
    }
}

fun <T> getPager(
    page: MutableState<Long>,
    size: Int,
    data: MutableState<List<T>>,
    loading: MutableState<Boolean>,
    finished: MutableState<Boolean>,
    onError: (Exception) -> Unit,
    getData: suspend (Paging<T>) -> List<T>,
): Paging<T> = object : Paging<T> {
    override val page: MutableState<Long>
        get() = page
    override val size: Int
        get() = size
    override val data: MutableState<List<T>>
        get() = data
    override val loading: MutableState<Boolean>
        get() = loading
    override val finished: MutableState<Boolean>
        get() = finished

    override fun onError(e: Exception) = onError(e)

    override suspend fun getData(paging: Paging<T>): List<T> = getData(this)
}