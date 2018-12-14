package org.scc.samples.harrypotteronlinebookstore.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Book(

        @field:NotNull
        val id: Int,

        @field:NotNull
        @field:Size(min = 5, max = 200)
        val name: String,

        @field:NotNull
        val price: Price
)