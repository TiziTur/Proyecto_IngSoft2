package com.undef.aprovecha.data.repository

import com.undef.aprovecha.domain.Product
import com.undef.aprovecha.domain.repository.ProductRepository

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        // Aquí iría la llamada al API o base de datos
        return listOf(
            Product(1, "Producto 1", "Descripción 1", 10.0),
            Product(2, "Producto 2", "Descripción 2", 20.0),
            Product(3, "Producto 3", "Descripción 3", 30.0),
        )
    }

    override suspend fun getProductById(id: Int): Product? {
        return getProducts().find { it.id == id }
    }
}
