package dev.ja.fammecatalog.product

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper

@Service
class ProductService(
	private val productRepository: ProductRepository,
	private val jsonMapper: JsonMapper,
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
	fun createProduct(form: ProductForm): Int {
		val normalizedForm = form.normalized()
		return productRepository.create(
			normalizedForm,
			jsonMapper.writeValueAsString(normalizedForm.variants),
		)
	}

	@Transactional
	fun updateProduct(id: Int, form: ProductForm) {
		val normalizedForm = form.normalized()
		val variantsJson = jsonMapper.writeValueAsString(normalizedForm.variants)
		if (productRepository.update(id, normalizedForm, variantsJson) == 0) {
			throw NoSuchElementException("Product $id was not found")
		}
	}

	@Transactional
	fun deleteProduct(id: Int) {
		if (productRepository.delete(id) == 0) {
			throw NoSuchElementException("Product $id was not found")
		}
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
