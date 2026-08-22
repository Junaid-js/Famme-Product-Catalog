package dev.ja.fammecatalog.product

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
@RequestMapping("/product")
class ProductController(
	private val productService: ProductService,
) {
	@GetMapping
	fun products(): String = "products"

	@GetMapping("/table")
	fun productTable(model: Model): String {
		addProductList(model, query = null, productType = null, tableContext = OVERVIEW_CONTEXT)
		return PRODUCT_TABLE_FRAGMENT
	}

	@GetMapping("/new")
	fun newProduct(model: Model): String {
		model.addAttribute("productForm", ProductForm())
		return "product-new"
	}

	@PostMapping
	fun createProduct(
		@Valid @ModelAttribute("productForm") productForm: ProductForm,
		bindingResult: BindingResult,
		model: Model,
	): String {
		if (!bindingResult.hasErrors()) {
			val productId = productService.createProduct(productForm)
			model.addAttribute("productForm", ProductForm())
			model.addAttribute("successMessage", "Product added")
			model.addAttribute("createdProductId", productId)
			model.addAttribute("createdProductTitle", productForm.title.trim())
		}
		return PRODUCT_CREATE_FRAGMENT
	}

	@GetMapping("/search")
	fun searchPage(
		@RequestParam(required = false) query: String?,
		@RequestParam(required = false) productType: String?,
		model: Model,
	): String {
		addProductList(model, query, productType, tableContext = SEARCH_CONTEXT)
		return "product-search"
	}

	@GetMapping("/search/results")
	fun searchResults(
		@RequestParam(required = false) query: String?,
		@RequestParam(required = false) productType: String?,
		model: Model,
	): String {
		addProductList(model, query, productType, tableContext = SEARCH_CONTEXT)
		return PRODUCT_TABLE_FRAGMENT
	}

	@GetMapping("/{id}/variants")
	fun productVariants(@PathVariable id: Int, model: Model): String {
		val product = productService.findProduct(id)
		val variants = productService.findProductVariants(id)
		model.addAttribute("product", product)
		model.addAttribute("variants", variants)
		model.addAttribute("hasVariants", variants.isNotEmpty())
		return PRODUCT_VARIANTS_FRAGMENT
	}

	@GetMapping("/{id}")
	fun editProduct(@PathVariable id: Int, model: Model): String {
		val product = productService.findProduct(id)
		addEditModel(model, product, ProductForm(product.title, product.vendor, product.productType))
		return "product"
	}

	@PutMapping("/{id}")
	fun updateProduct(
		@PathVariable id: Int,
		@Valid @ModelAttribute("productForm") productForm: ProductForm,
		bindingResult: BindingResult,
		model: Model,
	): String {
		if (bindingResult.hasErrors()) {
			addEditModel(model, productService.findProduct(id), productForm)
			return PRODUCT_EDIT_FRAGMENT
		}

		productService.updateProduct(id, productForm)
		val product = productService.findProduct(id)
		addEditModel(model, product, ProductForm(product.title, product.vendor, product.productType))
		model.addAttribute("successMessage", "Product updated")
		return PRODUCT_EDIT_FRAGMENT
	}

	@DeleteMapping("/{id}")
	fun deleteProduct(
		@PathVariable id: Int,
		@RequestParam(required = false) query: String?,
		@RequestParam(required = false) productType: String?,
		@RequestParam(defaultValue = OVERVIEW_CONTEXT) tableContext: String,
		model: Model,
	): String {
		productService.deleteProduct(id)
		addProductList(model, query, productType, normalizedTableContext(tableContext))
		return PRODUCT_TABLE_FRAGMENT
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoSuchElementException::class)
	fun productNotFound(exception: NoSuchElementException): String = exception.message ?: "Product not found"

	private fun addProductList(model: Model, query: String?, productType: String?, tableContext: String) {
		val normalizedQuery = query?.trim().orEmpty()
		val normalizedProductType = productType?.trim().orEmpty()
		val products = productService.findProducts(normalizedQuery, normalizedProductType)
		model.addAttribute("products", products)
		model.addAttribute("productTypes", productService.findProductTypes())
		model.addAttribute("query", normalizedQuery)
		model.addAttribute("selectedProductType", normalizedProductType)
		model.addAttribute("hasProducts", products.isNotEmpty())
		model.addAttribute("resultCount", products.size)
		model.addAttribute("tableContext", tableContext)
	}

	private fun addEditModel(model: Model, product: Product, form: ProductForm) {
		model.addAttribute("product", product)
		model.addAttribute("productForm", form)
		model.addAttribute("sourceLabel", if (product.externalId == null) "Manual" else "Famme")
	}

	private fun normalizedTableContext(tableContext: String): String {
		return if (tableContext == SEARCH_CONTEXT) SEARCH_CONTEXT else OVERVIEW_CONTEXT
	}

	private companion object {
		const val PRODUCT_TABLE_FRAGMENT = "fragments/product-table :: productTable"
		const val PRODUCT_VARIANTS_FRAGMENT = "fragments/product-variants :: productVariants"
		const val PRODUCT_CREATE_FRAGMENT = "product-new :: createForm"
		const val PRODUCT_EDIT_FRAGMENT = "product :: editForm"
		const val OVERVIEW_CONTEXT = "overview"
		const val SEARCH_CONTEXT = "search"
	}
}
