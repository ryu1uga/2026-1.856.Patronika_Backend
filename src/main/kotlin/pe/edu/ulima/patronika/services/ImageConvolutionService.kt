package pe.edu.ulima.patronika.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

@Service
class ImageConvolutionService(
    private val objectMapper: ObjectMapper
) {
    /**
     * Convierte una imagen a gridData pixelado.
     * @param width Ancho del grid en píxeles (resolución real).
     * @param height Alto del grid en píxeles (resolución real).
     * Retorna un JSON array 2D de colores hex: [["#RRGGBB", ...], ...]
     */
    fun imageToGridData(file: MultipartFile, width: Int, height: Int): String {
        val original: BufferedImage = ImageIO.read(file.inputStream)
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        val gridW = maxOf(1, width)
        val gridH = maxOf(1, height)

        val grid = downsampleWithAveraging(original, gridW, gridH)
        return objectMapper.writeValueAsString(grid)
    }

    /**
     * Divide la imagen original en `gridW x gridH` bloques
     * y promedia el color de cada bloque (box filter / average pooling).
     */
    private fun downsampleWithAveraging(
        image: BufferedImage,
        gridW: Int,
        gridH: Int
    ): Array<Array<String>> {
        val srcW = image.width
        val srcH = image.height

        val blockW = srcW.toDouble() / gridW
        val blockH = srcH.toDouble() / gridH

        return Array(gridH) { row ->
            Array(gridW) { col ->
                val xStart = (col * blockW).toInt()
                val yStart = (row * blockH).toInt()
                val xEnd = minOf(((col + 1) * blockW).toInt(), srcW)
                val yEnd = minOf(((row + 1) * blockH).toInt(), srcH)

                averageColor(image, xStart, yStart, xEnd, yEnd)
            }
        }
    }

    /**
     * Calcula el color promedio de una región rectangular de la imagen.
     */
    private fun averageColor(
        image: BufferedImage,
        x1: Int, y1: Int, x2: Int, y2: Int
    ): String {
        var r = 0L; var g = 0L; var b = 0L; var count = 0

        for (y in y1 until y2) {
            for (x in x1 until x2) {
                val rgb = image.getRGB(x, y)
                r += (rgb shr 16) and 0xFF
                g += (rgb shr 8) and 0xFF
                b += rgb and 0xFF
                count++
            }
        }

        if (count == 0) return "#000000"

        return "#%02X%02X%02X".format(r / count, g / count, b / count)
    }
}