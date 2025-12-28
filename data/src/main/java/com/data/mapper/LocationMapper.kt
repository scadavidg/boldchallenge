package com.data.mapper

import com.data.db.LocationEntity
import com.data.dto.LocationDto
import com.domain.model.Location

fun LocationDto.toDomain(): Location {
    return Location(
        id = id,
        name = name,
        region = region,
        country = country,
        lat = lat,
        lon = lon,
        url = url
    )
}

fun LocationEntity.toDomain(): Location {
    return Location(
        id = id,
        name = name,
        region = region,
        country = country,
        lat = lat,
        lon = lon,
        url = url
    )
}

fun LocationDto.toEntity(query: String): LocationEntity {
    return LocationEntity(
        id = id,
        name = name,
        region = region,
        country = country,
        lat = lat,
        lon = lon,
        url = url,
        query = query
    )
}
