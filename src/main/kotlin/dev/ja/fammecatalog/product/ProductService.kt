package dev.ja.fammecatalog.product

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper

@Service
class ProductService(
	private val productRepository: ProductRepository,
	private val jsonMapper: JsonMapper,
	private val productEventBroadcaster: ProductEventBroadcaster,
) {
	@Transactional(readOnly = true)
	fun findProducts(query: String?, productType: String?): List<Product> {
		return productRepository.findAll(query?.trim().orEmpty(), productType?.trim().orEmpty())
	}

	@Transactional(readOnly = true)
	fun findProductTypes(): List<String> = productRepository.findProductTypes()

	@Transactional(readOnly = true)
	fun findProduct(id: Int): Product {
		return productRepository.findById(id) ?: throw NoSuchElementException("Product $id was not found")
	}

	@Transactional(readOnly = true)
	fun findProductVariants(id: Int): List<ProductVariant> = productRepository.findVariants(id)

	@Transactional
	fun importProducts(products: List<ImportedProduct>): Int {
		return products.sumOf { product ->
			productRepository.insertImported(product, jsonMapper.writeValueAsString(product.variants))
		}
	}

	@Transactional
	fun createProduct(form: ProductForm, actorClientId: String?): Int {
		val normalizedForm = form.normalized()
		val id = productRepository.create(
			normalizedForm,
			jsonMapper.writeValueAsString(normalizedForm.variants),
		)
		productEventBroadcaster.publish(ProductEvent(ProductEventType.CREATED, id, normalizedForm.title, actorClientId))
		return id
	}

	@Transactional
	fun updateProduct(id: Int, form: ProductForm, actorClientId: String?) {
		val normalizedForm = form.normalized()
		val variantsJson = jsonMapper.writeValueAsString(normalizedForm.variants)
		if (productRepository.update(id, normalizedForm, variantsJson) == 0) {
			throw NoSuchElementException("Product $id was not found")
		}
		productEventBroadcaster.publish(ProductEvent(ProductEventType.UPDATED, id, normalizedForm.title, actorClientId))
	}

	@Transactional
	fun deleteProduct(id: Int, actorClientId: String?) {
		val product = productRepository.findById(id) ?: throw NoSuchElementException("Product $id was not found")
		if (productRepository.delete(id) == 0) {
			throw NoSuchElementException("Product $id was not found")
		}
		productEventBroadcaster.publish(ProductEvent(ProductEventType.DELETED, id, product.title, actorClientId))
	}

	private fun ProductForm.normalized(): ProductForm {
		return copy(
			title = title.trim(),
			vendor = vendor.trim(),
			productType = productType.trim(),
			variants = variants.map { it.normalized() },
		)
	}

	private fun ProductVariantForm.normalized(): ProductVariantForm {
		return copy(
			title = title.trim(),
			sku = sku.trim(),
			price = price.trim(),
		)
	}
}
