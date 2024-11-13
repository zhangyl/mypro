package com.zyl.mypro.wartermark.pdf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageUtil {
    public static String IMGE = "C:\\Users\\pi\\qm2.png";
    /**
     * 生成背景透明的文字水印图片，文字位于中央，且倾斜
     * @param content 水印文字
     * @return
     */
    public static BufferedImage createWaterMark(String content) {
        //生成图片宽度
        int width = 250;
        //生成图片高度
        int heigth = 160;
        //获取bufferedImage对象
        BufferedImage image = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
        //得到画笔对象
        Graphics2D g2d = image.createGraphics();
        //使得背景透明
        image = g2d.getDeviceConfiguration().createCompatibleImage(width, heigth, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = image.createGraphics();
        //设置对线段的锯齿状边缘处理
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //设置水印旋转，倾斜度
        g2d.rotate(-0.5, (double) image.getWidth()/2, (double) image.getHeight()/2);
        //设置颜色，这是黑色，第4个参数是透明度
        g2d.setColor(new Color(0, 0, 0, 20));
        //设置字体
        Font font = new Font("宋体", Font.ROMAN_BASELINE, 22);
        g2d.setFont(font);
        float fontSize = font.getSize();
        //计算绘图偏移x、y，使得使得水印文字在图片中居中
        float x = 0.5f * fontSize;
        float y = 0.5f * heigth + x;
        //取绘制的字串宽度、高度中间点进行偏移，使得文字在图片坐标中居中
        g2d.drawString(content, x, y);
        //释放资源
        g2d.dispose();
        return image;
    }

    public static void main(String[] args) throws Exception {
        BufferedImage bi = createWaterMark("水印图片 2024-11-02");
        ImageIO.write(bi, "png", new File(IMGE)); //写入文件
    }
}
