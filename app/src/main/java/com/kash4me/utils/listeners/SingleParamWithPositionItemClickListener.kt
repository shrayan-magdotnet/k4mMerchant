package com.kash4me.utils.listeners

interface SingleParamWithPositionItemClickListener<T> {

    fun onClick(item: T, position: Int)

}