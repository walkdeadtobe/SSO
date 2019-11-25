package com.aak.utils;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class AES {
    public static Log log= LogFactory.getLog(AES.class);
    private  Key final_key;
    private String Key;
    private final String Key_Ciper="AES";
    private final String Key_Algorithm="AES/ECB/PKCS5Padding";
    private final String CHARSETNAME="UTF-8";
    private final Integer SECRET_KEY_LENGTH=128;
    public AES(String Key){
        this.Key=Key;
        //init();
    }
    private void init(){
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(Key_Algorithm);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            kgen.init(SECRET_KEY_LENGTH, new SecureRandom(this.Key.getBytes()));
            final_key = kgen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public  String EncryptString(String str,String key) {
        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(str.getBytes("utf-8"));
            log.info("encry:"+encrypted);

            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String Decrypt(String str,String key){
        try{
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted = new Base64().decode(str);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }

    }

    public String Decrypt_now(String str){
        try{
           // byte[] back= (new Base64()).decode(str);
            //return back.toString();
            if(!str.matches("mobile=[0-9]{0,20},userName=.{1,200}"))
                return null;
            String[] back=str.split(",");
            String name="";
            String mobile="";
            for(int i=0;i<back.length;i++) {
                if (back[i].split("=")[0] == "mobile")
                    mobile = back[i].split("=")[1];
                if (back[i].split("=")[0] == "userName")
                    name = back[i].split("=")[1];
            }
            String uuid=name+"-"+mobile;
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(uuid.getBytes("UTF-8"));
            return ByteUtils.toHexString(m.digest());

        }catch (Exception e){
            log.info(e.toString());
            return null;
        }

    }

    public static void main(){
        AES aes=new AES("sss");
        String back=aes.EncryptString("aaaaa","1234567890123456");
        log.info("back:"+back);
        log.info("base:"+aes.Decrypt_now(back));
        String origin=aes.Decrypt(back,"1234567890123456");
        log.info("origin:"+origin);

    }

}
