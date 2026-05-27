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
    companion object {
        private val GRID_SIZES = mapOf(0 to 16, 1 to 32, 2 to 64)
    }

    /**
     * Convierte una imagen a gridData pixelado.
     * Retorna un JSON array 2D de colores hex: [["#RRGGBB", ...], ...]
     */
    fun imageToGridData(file: MultipartFile, size: Int): String {
        val gridSize = GRID_SIZES[size] ?: 16
        val original: BufferedImage = ImageIO.read(file.inputStream)
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        // Redimensionar con promedio de bloques (convolución de downsampling)
        val grid = downsampleWithAveraging(original, gridSize)

        return objectMapper.writeValueAsString(grid)
    }

    /**
     * Divide la imagen original en `gridSize x gridSize` bloques
     * y promedia el color de cada bloque (box filter / average pooling).
     */
    private fun downsampleWithAveraging(
        image: BufferedImage,
        gridSize: Int
    ): Array<Array<String>> {
        val srcW = image.width
        val srcH = image.height

        val blockW = srcW.toDouble() / gridSize
        val blockH = srcH.toDouble() / gridSize

        return Array(gridSize) { row ->
            Array(gridSize) { col ->
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

        val rAvg = (r / count).toInt()
        val gAvg = (g / count).toInt()
        val bAvg = (b / count).toInt()

        return "#%02X%02X%02X".format(rAvg, gAvg, bAvg)
    }
}