package com.aprovecha.app.common.annotation

/**
 * Anotación de trazabilidad para vincular código fuente con requerimientos del proyecto.
 *
 * Uso:
 * ```kotlin
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos para ser retirado
 * @Requirement(id = "REQ-F04", description = "Usuario reserva un pack disponible")
 * fun reservePack(packId: Long) { ... }
 * ```
 *
 * Esta anotación se retiene solo en el código fuente (SOURCE) para no agregar overhead
 * en runtime, y permite búsquedas en el IDE con: Ctrl+F "@Requirement(\"REQ-F0X\")"
 *
 * Equivalente de trazabilidad al comentario @REQ-FXX — ambas estrategias se usan
 * en conjunto en este proyecto.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE)
annotation class Requirement(
    /** ID del requerimiento, ej: "REQ-F01", "REQ-NF01" */
    val id: String,
    /** Descripción corta del requerimiento implementado */
    val description: String = ""
)
