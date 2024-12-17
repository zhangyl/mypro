package com.zyl.mypro.wartermark.ofd;


import java.nio.file.Files;
import java.nio.file.Path;

public class OfdMain {
    public static void main(String[] args) throws Exception {

//        String path = "C:\\Users\\pi\\Desktop\\吃饭_杭州每刻科技有限公司_20240805165417.ofd";
        int head = 8;
        byte[] tmp0 = new byte[head];
        byte[] tmp1 = new byte[head];
        byte[] tmp2 = new byte[head];
        {
            String path = "C:\\Users\\pi\\Desktop\\火车票.ofd";
            byte[] content = Files.readAllBytes(Path.of(path));

            for (int i = 0; i < head; i++) {
                tmp0[i] = content[i];
            }


        }
        {
            String path = "C:\\Users\\pi\\Desktop\\吃饭_杭州每刻科技有限公司_20240805165417.ofd";
            byte[] content = Files.readAllBytes(Path.of(path));
            for (int i = 0; i < head; i++) {
                tmp1[i] = content[i];
            }

        }
        {
            String path = "C:\\Users\\pi\\Desktop\\专属钉工作计划.zip";
            byte[] content = Files.readAllBytes(Path.of(path));
            for (int i = 0; i < head; i++) {
                tmp2[i] = content[i];
            }

        }
        System.out.println(tmp0);
        System.out.println(tmp1);
        System.out.println(tmp2);
    }
}
