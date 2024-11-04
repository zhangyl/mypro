package com.zyl.mypro.eml;

import org.ofdrw.converter.export.ImageExporter;
import org.ofdrw.converter.export.SVGExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OFDConverter {

    public static void main(String[] args) {
        Path ofdPath = Paths.get("C:\\Users\\pi\\Desktop\\fp\\订单31772197501电子行程单.ofd");
        Path imgDirPath = Paths.get("C:\\Users\\pi\\Desktop\\fp");
        try (ImageExporter exporter = new ImageExporter(ofdPath, imgDirPath)) {
            exporter.export();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
