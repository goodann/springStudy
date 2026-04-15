package com.example.demo.service

import com.example.demo.domain.Product
import com.example.demo.dto.ProductCreateRequest
import com.example.demo.repository.ProductRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun getProduct(id: Long): Product? {
        val key = "product:$id"

        // 1. 캐시 조회
        val cached = redisTemplate.opsForValue().get(key)
        if (cached != null) {
            println("🔥 cache hit")
            return null // (단순화, 실제는 deserialize 필요)
        }

        // 2. DB 조회
        val product = productRepository.findById(id).orElse(null)

        // 3. 캐시 저장
        if (product != null) {
            redisTemplate.opsForValue().set(key, product.toString())
        }

        return product
    }

    @Transactional
    fun decreaseStock(id: Long, quantity: Int) {
        val product = productRepository.findById(id)
            .orElseThrow { RuntimeException("상품 없음") }

        if (product.stock < quantity) {
            throw RuntimeException("재고 부족")
        }

        product.stock -= quantity
    }

    fun decreaseStockWithLock(id: Long, quantity: Int) {
        val lockKey = "lock:product:$id"

        val isLocked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "lock", java.time.Duration.ofSeconds(3))

        if (isLocked != true) {
            throw RuntimeException("다른 요청 처리중")
        }

        try {
            decreaseStock(id, quantity)
        } finally {
            redisTemplate.delete(lockKey)
        }
    }
    // 상품 생성
    @Transactional
    fun createProduct(req: ProductCreateRequest): Product {
        val product = Product(
            name = req.name,
            price = req.price,
            stock = req.stock
        )

        return productRepository.save(product)
    }

    // 재고 증가 (기본)
    @Transactional
    fun increaseStock(id: Long, quantity: Int) {
        val product = productRepository.findById(id)
            .orElseThrow { RuntimeException("상품 없음") }

        product.stock += quantity
    }

    fun increaseStockWithLock(id: Long, quantity: Int) {
        val lockKey = "lock:product:$id"

        val isLocked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "lock", java.time.Duration.ofSeconds(3))

        if (isLocked != true) {
            throw RuntimeException("다른 요청 처리중")
        }

        try {
            increaseStock(id, quantity)
        } finally {
            redisTemplate.delete(lockKey)
        }
    }
}
