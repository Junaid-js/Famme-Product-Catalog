package dev.ja.fammecatalog.famme

import dev.ja.fammecatalog.product.ImportedProduct
import dev.ja.fammecatalog.product.ImportedVariant

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@Component
class FammeProductClient(
	restClientBuilder: RestClient.Builder,
	@Value("\${products.source-url}") sourceUrl: String,
) {
	private val restClient = restClientBuilder.baseUrl(sourceUrl).build()

	fun fetchProducts(limit: Int): List<ImportedProduct> {
		val response = restClient.get()
			.retrieve()
			.body(FammeProductsResponse::class.java)

		return response?.products.orEmpty()
			.take(limit)
			.map { product ->
				ImportedProduct(
					externalId = product.id,
					title = product.title.trim(),
					vendor = product.vendor.trim(),
					productType = product.productType.trim().ifBlank { "Other" },
					variants = product.variants.map { variant ->
						ImportedVariant(
							id = variant.id,
							title = variant.title,
							price = variant.price,
							sku = variant.sku,
						)
					},
				)
			}
	}
}

private data class FammeProductsResponse(
	val products: List<FammeProduct> = emptyList(),
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
private data class FammeProduct(
	val id: Long,
	val title: String,
	val vendor: String,
	val productType: String,
	val variants: List<FammeVariant> = emptyList(),
)

private data class FammeVariant(
	val id: Long,
	val title: String,
	val price: String,
	val sku: String?,
)
