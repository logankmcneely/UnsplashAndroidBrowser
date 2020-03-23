package com.phorium.flickrimagebrowser

private const val KEY = "14d28c111fc43cb2211b247a8cbbe9e6a0f5a4aa42684fa651be7e29f44d97f3"

class SearchParams(
    searchQuery: String,
    orientation: String,
    pageNumber: Int
) {
    var randomURL: String = "https://api.unsplash.com/photos/random?client_id=$KEY&count=30"
    var sortURL: String = "https://api.unsplash.com/photos?client_id=$KEY&per_page=30"
    var searchURL: String = "https://api.unsplash.com/search/photos?client_id=$KEY&per_page=30"
    var searchQuery: String = searchQuery
    var orientation: String = orientation
    var pageNumber: Int = pageNumber
    var systemUI: Boolean = false
}