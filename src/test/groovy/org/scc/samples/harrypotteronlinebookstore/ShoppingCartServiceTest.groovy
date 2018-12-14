package org.scc.samples.harrypotteronlinebookstore

import org.scc.samples.harrypotteronlinebookstore.model.Book
import org.scc.samples.harrypotteronlinebookstore.model.Books
import org.scc.samples.harrypotteronlinebookstore.model.CurrencyType
import org.scc.samples.harrypotteronlinebookstore.model.Price
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ShoppingCartServiceTest extends Specification {


    @Shared
    def allBooks = new Books().getAllBooks()

    @Shared
    def shoppingCartService = new ShoppingCartService()


    @Shared
    def bookOne, bookTwo, bookThree, bookFour, bookFive


    void setup() {
    }

    def setupSpec() {
        reportHeader "<h2>Every Spring HarryPotter´s BookStore gets a visit from Spock</h2>"
        bookOne = allBooks[0]
        bookTwo = allBooks[1]
        bookThree = allBooks[2]
        bookFour = allBooks[3]
        bookFive = allBooks[4]

    }

    def "a book has a specific price"() {
        given: "Harry´s first book"
        def book = bookOne

        expect: "the price for the book is 8"
        book.price.amount == 8
    }

    @Unroll
    def "a cart of books has different books, #booksAreDifferentExpected"() {
        when: "allBooks are different"
        def booksAreDifferent = shoppingCartService.booksAreDifferent(newbooks)

        then: "allBooks should be as expected"
        booksAreDifferent == booksAreDifferentExpected

        where:
        newbooks                             | booksAreDifferentExpected
        []                                   | true
        [bookOne]                            | true
        [bookOne, bookOne]                   | false
        allBooks                             | true
        [allBooks.first(), allBooks.first()] | false
        [bookOne, bookTwo]                   | true
        [bookOne, bookTwo, bookThree]        | true
    }

    @Unroll
    def "a cart of books contains only copies of the same book #booksAreTheSameExpected"() {
        when: "allBooks are the same"
        def result = shoppingCartService.booksAreTheSame(books)

        then: "allBooks should be as expected"
        result == booksAreTheSameExpected

        where: "books in carts are same as expected"
        books                         | booksAreTheSameExpected
        [bookOne, bookOne, bookOne]   | true
        [bookOne, bookTwo, bookTwo]   | false
        [bookOne, bookTwo, bookThree] | false
        []                            | false
        [bookOne]                     | true
    }


    @Unroll
    def "cart of books are the same #newbooks, #booksAreTheSameExpected"() {
        when:
        def booksAreDifferent = shoppingCartService.booksAreTheSame(newbooks)

        then:
        booksAreDifferent == booksAreTheSameExpected

        where:
        newbooks                                                                          | booksAreTheSameExpected
        [bookOne, new Book(1, "der Stein der Weisen", new Price(8.0, CurrencyType.EURO))] | true
    }

    def "a cart of two identical books are not discounted"() {
        given:
        def books = [bookOne, bookOne]

        when:
        def discount = shoppingCartService.getDiscount(books)

        then:
        discount == 0
    }

    @Unroll
    def "a cart of books that contains copies of the same book does not receive a discount"() {
        given:
        def books = [bookOne, bookTwo, bookTwo]

        when:
        def discount = shoppingCartService.getDiscount(books)

        then:
        discount == 0
    }

    @Unroll
    def "a cart of #numberOfBooks different books are discounted with #discount%"() {
        given: "cartWithNumberOfBooks of #numberOfBooks books"
        def books = allBooks.subList(0, numberOfBooks) + []

        when:
        def discountResult = shoppingCartService.getDiscount(books)

        then:
        discountResult == discount

        where:
        numberOfBooks | discount
        0             | 0
        1             | 0
        2             | 5
        3             | 10
        4             | 20
        5             | 25
    }

    @Unroll
    def "applying discount of #discount% on full price of #fullPrice results in discounted price of #discountedPriceExpected"() {
        when:
        def discountedPrice = shoppingCartService.calculateDiscount(discount, fullPrice)

        then:
        discountedPrice == discountedPriceExpected

        where:
        discount | fullPrice | discountedPriceExpected
        0        | 10        | 10
        5        | 10        | 9.5
        10       | 10        | 9
        20       | 10        | 8
        25       | 10        | 7.5
        0        | 8         | 8
        5        | 8         | 7.6
        10       | 8         | 7.2
        20       | 8         | 6.4
        25       | 8         | 6
    }

    @Unroll
    def "a cart of books #books.size has a full price of #expectedFullPrice"() {
        when:
        def fullPrice = shoppingCartService.getFullPrice(books)

        then:
        fullPrice == expectedFullPrice

        where:
        books                       | expectedFullPrice
        []                          | 0
        [bookOne]                   | 8
        [bookOne, bookOne]          | 16
        [bookOne, bookTwo]          | 16
        [bookOne, bookOne, bookTwo] | 24
        allBooks + []               | 40
    }

    @Unroll
    def "group various books into bundles, each bundle containing different books"() {
        when: "books are organized"
        def cart = shoppingCartService.organizeBooksIntoBundles books as List<Book>

        then:
        cart == expectedcartsOfBooks

        and:
        cart.size() == sizeExpected

        and:
        cart.size() < 1 || cart.every { shoppingCartService.booksAreDifferent(it) }

        where:
        books << [
                [],
                [bookOne],
                [bookOne, bookTwo],
                [bookOne] + [bookOne],
                [bookOne] + [bookOne, bookTwo],
                [bookOne, bookOne, bookTwo, bookOne, bookThree, bookTwo]
        ]

        expectedcartsOfBooks << [
                [],
                [[new Book(1, "der Stein der Weisen", new Price(8, CurrencyType.EURO))]],
                [[bookOne, bookTwo]],
                [[bookOne], [bookOne]],
                [[bookOne, bookTwo], [bookOne]],
                [[bookOne, bookTwo, bookThree], [bookOne, bookTwo], [bookOne]]
        ]

        sizeExpected << [0, 1, 1, 2, 2, 3]
    }

    @Unroll
    def "cart should rearrange in to bundles of 4 books, to give higher discount"() {

        when: "both carts are put in the cart"
        def cart = shoppingCartService.organizeBooksIntoBundles bundleThree + bundleFive as List<Book>

        then: "should be one cart with book 1 to 4"
        cart[0].sort() == bundleFourOne.sort()

        and: "on cart with book 2 to 5"
        cart[1].sort() == bundleForTwo.sort()


        where: "given bundle of three books and bundle of five books matches the expected bundles of four books"
        bundleThree << [
                [bookOne, bookTwo, bookThree],
                [bookThree, bookFour, bookFive],
                [bookTwo, bookThree, bookFour]
        ]

        bundleFive << [
                allBooks,
                allBooks,
                allBooks
        ]
        bundleFourOne << [
                bundleThree + [bookFive],
                [bookTwo] + bundleThree,
                bundleThree + [bookFive]
        ]
        bundleForTwo << [
                bundleThree + [bookFour],
                [bookOne] + bundleThree,
                [bookOne] + bundleThree
        ]

    }


    @Unroll
    def "a specific cart of books costs #price after discount"() {

        given: "a cart of books"
        def cartOfBooks = copiesOfBookOne(book1) + copiesOfBookTwo(book2) + copiesOfBookThree(book3) + copiesOfBookFour(book4) + copiesOfBookFive(book5)

        when: "the price for the cartWithNumberOfBooks is calculated"
        def discountedPrice = shoppingCartService.getDiscountedPrice cartOfBooks

        then: "the price should be #price"
        discountedPrice == price

        where: "given books matches the expected price"
        book1 | book2 | book3 | book4 | book5 | price
        1     | 0     | 0     | 0     | 0     | 8
        1     | 1     | 0     | 0     | 0     | 15.2
        1     | 1     | 1     | 0     | 0     | 21.6
        1     | 1     | 1     | 1     | 0     | 25.6
        1     | 1     | 1     | 1     | 1     | 30
        2     | 0     | 0     | 0     | 0     | 16
        2     | 1     | 0     | 0     | 0     | 23.2
        2     | 1     | 1     | 0     | 0     | 29.6
        2     | 2     | 2     | 1     | 1     | 51.2

    }


    @Unroll
    def "get copies #copies of the same book #bookId"() {
        when:
        def books = copiesOfBook copies, bookId

        then:
        books == expectedBooks

        where:
        copies | bookId | expectedBooks
        0      | 0      | []
        0      | 1      | []
        1      | 1      | [allBooks.first()]
        1      | 2      | [bookTwo]
        2      | 2      | [bookTwo, bookTwo]
    }


    def copiesOfBookOne = { int copies ->
        copiesOfBook copies, 1
    }

    def copiesOfBookTwo = { int copies ->
        copiesOfBook copies, 2
    }

    def copiesOfBookThree = { int copies ->
        copiesOfBook copies, 3
    }

    def copiesOfBookFour = { int copies ->
        copiesOfBook copies, 4
    }

    def copiesOfBookFive(int copies) {
        copiesOfBook(copies, 5)
    }

    def copiesOfBook = { int copies, int bookId ->
        copies > 0 ? (1..copies).collect {
            allBooks.find { it.id == bookId }
        } : []
    }

}
