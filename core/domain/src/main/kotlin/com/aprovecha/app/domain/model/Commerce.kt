package com.aprovecha.app.domain.model

/**
 * Modelo de dominio para un comercio registrado en la plataforma.
 *
 * // @REQ-F01: El comercio debe poder registrarse en la plataforma.
 * // @REQ-F03: Usado para calcular distancia al usuario consumidor.
 *
 * @param id Identificador único del comercio
 * @param userId Referencia al [User] propietario del comercio
 * @param nombre Nombre del comercio (ej: "Panadería El Trigo")
 * @param direccion Dirección física del comercio
 * @param latitud Coordenada de latitud para geolocalización (REQ-F03)
 * @param longitud Coordenada de longitud para geolocalización (REQ-F03)
 * @param telefono Teléfono de contacto opcional
 */
data class Commerce(
    val id: Long = 0,
    val userId: Long,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val telefono: String? = null
)
