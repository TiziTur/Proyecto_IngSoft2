package com.aprovecha.app.domain.model

/**
 * Roles posibles de un usuario en la plataforma Aprovecha!
 *
 * // @REQ-F01: El comercio debe poder registrarse en la plataforma.
 * El rol se elige en el momento del registro y determina los flujos disponibles.
 */
enum class UserRole {
    /** Usuario final que busca y reserva packs de alimentos */
    CONSUMER,
    /** Comercio que publica packs de alimentos no vendidos */
    COMMERCE
}
