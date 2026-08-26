package com.example.data.remote

import com.example.data.model.BloggerBlogInfo
import com.example.data.model.BloggerPostItem
import com.example.data.model.BloggerPostListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BloggerApiService {

    @GET("blogs/byurl")
    suspend fun getBlogByUrl(
        @Query("url") url: String = "https://shortstudy999.blogspot.com/",
        @Query("key") apiKey: String
    ): Response<BloggerBlogInfo>

    @GET("blogs/{blogId}/posts")
    suspend fun getPosts(
        @Path("blogId") blogId: String,
        @Query("key") apiKey: String,
        @Query("labels") labels: String? = null,
        @Query("pageToken") pageToken: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("fetchBodies") fetchBodies: Boolean = true
    ): Response<BloggerPostListResponse>

    @GET("blogs/{blogId}/posts/search")
    suspend fun searchPosts(
        @Path("blogId") blogId: String,
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("fetchBodies") fetchBodies: Boolean = true
    ): Response<BloggerPostListResponse>

    @GET("blogs/{blogId}/posts/{postId}")
    suspend fun getPostById(
        @Path("blogId") blogId: String,
        @Path("postId") postId: String,
        @Query("key") apiKey: String
    ): Response<BloggerPostItem>
}
