package com.undef.aprovecha.domain.repository

import com.undef.aprovecha.domain.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: Int): Product?
}
