package controllers

import com.sun.net.httpserver.HttpExchange
import domain.entities.Customer
import domain.entities.Position
import services.CustomerService
import utils.JsonUtil

class CustomerController : Controller() {
    private val customerService = registry.inject("customerService") as CustomerService

    override fun handle(exchange: HttpExchange) {
        try {
            when (exchange.requestMethod) {
                "GET" -> handleGetAll(exchange)
                "POST" -> handleCreate(exchange)
                "PUT" -> handlePut(exchange)
                "DELETE" -> handleDelete(exchange)
                else -> {
                    exchange.sendResponseHeaders(405, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respond(exchange, 500, """{"error": "${e.message}"}""")
        }
    }

    private fun handleGetAll(exchange: HttpExchange) {
        val customers = customerService.getAll()
        val response = JsonUtil.toJson(customers)
        respond(exchange, 200, response)
    }

    private fun handleCreate(exchange: HttpExchange) {
        val body = exchange.requestBody.reader().readText()
        val position = JsonUtil.fromJson<Position>(body)

        customerService.create(Customer.create(position))
        respond(exchange, 201, "Customer created")
    }

    private fun handlePut(exchange: HttpExchange) {
        val requestBody = exchange.requestBody.reader().readText()
        val customer = JsonUtil.fromJson<Customer>(requestBody)
        customerService.update(customer.id, customer.position.x, customer.position.y)
        respond(exchange, 200, """{"message": "Customer updated"}""")
    }

    private fun handleDelete(exchange: HttpExchange) {
        val id = exchange.requestURI.query?.split("=")?.getOrNull(1)?.toIntOrNull()
        if (id == null) {
            respond(exchange, 400, "Invalid ID")
            return
        }
        customerService.delete(id.toString())
        respond(exchange, 200, "Customer deleted")
    }

    private fun respond(exchange: HttpExchange, statusCode: Int, response: String) {
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
        exchange.close()
    }
}
