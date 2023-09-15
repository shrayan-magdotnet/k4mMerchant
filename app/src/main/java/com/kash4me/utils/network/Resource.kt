package com.kash4me.utils.network

sealed class Resource<out T> {
    class Success<T>(val value: T) : Resource<T>()
    class Failure(val errorMsg: String, val exception: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}