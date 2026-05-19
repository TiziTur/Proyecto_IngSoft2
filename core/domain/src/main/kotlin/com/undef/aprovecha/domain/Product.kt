package com.undef.aprovecha.domain

/**
 * Clase base para representar productos en el dominio
 */
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double
)
