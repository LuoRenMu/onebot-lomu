package cn.luorenmu.common.utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * @author LoMu
 * Date 2024.08.03 6:25
 */
class DrawImageUtils {
    var fontName: String = "微软雅黑"
    var fontType: Int
    private var template: BufferedImage? = null
    private var graphics2D: Graphics2D? = null

    init {
        fontType = Font.BOLD
    }

    fun getTemplate(): BufferedImage {
        return template!!
    }

    fun setTemplate(path: String): DrawImageUtils {
        try {
            template = ImageIO.read(File(path))
            setImage()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this
    }

    fun setTemplate(bufferedImage: BufferedImage): DrawImageUtils {
        template = bufferedImage
        setImage()
        return this
    }

    fun getGraphics2D(): Graphics2D {
        return graphics2D!!
    }

    fun setGraphics2D(graphics2D: Graphics2D) {
        this.graphics2D = graphics2D
    }

    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Color?): DrawImageUtils {
        graphics2D!!.color = color
        graphics2D!!.drawRect(x, y, width, height)
        return this
    }

    private fun setImage() {
        graphics2D = template!!.createGraphics()
        graphics2D!!.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics2D!!.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics2D!!.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawString("Powered by LoMu", Color.gray, 0, 8, 8)
    }

    val width: Int
        get() = template!!.width

    val height: Int
        get() = template!!.height

    fun drawString(text: String, color: Color, x: Int, y: Int, size: Int): DrawImageUtils {
        graphics2D!!.font = Font(fontName, fontType, size)
        graphics2D!!.color = color
        graphics2D!!.drawString(text, x, y)
        return this
    }

    fun drawImage(
        bufferedImage: BufferedImage?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color?,
    ): DrawImageUtils {
        graphics2D!!.drawImage(bufferedImage, x, y, width, height, color, null)
        return this
    }

    fun drawImage(bufferedImage: BufferedImage, x: Int, y: Int): DrawImageUtils {
        graphics2D!!.drawImage(bufferedImage, x, y, null)
        return this
    }

    fun drawImage(bufferedImage: BufferedImage?, x: Int, y: Int, color: Color): DrawImageUtils {
        graphics2D!!.drawImage(bufferedImage, x, y, color, null)
        return this
    }


    fun drawImage(path: String, x: Int, y: Int, width: Int, height: Int, color: Color?): DrawImageUtils {
        try {
            val read = ImageIO.read(File(path))
            graphics2D!!.drawImage(read, x, y, width, height, color, null)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    fun drawImage(path: String, x: Int, y: Int): DrawImageUtils {
        try {
            val read = ImageIO.read(File(path))
            graphics2D!!.drawImage(read, x, y, null)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this
    }

    fun drawImage(path: String, x: Int, y: Int, color: Color): DrawImageUtils {
        try {
            val read = ImageIO.read(File(path))
            graphics2D!!.drawImage(read, x, y, color, null)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    fun setFont(fontName: String, type: Int): DrawImageUtils {
        this.fontName = fontName
        this.fontType = type
        return this
    }

    val graphics: Graphics2D
        get() = graphics2D!!

    fun saveImage(path: String) {
        try {
            ImageIO.write(template, "PNG", File(path))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        graphics2D!!.dispose()
    }

    companion object {
        fun builder(): DrawImageUtils {
            return DrawImageUtils()
        }
    }
}