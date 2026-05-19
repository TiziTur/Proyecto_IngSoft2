package com.aprovecha.app.domain.model

/**
 * Estados posibles de un pack de alimentos.
 *
 * Máquina de estados:
 *   AVAILABLE ──(usuario reserva)──► RESERVED
 *   RESERVED  ──(comercio retira)──► WITHDRAWN
 *   RESERVED  ──(usuario cancela)──► CANCELLED
 *
 * // @REQ-F02: El comercio publica packs → estado inicial AVAILABLE
 * // @REQ-F04: El usuario reserva → AVAILABLE → RESERVED
 * // @REQ-F05: El comercio marca como retirado → RESERVED → WITHDRAWN
 * // @REQ-F06: El usuario cancela → RESERVED → CANCELLED
 * // @REQ-NF01: Solo un usuario puede mover AVAILABLE → RESERVED (concurrencia)
 */
enum class PackStatus {
    /** Pack publicado por el comercio, disponible para reserva */
    AVAILABLE,
    /** Pack reservado por un usuario, pendiente de retiro */
    RESERVED,
    /** Pack retirado exitosamente por el usuario (estado final) */
    WITHDRAWN,
    /** Reserva cancelada por el usuario (estado final) */
    CANCELLED
}
