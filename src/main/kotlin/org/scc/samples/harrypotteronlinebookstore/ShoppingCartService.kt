package org.scc.samples.harrypotteronlinebookstore

import org.scc.samples.harrypotteronlinebookstore.model.Book
import org.springframework.stereotype.Service

@Service
class ShoppingCartService {

    private fun organizeBooksIntoBundles(books: List<Book>): List<List<Book>> {
        return adjustBundles(books.fold(emptyList(), this::putBookIntoBundle))
    }

    private fun adjustBundles(bundles: List<List<Book>>): List<List<Book>> {
        val bundleWithThreeBooks = bundles.filter { it.size == 3 }.flatten()
        val bundleWithFiveBooks = bundles.filter { it.size == 5 }.flatten()

        return when {
            bundleWithThreeBooks.isNotEmpty() && bundleWithFiveBooks.isNotEmpty()
            -> reorderBundlesForBetterDiscount(bundleWithThreeBooks, bundleWithFiveBooks)
            else -> bundles
        }
    }

    private fun reorderBundlesForBetterDiscount(bundleWithThreeBooks: List<Book>, bundleWithFiveBooks: List<Book>): List<List<Book>> {
        @Suppress("UnnecessaryVariable", "explaining variable")
        val sameBooks = bundleWithThreeBooks
        val differentBooks = bundleWithFiveBooks - sameBooks
        val bundleOneWithFourBooks = bundleWithThreeBooks + differentBooks.last()
        val bundleTwoWithFourBooks = bundleWithFiveBooks - differentBooks.last()
        return listOf(bundleOneWithFourBooks, bundleTwoWithFourBooks)
    }

    private fun putBookIntoBundle(acc: List<List<Book>>, book: Book): List<List<Book>> {
        val foundSet = acc.find { booksAreDifferent(it.plus(book)) } ?: emptyList()
        val newAcc = when {
            foundSet.isEmpty() -> acc.plusElement(listOf(book))
            else -> acc.minusElement(foundSet).plusElement(foundSet.plusElement(book))
        }
        return newAcc.sortedByDescending { it.size }
    }


    fun getDiscountedPrice(books: List<Book>): Double =
            this.organizeBooksIntoBundles(books).map { it -> discountedPriceForSet(it) }.sum()


    private fun discountedPriceForSet(books: List<Book>): Double {
        val discount = getDiscount(books)
        val price = getFullPrice(books)
        return calculateDiscount(discount, price)
    }

    private fun getFullPrice(books: List<Book>): Double = books.fold(0.00) { acc, book -> acc + book.price.amount }

    private fun calculateDiscount(discount: Int, fullPrice: Double): Double = fullPrice - fullPrice * discount / 100

    private fun getDiscount(books: List<Book>): Int {
//        val discounts = arrayOf(0, 0, 5, 10, 20, 25)
//        return if (booksCouldBeDiscountable(books)) discounts[books.size] else 0

//        return if (booksCouldBeDiscountable(books)) when (books.size) {
//            2 -> 5
//            3 -> 10
//            4 -> 20
//            5 -> 25
//            else -> 0
//        } else 0

        val discounts = mapOf(Pair(2, 5), Pair(3, 10), Pair(4, 20), Pair(5, 25))
        return if (booksCouldBeDiscountable(books)) discounts.getOrDefault(books.size, 0) else 0

    }

    private fun booksCouldBeDiscountable(books: List<Book>) = booksAreDifferent(books)

    fun booksAreTheSame(books: List<Book>) =
            books.distinct().size == 1

    fun booksAreDifferent(books: List<Book>) =
            books.distinct().size == books.size

}