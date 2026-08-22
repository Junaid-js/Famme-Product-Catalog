package dev.ja.fammecatalog.famme

import dev.ja.fammecatalog.product.ProductService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private const val PRODUCT_IMPORT_LIMIT = 50

@Component
class ProductImportJob(
	private val fammeProductClient: FammeProductClient,
	private val productService: ProductService,
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@Scheduled(initialDelay = 0, fixedDelay = 24, timeUnit = TimeUnit.HOURS)
	fun importProducts() {
		val products = fammeProductClient.fetchProducts(PRODUCT_IMPORT_LIMIT)
		val insertedProducts = productService.importProducts(products)
		logger.info("Famme product import fetched {} and inserted {} products", products.size, insertedProducts)
	}
}
