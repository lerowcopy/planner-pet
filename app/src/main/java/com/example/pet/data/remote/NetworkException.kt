package com.example.pet.data.remote

/**
 * Custom exception for network errors.
 * Provides detailed information about HTTP errors.
 */
sealed class NetworkException(message: String) : Exception(message) {
    /**
     * HTTP error with status code and message.
     */
    data class HttpError(
        val code: Int,
        val errorMessage: String
    ) : NetworkException("HTTP $code: $errorMessage")
    
    /**
     * Network connectivity error.
     */
    data class NetworkError(val errorMessage: String) : NetworkException("Network error: $errorMessage")
    
    /**
     * Timeout error.
     */
    object TimeoutError : NetworkException("Request timeout")
    
    /**
     * Unknown error.
     */
    data class UnknownError(val errorMessage: String) : NetworkException("Unknown error: $errorMessage")
}
