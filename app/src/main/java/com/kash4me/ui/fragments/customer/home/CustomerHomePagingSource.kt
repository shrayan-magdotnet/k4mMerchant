package com.kash4me.ui.fragments.customer.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kash4me.data.local.customer.cashback.CashbackDao
import com.kash4me.data.local.customer.cashback.CashbackEntity

class CustomerHomePagingSource(
    private val dao: CashbackDao
) : PagingSource<Int, CashbackEntity>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CashbackEntity> {
        val page = params.key ?: 0

        return try {
            val entities = dao.getPagedList(params.loadSize, page * params.loadSize)
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (entities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CashbackEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}