package com.undef.aprovecha.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.undef.aprovecha.domain.Product
import com.undef.aprovecha.domain.usecase.GetProductsUseCase

class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = ProductsUiState.Loading
                val products = getProductsUseCase()
                _uiState.value = ProductsUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = ProductsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ProductsUiState {
    data object Loading : ProductsUiState()
    data class Success(val products: List<Product>) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}
