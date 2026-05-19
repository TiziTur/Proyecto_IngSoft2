package com.aprovecha.app.common.util

/**
 * Wrapper genérico para resultados de operaciones asíncronas.
 * Utilizado en todos los UseCases y Repositorios del proyecto.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/** Retorna true si el resultado es exitoso */
val <T> Result<T>.isSuccess: Boolean get() = this is Result.Success

/** Retorna true si el resultado es un error */
val <T> Result<T>.isError: Boolean get() = this is Result.Error

/** Obtiene el dato si es Success, null en otro caso */
fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data
