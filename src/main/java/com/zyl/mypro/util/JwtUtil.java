package com.zyl.mypro.util;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
 
 
public class JwtUtil {
	
    /**
     * 生成密钥
     * @return SecretKey
     */
    private static SecretKey generalKey(){
        String stringKey = "7786df7fc3a34e26a61c034d5ec8245d";
        byte[] encodedKey = Base64.getEncoder().encode(stringKey.getBytes());
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return secretKey;
    }

    /**
     * 根据用户信息为其签发token
     */
    public static String generalTocken(Map<String, Object> claims){
        try {
            // 设置签发算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            // 生成密钥
            SecretKey key = generalKey();

            // 记录生成JWT的时间
            long nowMillis = System.currentTimeMillis();
            Date nowTime = new Date(nowMillis);
            // 设置过期时间
            long expMillis = nowMillis + 24 * 60 * 60 * 1000;
            Date expTime = new Date(expMillis);
            Map<String, Object> header = new HashMap<>();
            // 创建tocken构建器实例
            JwtBuilder jwtBuilder = Jwts.builder()
            		.setHeader(header)
                    // 设置自己的私有声明
                    .setClaims(claims)
                    // 设置该tocken的Id，用于防止tocken重复
                    .setId(UUID.randomUUID().toString())
                    // 设置签发者
                    .setIssuer("FUQI-PC")
                    // 设置签发时间
                    .setIssuedAt(nowTime)
                    // 设置过期时间
                    .setExpiration(expTime)
                    // 设置tocken的签发对象
                    .setSubject("users")
                    // 设置签发算法和密钥
                    .signWith(signatureAlgorithm, key);
            
            return jwtBuilder.compact();
        } catch (Exception e) {
            e.printStackTrace();
            return "生成tocken失败";
        }
    }
 
    /**
     * 解析tocken，从中提取出声明信息，里面包含用户信息
     * @param tocken
     * @return Claims
     * @throws Exception
     */
    public static Claims parseTocken(String tocken) throws Exception{
        SecretKey key = generalKey();
//        System.out.println("解析tocken时生成的key为：" + key);
        // 获取tocken中的声明部分
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(tocken).getBody();
        return claims;
    }
 
 
}