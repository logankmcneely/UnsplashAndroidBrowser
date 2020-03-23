package com.phorium.flickrimagebrowser

class Photo(
    var id: String,
    var createdAt: String,
    var urlRaw: String,
    var urlFull: String,
    var urlRegular: String,
    var urlSmall: String,
    var urlThumb: String,
    var width: Int,
    var height: Int,
    var color: String,
    var description: String,
    var user: String
)