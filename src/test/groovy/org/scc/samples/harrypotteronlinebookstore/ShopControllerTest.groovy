package org.scc.samples.harrypotteronlinebookstore

import org.scc.samples.harrypotteronlinebookstore.model.Book
import org.scc.samples.harrypotteronlinebookstore.model.Books
import org.scc.samples.harrypotteronlinebookstore.model.CurrencyType
import org.scc.samples.harrypotteronlinebookstore.model.Price
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
class ShopControllerTest extends Specification {

    @Shared
    def allBooks = new Books().getAllBooks()

    @SpringBean
    ShoppingCartService shoppingCartService = Mock(ShoppingCartService)

    @SpringBean
    Books books = Mock(Books)

    @Autowired
    ShopController shopController


    def "Greeting"() {
        when:
        def result = shopController.greeting("hallo")

        then:
        result == "Hello, hallo"
    }

    def "the price for a list of book ids is calculated"() {
        when:
        def result = shopController.getPriceForBooks([1, 2])

        then:
        1 * shoppingCartService.getDiscountedPrice([allBooks.get(0), allBooks.get(1)]) >> 4
        result == new Price(4, CurrencyType.EURO)
    }

    def "get all informations of all books"() {
        given: "a fake list of books"
        def fakeBooks = new Books([new Book(1, "1", new Price(1, CurrencyType.EURO))])

        when: "I want to get all books"
        def result = shopController.getAllBooks()

        then: "I get all the books"
        1*books.allBooks >> fakeBooks.allBooks
        result == fakeBooks.allBooks
    }
}
