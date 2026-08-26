package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BloggerPostListResponse(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
    @Json(name = "items") val items: List<BloggerPostItem>? = null,
    @Json(name = "totalItems") val totalItems: Int? = null
)

@JsonClass(generateAdapter = true)
data class BloggerPostItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String? = null,
    @Json(name = "published") val published: String? = null,
    @Json(name = "updated") val updated: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "selfLink") val selfLink: String? = null,
    @Json(name = "author") val author: BloggerAuthor? = null,
    @Json(name = "labels") val labels: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class BloggerAuthor(
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "image") val image: BloggerAuthorImage? = null
)

@JsonClass(generateAdapter = true)
data class BloggerAuthorImage(
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class BloggerBlogInfo(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String? = null
)
