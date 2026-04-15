package com.example.demo.dto

data class ProductCreateRequest(
    val name: String,
    val price: Int,
    val stock: Int
)