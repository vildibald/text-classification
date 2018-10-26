package sk.vildibald.text.classification.data.entities

import java.time.LocalDate

data class News(val category: String,
                val date: LocalDate,
                val snippet: String,
                val content: String)