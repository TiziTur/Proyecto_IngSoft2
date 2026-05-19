package com.undef.aprovecha.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.undef.aprovecha.domain.Product

@Composable
fun ProductsScreen(
    uiState: ProductsUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is ProductsUiState.Loading -> {
            CircularProgressIndicator(modifier = modifier)
        }
        is ProductsUiState.Success -> {
            ProductsList(products = uiState.products, modifier = modifier)
        }
        is ProductsUiState.Error -> {
            Text(text = "Error: ${uiState.message}", modifier = modifier)
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(products) { product ->
            ProductCard(product = product)
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name)
            Text(text = product.description)
            Text(text = "$${product.price}")
        }
    }
}
