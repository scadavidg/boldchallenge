package com.data.mapper

import com.data.db.LocationEntity
import com.data.dto.LocationDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("LocationMapper Tests")
class LocationMapperTest {

    @Nested
    @DisplayName("LocationDto.toDomain()")
    inner class LocationDtoToDomainTest {

        @Test
        @DisplayName("GIVEN a LocationDto with all fields populated WHEN mapping to domain THEN should return Location with all fields correctly mapped")
        fun `given LocationDto with all fields when mapping to domain then returns Location with all fields correctly mapped`() {
            // Given
            val locationDto = LocationDto(
                id = 12345L,
                name = "Bogotá",
                region = "Cundinamarca",
                country = "Colombia",
                lat = 4.6097,
                lon = -74.0817,
                url = "bogota-colombia"
            )

            // When
            val result = locationDto.toDomain()

            // Then
            assertEquals(12345L, result.id)
            assertEquals("Bogotá", result.name)
            assertEquals("Cundinamarca", result.region)
            assertEquals("Colombia", result.country)
            assertEquals(4.6097, result.lat)
            assertEquals(-74.0817, result.lon)
            assertEquals("bogota-colombia", result.url)
        }

        @Test
        @DisplayName("GIVEN a LocationDto with null optional fields WHEN mapping to domain THEN should return Location with null values preserved")
        fun `given LocationDto with null optional fields when mapping to domain then returns Location with null values preserved`() {
            // Given
            val locationDto = LocationDto(
                id = 67890L,
                name = "Medellín",
                region = null,
                country = "Colombia",
                lat = null,
                lon = null,
                url = null
            )

            // When
            val result = locationDto.toDomain()

            // Then
            assertEquals(67890L, result.id)
            assertEquals("Medellín", result.name)
            assertEquals(null, result.region)
            assertEquals("Colombia", result.country)
            assertEquals(null, result.lat)
            assertEquals(null, result.lon)
            assertEquals(null, result.url)
        }

        @Test
        @DisplayName("GIVEN a LocationDto with zero coordinates WHEN mapping to domain THEN should return Location with zero coordinates")
        fun `given LocationDto with zero coordinates when mapping to domain then returns Location with zero coordinates`() {
            // Given
            val locationDto = LocationDto(
                id = 11111L,
                name = "Origin",
                region = "Region",
                country = "Country",
                lat = 0.0,
                lon = 0.0,
                url = "origin"
            )

            // When
            val result = locationDto.toDomain()

            // Then
            assertEquals(11111L, result.id)
            assertEquals(0.0, result.lat)
            assertEquals(0.0, result.lon)
        }

        @Test
        @DisplayName("GIVEN a LocationDto with negative coordinates WHEN mapping to domain THEN should return Location with negative coordinates preserved")
        fun `given LocationDto with negative coordinates when mapping to domain then returns Location with negative coordinates preserved`() {
            // Given
            val locationDto = LocationDto(
                id = 22222L,
                name = "South America",
                region = "Region",
                country = "Colombia",
                lat = -4.6097,
                lon = -74.0817,
                url = "south-america"
            )

            // When
            val result = locationDto.toDomain()

            // Then
            assertEquals(22222L, result.id)
            assertEquals(-4.6097, result.lat)
            assertEquals(-74.0817, result.lon)
        }

        @Test
        @DisplayName("GIVEN a LocationDto with empty string values WHEN mapping to domain THEN should return Location with empty strings preserved")
        fun `given LocationDto with empty string values when mapping to domain then returns Location with empty strings preserved`() {
            // Given
            val locationDto = LocationDto(
                id = 33333L,
                name = "",
                region = "",
                country = "",
                lat = null,
                lon = null,
                url = ""
            )

            // When
            val result = locationDto.toDomain()

            // Then
            assertEquals(33333L, result.id)
            assertEquals("", result.name)
            assertEquals("", result.region)
            assertEquals("", result.country)
            assertEquals("", result.url)
        }
    }

    @Nested
    @DisplayName("LocationEntity.toDomain()")
    inner class LocationEntityToDomainTest {

        @Test
        @DisplayName("GIVEN a LocationEntity with all fields populated WHEN mapping to domain THEN should return Location with all fields correctly mapped")
        fun `given LocationEntity with all fields when mapping to domain then returns Location with all fields correctly mapped`() {
            // Given
            val locationEntity = LocationEntity(
                id = 12345L,
                name = "Cali",
                region = "Valle del Cauca",
                country = "Colombia",
                lat = 3.4516,
                lon = -76.5320,
                url = "cali-colombia",
                query = "cali"
            )

            // When
            val result = locationEntity.toDomain()

            // Then
            assertEquals(12345L, result.id)
            assertEquals("Cali", result.name)
            assertEquals("Valle del Cauca", result.region)
            assertEquals("Colombia", result.country)
            assertEquals(3.4516, result.lat)
            assertEquals(-76.5320, result.lon)
            assertEquals("cali-colombia", result.url)
        }

        @Test
        @DisplayName("GIVEN a LocationEntity with null optional fields WHEN mapping to domain THEN should return Location with null values preserved")
        fun `given LocationEntity with null optional fields when mapping to domain then returns Location with null values preserved`() {
            // Given
            val locationEntity = LocationEntity(
                id = 44444L,
                name = "Barranquilla",
                region = null,
                country = "Colombia",
                lat = null,
                lon = null,
                url = null,
                query = "barranquilla"
            )

            // When
            val result = locationEntity.toDomain()

            // Then
            assertEquals(44444L, result.id)
            assertEquals("Barranquilla", result.name)
            assertEquals(null, result.region)
            assertEquals("Colombia", result.country)
            assertEquals(null, result.lat)
            assertEquals(null, result.lon)
            assertEquals(null, result.url)
        }

        @Test
        @DisplayName("GIVEN a LocationEntity WHEN mapping to domain THEN should not include query field in result")
        fun `given LocationEntity when mapping to domain then does not include query field in result`() {
            // Given
            val locationEntity = LocationEntity(
                id = 55555L,
                name = "Cartagena",
                region = "Bolívar",
                country = "Colombia",
                lat = 10.3910,
                lon = -75.4794,
                url = "cartagena-colombia",
                query = "cartagena search query"
            )

            // When
            val result = locationEntity.toDomain()

            // Then
            // Verify that query is not part of the domain model (Location doesn't have query field)
            assertEquals(55555L, result.id)
            assertEquals("Cartagena", result.name)
            // The query field is a cache concern and should not be in the domain model
        }

        @Test
        @DisplayName("GIVEN a LocationEntity with extreme coordinate values WHEN mapping to domain THEN should return Location with extreme values preserved")
        fun `given LocationEntity with extreme coordinate values when mapping to domain then returns Location with extreme values preserved`() {
            // Given
            val locationEntity = LocationEntity(
                id = 66666L,
                name = "Extreme Location",
                region = "Region",
                country = "Country",
                lat = 90.0,
                lon = 180.0,
                url = "extreme",
                query = "extreme"
            )

            // When
            val result = locationEntity.toDomain()

            // Then
            assertEquals(66666L, result.id)
            assertEquals(90.0, result.lat)
            assertEquals(180.0, result.lon)
        }
    }

    @Nested
    @DisplayName("LocationDto.toEntity()")
    inner class LocationDtoToEntityTest {

        @Test
        @DisplayName("GIVEN a LocationDto and query string WHEN mapping to entity THEN should return LocationEntity with query field included")
        fun `given LocationDto and query string when mapping to entity then returns LocationEntity with query field included`() {
            // Given
            val locationDto = LocationDto(
                id = 12345L,
                name = "Santa Marta",
                region = "Magdalena",
                country = "Colombia",
                lat = 11.2408,
                lon = -74.1990,
                url = "santa-marta-colombia"
            )
            val query = "santa marta"

            // When
            val result = locationDto.toEntity(query)

            // Then
            assertEquals(12345L, result.id)
            assertEquals("Santa Marta", result.name)
            assertEquals("Magdalena", result.region)
            assertEquals("Colombia", result.country)
            assertEquals(11.2408, result.lat)
            assertEquals(-74.1990, result.lon)
            assertEquals("santa-marta-colombia", result.url)
            assertEquals(query, result.query)
        }

        @Test
        @DisplayName("GIVEN a LocationDto with null optional fields and query WHEN mapping to entity THEN should return LocationEntity with null values and query preserved")
        fun `given LocationDto with null optional fields and query when mapping to entity then returns LocationEntity with null values and query preserved`() {
            // Given
            val locationDto = LocationDto(
                id = 77777L,
                name = "Pereira",
                region = null,
                country = "Colombia",
                lat = null,
                lon = null,
                url = null
            )
            val query = "pereira"

            // When
            val result = locationDto.toEntity(query)

            // Then
            assertEquals(77777L, result.id)
            assertEquals("Pereira", result.name)
            assertEquals(null, result.region)
            assertEquals("Colombia", result.country)
            assertEquals(null, result.lat)
            assertEquals(null, result.lon)
            assertEquals(null, result.url)
            assertEquals(query, result.query)
        }

        @Test
        @DisplayName("GIVEN a LocationDto and empty query string WHEN mapping to entity THEN should return LocationEntity with empty query")
        fun `given LocationDto and empty query string when mapping to entity then returns LocationEntity with empty query`() {
            // Given
            val locationDto = LocationDto(
                id = 88888L,
                name = "Manizales",
                region = "Caldas",
                country = "Colombia",
                lat = 5.0700,
                lon = -75.5133,
                url = "manizales-colombia"
            )
            val query = ""

            // When
            val result = locationDto.toEntity(query)

            // Then
            assertEquals(88888L, result.id)
            assertEquals("Manizales", result.name)
            assertEquals(query, result.query)
        }

        @Test
        @DisplayName("GIVEN a LocationDto and query with special characters WHEN mapping to entity THEN should return LocationEntity with query preserved as-is")
        fun `given LocationDto and query with special characters when mapping to entity then returns LocationEntity with query preserved as-is`() {
            // Given
            val locationDto = LocationDto(
                id = 99999L,
                name = "Armenia",
                region = "Quindío",
                country = "Colombia",
                lat = 4.5339,
                lon = -75.6811,
                url = "armenia-colombia"
            )
            val query = "armenia, colombia (capital)"

            // When
            val result = locationDto.toEntity(query)

            // Then
            assertEquals(99999L, result.id)
            assertEquals("Armenia", result.name)
            assertEquals(query, result.query)
        }

        @Test
        @DisplayName("GIVEN a LocationDto WHEN mapping to entity with different queries THEN should return different entities with same data but different queries")
        fun `given LocationDto when mapping to entity with different queries then returns different entities with same data but different queries`() {
            // Given
            val locationDto = LocationDto(
                id = 11111L,
                name = "Bucaramanga",
                region = "Santander",
                country = "Colombia",
                lat = 7.1254,
                lon = -73.1198,
                url = "bucaramanga-colombia"
            )
            val query1 = "bucaramanga"
            val query2 = "bucara"

            // When
            val result1 = locationDto.toEntity(query1)
            val result2 = locationDto.toEntity(query2)

            // Then
            assertEquals(result1.id, result2.id)
            assertEquals(result1.name, result2.name)
            assertEquals(result1.region, result2.region)
            assertEquals(result1.country, result2.country)
            assertEquals(result1.lat, result2.lat)
            assertEquals(result1.lon, result2.lon)
            assertEquals(result1.url, result2.url)
            assertEquals(query1, result1.query)
            assertEquals(query2, result2.query)
        }
    }

    @Nested
    @DisplayName("Round-trip mapping tests")
    inner class RoundTripMappingTest {

        @Test
        @DisplayName("GIVEN a LocationDto WHEN mapping to domain and back is not applicable THEN domain model should contain all necessary information")
        fun `given LocationDto when mapping to domain then domain model contains all necessary information`() {
            // Given
            val locationDto = LocationDto(
                id = 123456L,
                name = "Test City",
                region = "Test Region",
                country = "Test Country",
                lat = 10.0,
                lon = -10.0,
                url = "test-city"
            )

            // When
            val domainModel = locationDto.toDomain()
            val entity = locationDto.toEntity("test query")
            val entityToDomain = entity.toDomain()

            // Then
            // Verify that DTO -> Domain and DTO -> Entity -> Domain produce same domain model
            assertEquals(domainModel.id, entityToDomain.id)
            assertEquals(domainModel.name, entityToDomain.name)
            assertEquals(domainModel.region, entityToDomain.region)
            assertEquals(domainModel.country, entityToDomain.country)
            assertEquals(domainModel.lat, entityToDomain.lat)
            assertEquals(domainModel.lon, entityToDomain.lon)
            assertEquals(domainModel.url, entityToDomain.url)
        }
    }
}

