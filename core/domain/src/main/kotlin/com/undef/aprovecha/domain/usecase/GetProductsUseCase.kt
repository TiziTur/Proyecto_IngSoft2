package com.undef.aprovecha.domain.usecase

import com.undef.aprovecha.domain.Product
import com.undef.aprovecha.domain.repository.ProductRepository

class GetProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<Product> = repository.getProducts()
}
