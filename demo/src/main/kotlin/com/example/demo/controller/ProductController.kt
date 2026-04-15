package com.example.demo.controller

import com.example.demo.service.ProductService
import org.springframework.web.bind.annotation.*
import com.example.demo.dto.ProductCreateRequest
import com.example.demo.dto.StockIncreaseRequest

@RestController
@RequestMapping("/product")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) {
        val get = productService.getProduct(id)
        println(get)
    }

    @PostMapping("/{id}/decrease")
    fun decrease(
        @PathVariable id: Long,
        @RequestParam quantity: Int
    ) {
        productService.decreaseStockWithLock(id, quantity)
    }

    @PostMapping
    fun create(@RequestBody req: ProductCreateRequest) =
        productService.createProduct(req)

    @PostMapping("/{id}/increase")
    fun increase(
        @PathVariable id: Long,
        //@RequestBody req: StockIncreaseRequest
        @RequestParam quantity: Int
    ) {
        productService.increaseStockWithLock(id, quantity)
    }
}