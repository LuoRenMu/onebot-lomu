package cn.luorenmu.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author LoMu
 * Date 2024.08.03 6:25
 */
public class DrawImageUtils {
    private String fontName;
    private int fontType;
    private BufferedImage template;
    private Graphics2D graphics2D;

    public DrawImageUtils() {
        fontName = "微软雅黑";
        fontType = Font.BOLD;
    }

    public static DrawImageUtils builder() {
        return new DrawImageUtils();
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontType() {
        return fontType;
    }

    public void setFontType(int fontType) {
        this.fontType = fontType;
    }

    public BufferedImage getTemplate() {
        return template;
    }

    public DrawImageUtils setTemplate(String path) {
        try {
            template = ImageIO.read(new File(path));
            setImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DrawImageUtils setTemplate(BufferedImage bufferedImage) {
        template = bufferedImage;
        setImage();
        return this;
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }

    public void setGraphics2D(Graphics2D graphics2D) {
        this.graphics2D = graphics2D;
    }

    public DrawImageUtils drawRect(int x, int y, int width, int height, Color color) {
        graphics2D.setColor(color);
        graphics2D.drawRect(x, y, width, height);
        return this;
    }

    private void setImage() {
        graphics2D = template.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawString("Powered by LoMu", Color.gray, 0, 8, 8);
    }

    public int getWidth() {
        return template.getWidth();
    }

    public int getHeight() {
        return template.getHeight();
    }

    public DrawImageUtils drawString(String text, Color color, int x, int y, int size) {
        graphics2D.setFont(new Font(fontName, fontType, size));
        graphics2D.setColor(color);
        graphics2D.drawString(text, x, y);
        return this;
    }

    public DrawImageUtils drawImage(BufferedImage bufferedImage, int x, int y, int width, int height, Color color) {
        graphics2D.drawImage(bufferedImage, x, y, width, height, color, null);
        return this;
    }

    public DrawImageUtils drawImage(BufferedImage bufferedImage, int x, int y) {
        graphics2D.drawImage(bufferedImage, x, y, null);
        return this;
    }

    public DrawImageUtils drawImage(BufferedImage bufferedImage, int x, int y, Color color) {
        graphics2D.drawImage(bufferedImage, x, y, color, null);
        return this;
    }


    public DrawImageUtils drawImage(String path, int x, int y, int width, int height, Color color) {
        try {
            BufferedImage read = ImageIO.read(new File(path));
            graphics2D.drawImage(read, x, y, width, height, color, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public DrawImageUtils drawImage(String path, int x, int y) {
        try {
            BufferedImage read = ImageIO.read(new File(path));
            graphics2D.drawImage(read, x, y, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DrawImageUtils drawImage(String path, int x, int y, Color color) {
        try {
            BufferedImage read = ImageIO.read(new File(path));
            graphics2D.drawImage(read, x, y, color, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public DrawImageUtils setFont(String fontName, int type) {
        this.fontName = fontName;
        this.fontType = type;
        return this;
    }

    public Graphics2D getGraphics() {
        return graphics2D;
    }

    public void saveImage(String path) {
        try {
            ImageIO.write(template, "PNG", new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        graphics2D.dispose();
    }
}
