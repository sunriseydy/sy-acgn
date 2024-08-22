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
    fun getData(paging: Paging<T>): List<T>

    fun onError(e: Exception)

    fun load(init: Boolean) {
        if (loading.value) return
        if (finished.value) return
        loading.value = true
        if (init) page.value = 1L
        try {
            getData(this).also {
                if (it.isEmpty()) finished.value = true
                if (init) data.value = it else data.value += it
            }
        } catch (e: Exception) {
            onError(e)
        } finally {
            loading.value = false
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

fun <T> getPager(
    page: MutableState<Long>,
    size: Int,
    data: MutableState<List<T>>,
    loading: MutableState<Boolean>,
    finished: MutableState<Boolean>,
    onError: (Exception) -> Unit,
    getData: (Paging<T>) -> List<T>,
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

    override fun getData(paging: Paging<T>): List<T> = getData(this)
}