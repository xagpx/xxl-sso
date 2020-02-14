package com.xxl.sso.server.core.util;
/**
  * @Title:JwtTokenUtil.java
  * @Description:TODO
  * @Author:82322156@qq.com
  * @Date:2020年2月14日下午6:03:02
  * @Version:1.0
  * Copyright 2020  Internet  Products Co., Ltd.
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.xxl.sso.core.user.XxlSsoUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.LinkedHashMap;
/**
  * @Title:JwtTokenUtil.java
  * @Description:TODO
  * @Author:82322156@qq.com
  * @Date:2020年2月14日下午6:30:37
  * @Version:1.0
  * Copyright 2020  Internet  Products Co., Ltd.
  */
public class JwtTokenUtil {
    private static Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);
    public static final String AUTH_HEADER_KEY = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String base64Secret= "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=";
    public static final int  expiresSecond=172800;
    public static final String clientId="098f6bcd4621d373cade4e832627b4f6";
    public static final String issuername="restapiuser";
    /**
     * 解析jwt
     * @param jsonWebToken
     * @param base64Security
     * @return
     */
    public static Claims parseJWT(String jsonWebToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Secret))
                    .parseClaimsJws(jsonWebToken).getBody();
            return claims;
        } catch (ExpiredJwtException  eje) {
        	 return null;
        } 
    }

    /**
     * 构建jwt
     * @param userId
     * @param username
     * @param role
     * @param audience
     * @return
     */
    public static String createJWT(XxlSsoUser xxlUser) {
        try {
            // 使用HS256加密算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Secret);
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());


            //添加构成JWT的参数
            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    // 可以将基本不重要的对象信息放到claims
                    .claim("user", JSONObject.toJSONString(xxlUser))
                    .setSubject(xxlUser.getUsername())           // 代表这个JWT的主体，即它的所有人
                    .setIssuer(clientId)              // 代表这个JWT的签发主体；
                    .setIssuedAt(new Date())        // 是一个时间戳，代表这个JWT的签发时间；
                    .setAudience(issuername)          // 代表这个JWT的接收对象；
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            int TTLMillis = expiresSecond;
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                builder.setExpiration(exp)  // 是一个时间戳，代表这个JWT的过期时间；
                        .setNotBefore(now); // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
            }

            //生成JWT
            return builder.compact();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取用户名
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUsername(String token){
        return parseJWT(token).getSubject();
    }

    /**
     * 从token中获取用户
     * @param token
     * @param base64Security
     * @return
     */
    public static XxlSsoUser getUser(String token){
    	String json = parseJWT(token).get("user", String.class);
    	XxlSsoUser user=JSONObject.parseObject(json, XxlSsoUser.class);
        return user;
    }

    /**
     * 是否已过期
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean isExpiration(String token, String base64Security) {
        return parseJWT(token).getExpiration().before(new Date());
    }
    
    public static void main(String[] args){
    	XxlSsoUser user=new XxlSsoUser();
    	user.setUserid("343");
    	user.setUsername("345353");
    	String token=createJWT(user);
    	System.out.println(token);
    	
    	XxlSsoUser user1=getUser(token);
    	System.out.println(user1.getUserid());
    	
    }
}
