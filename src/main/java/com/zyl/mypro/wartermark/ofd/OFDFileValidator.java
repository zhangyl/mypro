package com.zyl.mypro.wartermark.ofd;

import java.io.*;

public class OFDFileValidator {
    private static final String OFD_FILE_EXTENSION = ".ofd";
    private static final byte[] OFD_FILE_SIGNATURE = { 0x4f, 0x46, 0x44, 0x20 }; // OFD 文件的签名

    public static boolean isOFDFile(File file) throws IOException {
        if (!file.getName().toLowerCase().endsWith(OFD_FILE_EXTENSION)) {
            return false; // 文件扩展名不是 .ofd
        }

        byte[] buffer = new byte[OFD_FILE_SIGNATURE.length];
        try (InputStream is = new FileInputStream(file)) {
            if (is.read(buffer) != OFD_FILE_SIGNATURE.length) {
                return false; // 文件大小小于签名长度
            }
        }

        for (int i = 0; i < OFD_FILE_SIGNATURE.length; i++) {
            if (buffer[i] != OFD_FILE_SIGNATURE[i]) {
                return false; // 文件签名不匹配
            }
        }

        return true; // 文件是OFD格式
    }

    public static void main(String[] args) throws Exception {

        try {

            String line0 = null;
            String line1 = null;
            {
                String path = "C:\\Users\\pi\\Desktop\\吃饭_杭州每刻科技有限公司_20240805165417.ofd";
                File file = new File(path);
                FileInputStream inputStream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                line0 = reader.readLine();
            }
            {
                String path = "C:\\Users\\pi\\Desktop\\专属钉工作计划.zip";
                File file = new File(path);
                FileInputStream inputStream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                line1 = reader.readLine();
            }
            System.out.println(line0);
            System.out.println(line1);
        } catch (IOException e) {
            System.out.println("An error occurred while processing the file.");
        }
    }
}
