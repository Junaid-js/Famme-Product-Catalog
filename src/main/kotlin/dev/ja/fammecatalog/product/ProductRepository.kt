package dev.ja.fammecatalog.product

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
	private val jdbcClient: JdbcClient,
) {
	fun findAll(query: String, productType: String): List<Product> {
		val sql = """
			SELECT id,
			       external_id,
			       title,
			       vendor,
			       product_type,
			       jsonb_array_length(variants) AS variant_count
			FROM products
			WHERE (
				:query = ''
				OR NOT EXISTS (
					SELECT 1
					FROM regexp_split_to_table(:query, '\s+') AS search_word
					WHERE title NOT ILIKE CONCAT('%', search_word, '%')
				)
			)
			  AND (:productType = '' OR product_type = :productType)
			ORDER BY title, id
		"""
		return jdbcClient.sql(sql)
			.param("query", query)
			.param("productType", productType)
			.query(Product::class.java)
			.list()
			.filterNotNull()
	}

	fun findById(id: Int): Product? {
		val sql = """
			SELECT id,
			       external_id,
			       title,
			       vendor,
			       product_type,
			       jsonb_array_length(variants) AS variant_count
			FROM products
			WHERE id = :id
		"""
		return jdbcClient.sql(sql)
			.param("id", id)
			.query(Product::class.java)
			.optional()
			.orElse(null)
	}

	fun findVariants(id: Int): List<ProductVariant> {
		val sql = """
			SELECT variant.data ->> 'title' AS title,
			       NULLIF(variant.data ->> 'sku', '') AS sku,
			       variant.data ->> 'price' AS price
			FROM products product
			CROSS JOIN LATERAL jsonb_array_elements(product.variants)
				WITH ORDINALITY AS variant(data, position)
			WHERE product.id = :id
			ORDER BY variant.position
		"""
		return jdbcClient.sql(sql)
			.param("id", id)
			.query(ProductVariant::class.java)
			.list()
			.filterNotNull()
	}

	fun findProductTypes(): List<String> {
		return jdbcClient.sql(
			"""
				SELECT DISTINCT product_type
				FROM products
				ORDER BY product_type
			"""
		)
			.query(String::class.java)
			.list()
			.filterNotNull()
	}

	fun insertImported(product: ImportedProduct, variantsJson: String): Int {
		val sql = """
			INSERT INTO products (external_id, title, vendor, product_type, variants)
			VALUES (:externalId, :title, :vendor, :productType, CAST(:variants AS jsonb))
			ON CONFLICT (external_id) DO NOTHING
		"""
		return jdbcClient.sql(sql)
			.param("externalId", product.externalId)
			.param("title", product.title)
			.param("vendor", product.vendor)
			.param("productType", product.productType)
			.param("variants", variantsJson)
			.update()
	}

	fun create(form: ProductForm, variantsJson: String): Int {
		val sql = """
			INSERT INTO products (title, vendor, product_type, variants)
			VALUES (:title, :vendor, :productType, CAST(:variants AS jsonb))
			RETURNING id
		"""
		return jdbcClient.sql(sql)
			.param("title", form.title)
			.param("vendor", form.vendor)
			.param("productType", form.productType)
			.param("variants", variantsJson)
			.query(Int::class.javaObjectType)
			.single()
	}

	fun update(id: Int, form: ProductForm, variantsJson: String): Int {
		val sql = """
			UPDATE products
			SET title = :title,
			    vendor = :vendor,
			    product_type = :productType,
			    variants = variants || CAST(:variants AS jsonb)
			WHERE id = :id
		"""
		return jdbcClient.sql(sql)
			.param("id", id)
			.param("title", form.title)
			.param("vendor", form.vendor)
			.param("productType", form.productType)
			.param("variants", variantsJson)
			.update()
	}

	fun delete(id: Int): Int {
		return jdbcClient.sql("DELETE FROM products WHERE id = :id")
			.param("id", id)
			.update()
	}
}
