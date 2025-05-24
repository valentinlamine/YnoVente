package com.example.ynovente.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ynovente.data.model.Offer

class FakeOfferRepository {
    fun getOffersPagingSource(): PagingSource<Int, Offer> = object : PagingSource<Int, Offer>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val offers = (1..100).map {
                Offer(
                    id = it.toString(),
                    title = "Produit $it",
                    price = it * 10.0,
                    endDate = "2025-12-31"
                )
            }.drop((page - 1) * pageSize).take(pageSize)
            val nextKey = if (offers.isEmpty()) null else page + 1
            return LoadResult.Page(
                data = offers,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        }

        override fun getRefreshKey(state: PagingState<Int, Offer>): Int? {
            // Cette implémentation suit la recommandation officielle Paging 3
            return state.anchorPosition?.let { anchorPosition ->
                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            }
        }
    }
}