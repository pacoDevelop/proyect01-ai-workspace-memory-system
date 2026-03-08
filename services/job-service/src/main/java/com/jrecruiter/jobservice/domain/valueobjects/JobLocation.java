package com.jrecruiter.jobservice.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Job Location
 *
 * Representa la localización de una oferta de empleo.
 * Modelo alineado con:
 * - DTOs de API (`CreateJobRequest.LocationRequest`, `JobResponse.LocationResponse`)
 * - Entidad JPA `JobLocationEmbeddable`
 *
 * Invariantes:
 * - `country` y `countryCode` obligatorios.
 * - Si hay coordenadas, deben estar en los rangos válidos.
 * - Se permite:
 *   - Sólo dirección física.
 *   - Sólo coordenadas (por ejemplo, remoto puro).
 *   - Ambas.
 */
public final class JobLocation {

    private final String street;
    private final String city;
    private final String stateProvince;
    private final String postalCode;
    private final String country;
    private final String countryCode;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final boolean remote;

    private JobLocation(
            String street,
            String city,
            String stateProvince,
            String postalCode,
            String country,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean remote) {

        this.country = Objects.requireNonNull(country, "Country cannot be null");
        this.countryCode = Objects.requireNonNull(countryCode, "Country code cannot be null");

        if (country.length() > 100) {
            throw new IllegalArgumentException("Country name too long");
        }
        if (countryCode.length() != 2) {
            throw new IllegalArgumentException("Country code must be ISO 3166-1 alpha-2 (2 chars)");
        }

        boolean hasAddress = (city != null && !city.isBlank())
                || (street != null && !street.isBlank())
                || (postalCode != null && !postalCode.isBlank());

        boolean hasCoordinates = latitude != null && longitude != null;

        if (hasCoordinates) {
            double lat = latitude.doubleValue();
            double lon = longitude.doubleValue();
            if (lat < -90 || lat > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            if (lon < -180 || lon > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
        }

        if (!hasAddress && !hasCoordinates) {
            throw new IllegalArgumentException(
                    "Job location must have at least some address fields or valid coordinates");
        }

        this.street = street;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.remote = remote;
    }

    /**
     * Crea una localización basada sólo en dirección física.
     */
    public static JobLocation withAddress(
            String street,
            String city,
            String stateProvince,
            String postalCode,
            String country,
            String countryCode) {
        return new JobLocation(
                street,
                city,
                stateProvince,
                postalCode,
                country,
                countryCode,
                null,
                null,
                false
        );
    }

    /**
     * Crea una localización con dirección y flag de remoto.
     */
    public static JobLocation withAddress(
            String street,
            String city,
            String stateProvince,
            String postalCode,
            String country,
            String countryCode,
            boolean remote) {
        return new JobLocation(
                street,
                city,
                stateProvince,
                postalCode,
                country,
                countryCode,
                null,
                null,
                remote
        );
    }

    /**
     * Crea una localización basada sólo en coordenadas.
     */
    public static JobLocation withCoordinates(
            String country,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean remote) {
        return new JobLocation(
                null,
                null,
                null,
                null,
                country,
                countryCode,
                latitude,
                longitude,
                remote
        );
    }

    /**
     * Crea una localización con dirección y coordenadas.
     */
    public static JobLocation withAddressAndCoordinates(
            String street,
            String city,
            String stateProvince,
            String postalCode,
            String country,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean remote) {
        return new JobLocation(
                street,
                city,
                stateProvince,
                postalCode,
                country,
                countryCode,
                latitude,
                longitude,
                remote
        );
    }

    // Getters alineados con DTOs y embeddable

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public boolean isRemote() {
        return remote;
    }
}
