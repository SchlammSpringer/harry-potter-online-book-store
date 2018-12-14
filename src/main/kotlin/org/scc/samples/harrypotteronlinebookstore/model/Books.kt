package org.scc.samples.harrypotteronlinebookstore.model

import org.springframework.stereotype.Component

@Component
data class Books(
        var allBooks: List<Book> =
                         listOf(
                                 Book(1, "der Stein der Weisen", Price(8.0, CurrencyType.EURO)),
                                 Book(2, "die Kammer des Schreckens", Price(8.0,CurrencyType.EURO)),
                                 Book(3, "der Gefangene von Askaban", Price(8.0, CurrencyType.EURO)),
                                 Book(4, "der Feuerkelch", Price(8.0, CurrencyType.EURO)),
                                 Book(5, "der Orden des Phönix", Price(8.0, CurrencyType.EURO))
                         )

)