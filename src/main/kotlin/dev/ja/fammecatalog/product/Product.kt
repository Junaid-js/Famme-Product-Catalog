package dev.ja.fammecatalog.product

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class Product(
	val id: Int,
	val externalId: Long?,
	val title: String,
	val vendor: String,
	val productType: String,
	val variantCount: Int,
)

data class ProductVariant(
	val title: String,
	val sku: String?,
	val price: String,
)

data class ProductForm(
	@field:NotBlank(message = "Title is required")
	@field:Size(max = 200, message = "Title must be 200 characters or fewer")
	val title: String = "",
	@field:NotBlank(message = "Vendor is required")
	@field:Size(max = 120, message = "Vendor must be 120 characters or fewer")
	val vendor: String = "",
	@field:NotBlank(message = "Product type is required")
	@field:Size(max = 120, message = "Product type must be 120 characters or fewer")
	val productType: String = "",
	val variants: List<@Valid ProductVariantForm> = emptyList(),
)

data class ProductVariantForm(
	@field:NotBlank(message = "Variant title is required")
	@field:Size(max = 200, message = "Variant title must be 200 characters or fewer")
	val title: String = "",
	@field:Size(max = 120, message = "SKU must be 120 characters or fewer")
	val sku: String = "",
	@field:NotBlank(message = "Price is required")
	@field:Pattern(
		regexp = "^\\d+(\\.\\d{1,2})?$",
		message = "Price must be a valid amount with up to 2 decimal places",
	)
	val price: String = "",
)

data class ImportedProduct(
	val externalId: Long,
	val title: String,
	val vendor: String,
	val productType: String,
	val variants: List<ImportedVariant>,
)

data class ImportedVariant(
	val id: Long,
	val title: String,
	val price: String,
	val sku: String?,
)
