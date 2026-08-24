package dev.ja.fammecatalog.product

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@Component
class ProductEventBroadcaster(
	private val jsonMapper: JsonMapper,
) {
	private val emitters = CopyOnWriteArrayList<SseEmitter>()
	private val logger = LoggerFactory.getLogger(ProductEventBroadcaster::class.java)

	fun subscribe(): SseEmitter {
		val emitter = SseEmitter(SSE_TIMEOUT_MILLIS)
		emitters += emitter
		emitter.onCompletion { emitters -= emitter }
		emitter.onTimeout { emitter.complete() }
		emitter.onError { emitters -= emitter }
		return emitter
	}

	fun publish(event: ProductEvent) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
				override fun afterCommit() = send(event)
			})
		} else {
			send(event)
		}
	}

	private fun send(event: ProductEvent) {
		val payload = jsonMapper.writeValueAsString(event)
		emitters.forEach { emitter ->
			try {
				emitter.send(SseEmitter.event().name(event.type).data(payload))
			} catch (exception: IOException) {
				logger.debug("Dropping stale SSE subscriber", exception)
				emitters -= emitter
			}
		}
	}

	private companion object {
		const val SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L
	}
}

object ProductEventType {
	const val CREATED = "product-created"
	const val UPDATED = "product-updated"
	const val DELETED = "product-deleted"
}

data class ProductEvent(
	val type: String,
	val productId: Int,
	val title: String,
	val actorClientId: String?,
)
