package org.scc.samples.harrypotteronlinebookstore

import org.scc.samples.harrypotteronlinebookstore.model.Books
import org.scc.samples.harrypotteronlinebookstore.model.CurrencyType
import org.scc.samples.harrypotteronlinebookstore.model.Price
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import javax.validation.ConstraintViolationException
import javax.validation.constraints.Max
import javax.validation.constraints.Min


@RestController
@RequestMapping("/shop")
@Validated
class ShopController {

    @Autowired
    private lateinit var shoppingCartService: ShoppingCartService

    @Autowired
    private lateinit var books: Books

    @GetMapping("/greeting", produces = ["text/plain;charset=UTF-8"])
    fun greeting(
            @RequestParam(value = "name", defaultValue = "World")
            name: String
    ) = "Hello, $name"

    @GetMapping("/books", produces = ["application/json;charset=UTF-8"])
    fun getAllBooks() = books.allBooks

    @GetMapping("books/price", produces = ["application/json;charset=UTF-8"])
    fun getPriceForBooks(
            @RequestParam(value = "bookids", required = true)
            bookIds: List<Int>
    ) = Price(
            shoppingCartService.getDiscountedPrice(bookIds.flatMap { bookId -> bookWithId(bookId) }),
            CurrencyType.EURO
    )

    @GetMapping("/book/{id}", produces = ["application/json;charset=UTF-8"])
    fun getBookWithId(
            @PathVariable(value = "id")
            @Min(1)
            @Max(5)
            id: Int
    ) = bookWithId(id)

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, request: WebRequest): ResponseEntity<Any> =
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(ex: MissingServletRequestParameterException, request: WebRequest): ResponseEntity<Any> =
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)

    private fun bookWithId(id: Int) = Books().allBooks.filter { it.id == id }

}


//    @PostMapping("/shopping-cart"){
//
//    }

