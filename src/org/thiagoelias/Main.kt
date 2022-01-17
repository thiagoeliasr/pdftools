package org.thiagoelias

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
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

                removePages(input, output, pagesToRemove)
                return
            }
            if (action == "merge-files") {
                // When merging PDFs, the last path will always be the output.
                if (args.size < 4) {
                    println("To merge PDFs, you need to specify at least two inputs. "
                        + "(Remember that the last file will always be the output")
                    return
                }

                val inputs: MutableList<String> = mutableListOf()
                for (i in 1 until (args.size - 1)) {
                    inputs.add(args[i])
                }

                output = args[args.size - 1]

                // @todo: Let user specify how much MainMemory he would like to use. Default to 512MiB
                mergeFiles(inputs, output)
                return
            }
            println("Usage: java pdftools <action> <input> <output?> <pagesToRemove?>")
        } catch (iex: IndexOutOfBoundsException) {
            println("Page out of bounds. Check if the document has all the pages specified for removal")
        } catch (ioe: IOException) {
            println("An IO Error has occurred: ${ioe.message}")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }
    }

    @Throws(IOException::class)
    private fun getPages(input: String): Int {
        var qtPages = 0
        val document = PDDocument.load(File(input))
        qtPages = document.numberOfPages
        document.close()
        return qtPages
    }

    @Throws(IndexOutOfBoundsException::class, Exception::class)
    private fun removePages(input: String, output: String, pages: String) {
        var document: PDDocument? = null
        val strPages = pages.split(",")
        val pages: MutableList<Int> = mutableListOf()

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
    }

    @Throws(IOException::class)
    private fun mergeFiles(inputs: MutableList<String>, output: String, maxMemory: Long = 512) {
        val pdfMerger = PDFMergerUtility()
        pdfMerger.destinationFileName = output
        inputs.forEach() {
            pdfMerger.addSource(File(it))
        }
        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * maxMemory))
    }

}