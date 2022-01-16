package org.thiagoelias

import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val action: String?
        val input : String?
        val output : String?
        val pagesToRemove: String?

        try {
            action = args[0]
            input = args[1]

            if (action == "page-number") {
                val qtPages = getPages(input)
                println("Pages: $qtPages")
                return
            }
            if (action == "remove-pages") {
                output = args[2]
                pagesToRemove = args[3]

                if (removePages(input, output, pagesToRemove)) {
                    println("Pages were removed and saved on the output path")
                } else {
                    println("An error ocurred while trying to remove pages from input document")
                }
                return;
            }
            println("Usage: java pdftools <action> <input> <output?> <pagesToRemove?>")
        } catch (e: IOException) {
            return
        } catch (ex: Exception) {
            println("An error occurred: ${ex.message}")
        }
    }

    private fun getPages(input: String): Int {
        var document = PDDocument()
        var qtPages = 0
        try {
            document = PDDocument.load(File(input))
            qtPages = document.numberOfPages
        } catch (e: IOException) {
            println("The specified file couldn't be found.")
            e.printStackTrace()
        }
        document.close()
        return qtPages
    }

    private fun removePages(input: String, output: String, pages: String): Boolean {
        var document: PDDocument? = null
        try {
            var strPages = pages.split(",")
            var pages: MutableList<Int> = mutableListOf()
            // Converting values to int before sorting them
            strPages.forEach() {
                pages.add(Integer.parseInt(it))
            }
            Collections.sort(pages, Collections.reverseOrder())
            document = PDDocument.load(File(input))
            pages.forEach {
                document.removePage(it)
            }
            document.save(output)
            document.close()
            return true
        } catch (obe: IndexOutOfBoundsException) {
            if (document != null) {
                document.close()
            }
            println("At least one of the specified pages exceeds the size of the document")
            return false
        } catch (e: Exception) {
            if (document != null) {
                document.close()
            }
            e.printStackTrace()
            return false
        }
    }

}