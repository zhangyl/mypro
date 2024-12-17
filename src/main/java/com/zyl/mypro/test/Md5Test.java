package com.zyl.mypro.test;


import org.apache.commons.codec.digest.Md5Crypt;

import java.security.MessageDigest;

public class Md5Test {
    public static void main(String[] args) throws  Exception {


        {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            String msg = "用户名称不匹配";
            System.out.println(msg.hashCode());
            System.out.println(Md5Crypt.md5Crypt(msg.getBytes()));
        }
        {
            String msg = new String("用户名称不匹配");
            System.out.println(msg.hashCode());
            System.out.println(Md5Crypt.md5Crypt(msg.getBytes()));
        }

    }
}
