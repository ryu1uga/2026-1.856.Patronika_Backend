package pe.edu.ulima.patronika.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class ImageConvolutionServiceTest {

    private lateinit var service: ImageConvolutionService

    @BeforeEach
    fun setUp() {
        service = ImageConvolutionService(ObjectMapper())
    }

    private fun BuildImage(
        width: Int,
        height: Int,
        colorAt: (X: Int, y: Int) -> Color
    ): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                image.setRGB(x, y, colorAt(x, y).rgb)
            }
        }
        return image
    }

    private fun toMultipartFile(image: BufferedImage, filename: String = "test.png"): MockMultipartFile {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return MockMultipartFile(
            "file",
            filename,
            "image/png",
            baos.toByteArray()
        )
    }

    //Happy paths
    @Test
    fun imagen_unSoloColor() {
        val rojo = Color(255, 0, 0)
        val image = BuildImage(10, 10) { _, _ -> rojo }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 2, height = 2)

        val grid: Array<Array<String>> = ObjectMapper().readValue(
            json,
            Array<Array<String>>::class.java
        )

        assertEquals(2, grid.size, "Debe haber 'height' filas")
        assertEquals(2, grid[0].size, "Debe haber 'width' columnas")
        grid.forEach { row -> row.forEach { cell -> assertEquals("#FF0000", cell) } }
    }

    @Test
    fun grid1x1_promediaUnColor() {
        val image = BuildImage(10, 10) { x, _ -> if (x < 5) Color.BLACK else Color.WHITE }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 1, height = 1)
        val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)

        assertEquals(1, grid.size)
        assertEquals(1, grid[0].size)
        assertEquals("#7F7F7F", grid[0][0])

    }

    @Test
    fun BloqueVacio_devuelveNegro() {
        val image = BuildImage(1, 1) { _, _ -> Color.RED }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 3, height = 3)
        val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)

        assertEquals("#000000", grid[0][1])
    }

    @Test
    fun gridDeCuadrantes_cadaUnoConSuColor(){
        val image = BuildImage(4, 4) { x, y ->
            when {
                x < 2 && y < 2 -> Color.RED
                x >= 2 && y < 2 -> Color.GREEN
                x < 2 && y >= 2 -> Color.BLUE
                else -> Color.YELLOW
            }
        }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 2, height = 2)
        val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)

        // grid[row][col] -> ¿qué color esperas en cada posición?
        assertEquals("#FF0000", grid[0][0])
        assertEquals("#00FF00", grid[0][1]);
        assertEquals("#0000FF", grid[1][0])
        assertEquals("#FFFF00", grid[1][1])
    }

    @Test
    fun gridMasGrande() {
        val image = BuildImage(2, 2) { _, _ -> Color.BLUE }
        val file = toMultipartFile(image)

        assertDoesNotThrow {
            val json = service.imageToGridData(file, width = 5, height = 5)
            val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)
            assertEquals(5, grid.size)
            assertEquals(5, grid[0].size)
        }
    }

    //Valores Limite
    @Test
    fun widthCero_ForzarAUno(){
        val image = BuildImage(4, 4) { _, _ -> Color.GREEN }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 0, height = 2)
        val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)

        assertEquals(2, grid.size)
        assertEquals(1, grid[0].size, "width=0 debe convertirse en 1 por el maxOf(1, width)")
    }

    @Test
    fun heightNegativo_ForzarAUno(){
        val image = BuildImage(4, 4) { _, _ -> Color.GREEN }
        val file = toMultipartFile(image)

        val json = service.imageToGridData(file, width = 2, height = -3)
        val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)

        assertEquals(1, grid.size, "height negativo debe convertirse en 1 por el maxOf(1, height)")
        assertEquals(2, grid[0].size)
    }

    @Test
    fun ImagenYGridProducen_NoEntero(){
        val image = BuildImage(7, 7) { x, y -> if ((x + y) % 2 == 0) Color.BLACK else Color.WHITE }
        val file = toMultipartFile(image)
        // 7 Entre 3 no es entero por lo que se redondea
        assertDoesNotThrow {
            val json = service.imageToGridData(file, width = 3, height = 3)
            val grid: Array<Array<String>> = ObjectMapper().readValue(json, Array<Array<String>>::class.java)
            assertEquals(3, grid.size)
            assertEquals(3, grid[0].size)
            // Cada celda debe ser un color hexadecimal válido de 7 caracteres (#RRGGBB)
            grid.forEach { row -> row.forEach { cell -> assertTrue(cell.matches(Regex("#[0-9A-F]{6}"))) } }
        }
    }

    //errores
    @Test
    fun ImagenNoCodificable_IllegalArgumentExcep(){
        val archivoTexto = MockMultipartFile(
            "file",
            "no-es-imagen.txt",
            "text/plain",
            "esto no es una imagen".toByteArray()
        )
        assertThrows(IllegalArgumentException::class.java) {
            service.imageToGridData(archivoTexto, width = 2, height = 2)
        }
    }

}