package org.thiagoelias

import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.media.jai.PlanarImage
import kotlin.math.roundToInt


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
            if (action == "compress") {
                output = args[2]
                compressPDF(input, output)
                return
            }
            println("Usage: java pdftools <action> <input> <output?> <pagesToRemove?>")
        } catch (iex: IndexOutOfBoundsException) {
            println("Page out of bounds. Check if the document has all the pages specified for removal")
        } catch (ioe: IOException) {
            println("An IO Error has occurred: ${ioe.message}")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            e.printStackTrace()
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

    @Throws(IOException::class)
    private fun compressPDF(input: String, output: String) {
        val document: PDDocument = PDDocument.load(File(input))
        replaceBitmapImagesResources(document)
        document.save(output)
        document.close()
    }

    @Throws(IOException::class)
    fun replaceBitmapImagesResources(document: PDDocument) {
        val pdFormXObject = PDFormXObject(document)
        pdFormXObject.bBox = PDRectangle(1f, 1f)
        for (pdPage in document.pages) {
            replaceBitmapImagesResources(pdPage.resources, pdFormXObject, document)
        }
    }

    @JvmStatic
    fun createCompressedJpeg(image: RenderedImage): ByteArray {
        val compressed = ByteArrayOutputStream()

        // @todo: Let user input compression quality and scaling factor if necessary
        ImageIO.createImageOutputStream(compressed).use { outputStream ->

            // Obtain writer for JPEG format
            val jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next()

            // Configure JPEG compression: 70% quality
            val jpgWriteParam = jpgWriter.defaultWriteParam
            jpgWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            jpgWriteParam.compressionQuality = 0.50f

            var bfImg = PlanarImage.wrapRenderedImage(image).asBufferedImage
            // Dropping the alpha channel first, since it can cause color issues
            if (bfImg.colorModel.hasAlpha()) {
                var newimg = BufferedImage(bfImg.width, bfImg.height, BufferedImage.TYPE_INT_RGB)
                newimg.graphics.drawImage(bfImg, 0, 0, null)
                bfImg = newimg
            }

            // Scaling down the image in 20%
            val scaledImage: BufferedImage = Scalr.resize(bfImg,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    (image.width - (image.width * (0.2))).roundToInt(),
                    (image.height - (image.height * (0.2))).roundToInt(), null)

            // Set your in-memory stream as the output
            jpgWriter.output = outputStream

            jpgWriter.write(null, IIOImage(scaledImage, null, null), jpgWriteParam)

            // Dispose the writer to free resources
            jpgWriter.dispose()
        }

        return compressed.toByteArray()
    }

    @Throws(IOException::class)
    fun replaceBitmapImagesResources(resources: PDResources?, formXObject: PDFormXObject?, document: PDDocument?) {
        if (resources == null) return
        for (cosName in resources.patternNames) {
            val pdAbstractPattern = resources.getPattern(cosName)
            if (pdAbstractPattern is PDTilingPattern) {
                replaceBitmapImagesResources(pdAbstractPattern.resources, formXObject, document)
            }
        }
        val xobjectsToReplace: MutableList<COSName> = ArrayList()
        val xobjectsToReplaceImage: MutableList<PDImageXObject> = ArrayList()
        for (cosName in resources.xObjectNames) {
            val pdxObject = resources.getXObject(cosName)
            if (pdxObject is PDImageXObject) {
                // @todo: Let user specify values for quality and DPI
                val byteArrayImage = createCompressedJpeg(pdxObject.image)
                // val pdImage = JPEGFactory.createFromImage(document, pdxObject.image, 0.50f, 0)
                var pdImage = JPEGFactory.createFromByteArray(document, byteArrayImage)
                xobjectsToReplace.add(cosName)
                xobjectsToReplaceImage.add(pdImage)
            } else if (pdxObject is PDFormXObject) {
                replaceBitmapImagesResources(pdxObject.resources, formXObject, document)
            }
        }
        var idx = 0
        for (cosName in xobjectsToReplace) {
            resources.put(cosName, xobjectsToReplaceImage.get(idx))
            idx++
        }
    }

}