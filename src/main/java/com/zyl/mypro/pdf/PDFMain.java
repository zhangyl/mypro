package com.zyl.mypro.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;


public class PDFMain {
    public static void main(String[] args) throws Exception {
//        String src = "C:\\Users\\pi\\Desktop\\fp\\aa.pdf";
//        String tar = "C:\\Users\\pi\\Desktop\\fp\\cc.pdf";
//        pdfToSub(src, tar, 2, 2);
        String fw = "C:\\Users\\pi\\Desktop\\fp\\fw.pdf";
        getTextFromPDF(fw);
    }

    public static void pdfToSub(String filePath,String newFile, int from, int end) {
        Document document = null;
        PdfCopy copy = null;
        try {
            PdfReader reader = new PdfReader(filePath);
            //总页数
            int n = reader.getNumberOfPages();
            if (end == 0) {
                end = n;
            }
            document = new Document(reader.getPageSize(1));
            copy = new PdfCopy(document, new FileOutputStream(newFile));
            document.open();
            for (int j = from; j <= end; j++) {
                document.newPage();
                PdfImportedPage page = copy.getImportedPage(reader, j);
                copy.addPage(page);
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTextFromPDF(String pdfFilePath) throws Exception {
        RandomAccessRead accessRead = new RandomAccessFile(new File(pdfFilePath), "rw");
        PDFParser parser = new PDFParser(accessRead); // 创建PDF解析器
        parser.parse(); // 执行PDF解析过程
        PDDocument pdfdocument = parser.getPDDocument(); // 获取解析器的PDF文档对象
        PDFTextStripper pdfstripper = new PDFTextStripper(); // 生成PDF文档内容剥离器
        String contenttxt = pdfstripper.getText(pdfdocument); // 利用剥离器获取文档
        System.out.println(contenttxt);
        accessRead.close();
        pdfdocument.close();
        return contenttxt;
    }

}
