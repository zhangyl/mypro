package com.zyl.mypro.wartermark.pdf;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.FileOutputStream;


public class PDFUtil {

    /**
     * pdf文件添加图片水印
     * itext-2.0.1.jar
     * @param srcPath 输入的文件路径
     * @param destPath 输出的文件路径
     * @param imagePath 水印图片的路径
     * @throws Exception
     */
    public static void addPDFImageWaterMark(String srcPath, String destPath, String imagePath)
            throws Exception {

        PdfReader reader = new PdfReader(srcPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(destPath));

        //加载图片
        Image image = Image.getInstance(imagePath);

        PdfGState gs = new PdfGState();
        //gs.setFillOpacity(0.2f);//图片水印透明度
        //gs.setStrokeOpacity(0.4f);//设置笔触字体不透明度
        PdfContentByte content = null;

        int total = reader.getNumberOfPages();//pdf文件页数
        for (int i=0; i<total; i++) {
            float x = reader.getPageSize(i+1).getWidth();//页宽度
            float y = reader.getPageSize(i+1).getHeight();//页高度
            content = stamper.getOverContent(i+1);
            content.setGState(gs);
            content.beginText();//开始写入

            //每页7行，一行3个
            for (int j=0; j<3; j++) {
                for (int k=0; k<7; k++) {
                    //setAbsolutePosition 方法的参数（输出水印X轴位置，Y轴位置）
                    image.setAbsolutePosition(x/3*j-30, y/7*k-20);
                    content.addImage(image);
                }
            }
            content.endText();//结束写入
        }
        //关闭流
        stamper.close();
        reader.close();
    }

    public static void main(String[] args) throws Exception {
        addPDFImageWaterMark("C:\\Users\\pi\\Desktop\\fp\\fw.pdf", "C:\\Users\\pi\\Desktop\\fp\\fw2.pdf", ImageUtil.IMGE);
    }
}

