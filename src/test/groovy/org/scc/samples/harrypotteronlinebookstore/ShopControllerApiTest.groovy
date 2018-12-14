package org.scc.samples.harrypotteronlinebookstore

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.scc.samples.harrypotteronlinebookstore.model.Book
import org.scc.samples.harrypotteronlinebookstore.model.Books
import org.scc.samples.harrypotteronlinebookstore.model.Price
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.scc.samples.harrypotteronlinebookstore.model.CurrencyType.EURO
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import static org.springframework.http.MediaType.TEXT_PLAIN
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [ShopController.class])
class ShopControllerApiTest extends Specification {

    @Autowired
    private MockMvc mvc

    @SpringBean
    ShoppingCartService shoppingCartService = Mock(ShoppingCartService)

    @SpringBean
    Books books = Mock(Books)


    private slurper = new JsonSlurper()

    def "first api Test"() {
        when:
        def response = mvc.perform(
                get("/shop/greeting?name=test")
                        .accept(TEXT_PLAIN)
        ).andReturn().response

        then:
        response.status == 200
        and:
        response.contentAsString == "Hello, test"
    }

    def "a list of all books in the inventory should be listed"() {

        given: "a fake inventory"
        def fakeInventory = new Books([
                new Book(1, "1", new Price(1, EURO)),
                new Book(2, "2", new Price(2, EURO))
        ])

        when: "a consumer wants to get all books"
        def response = mvc
                .perform(get("/shop/books").accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().response

        then:
        1 * books.allBooks >> fakeInventory.allBooks
        //Make Groovy LazyMap for better comparable
        // (do not want to test some object serializer implicit, or take care of properties order in JSON structure)
        slurper.parseText(response.contentAsString) == slurper.parseText(JsonOutput.toJson(fakeInventory.allBooks))
    }

    def "a specific price for a cart of books should be delivered"() {

        given: "a fake Price for a shopping cart"
        def fakePrice = new Price(10, EURO)

        and: "the ids of the books"
        def bookIds = [1, 2, 1, 2, 3, 3, 4, 5]
        String idsForUrl = bookIds.join(",")

        when: "a consumer wants to get a price for her/his cart"
        def response = mvc
                .perform(get("/shop/books/price?bookids=$idsForUrl").accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().response

        then:
        1 * shoppingCartService.getDiscountedPrice(_) >> fakePrice.amount
        //Make Groovy LazyMap for better comparable
        // (do not want to test some object serializer implicit, or take care of properties order in JSON structure)
        this.slurper.parseText(response.contentAsString) == this.slurper.parseText(JsonOutput.toJson(fakePrice))

    }

    def "a Bad Request should be delivered if no book id is part of the price call"() {

        when: "a consumer wants to get a price for her/his cart but without a book id"
        def response = mvc
                .perform(get("/shop/books/price").accept(APPLICATION_JSON_UTF8))
                .andReturn().response

        then:
        0 * shoppingCartService.getDiscountedPrice(_)
        response.status == HttpStatus.BAD_REQUEST.value()

    }


}
