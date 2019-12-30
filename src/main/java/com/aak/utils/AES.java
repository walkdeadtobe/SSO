package com.aak.utils;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
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
    /**
     * Generate key by keyword and aes 128
     * @param keyword
     * @return key
     */
    public String key_generate(String keyword){
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, new SecureRandom(keyword.getBytes()));
            SecretKey sk = kg.generateKey();
            byte[] b = sk.getEncoded();
            String s = byteToHexString(b);
            System.out.println(s);
        }catch(Exception e){
            log.info(e.toString());

        }
        return null;
    }
    /**
     * transform byte array to hex string
     * @param bytes
     * @return string
     */
    public static String byteToHexString(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String strHex=Integer.toHexString(bytes[i]);
            if(strHex.length() > 3){
                sb.append(strHex.substring(6));
            } else {
                if(strHex.length() < 2){
                    sb.append("0" + strHex);
                } else {
                    sb.append(strHex);
                }
            }
        }
        return  sb.toString();
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
            System.out.println("str:"+str);
            if(!str.matches("mobile=[0-9]{0,20},userName=.{1,200}"))
                return null;
            String[] back=str.split(",");
            String name="";
            String mobile="";
            for(int i=0;i<back.length;i++) {
                if (back[i].split("=")[0].equals("mobile"))
                    mobile = back[i].split("=")[1];
                if (back[i].split("=")[0].equals( "userName"))
                    name = back[i].split("=")[1];
            }
            String uuid=name+"-"+mobile;
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(uuid.getBytes("UTF-8"));
            System.out.println("name:"+name);
            //return ByteUtils.toHexString(m.digest());
            return name;

        }catch (Exception e){
            log.info(e.toString());
            return null;
        }

    }

    public static void main(String []args){
        AES aes=new AES("sss");
        /*
        String back=aes.EncryptString("aaaaa","1234567890123456");
        log.info("back:"+back);
        log.info("base:"+aes.Decrypt_now(back));
        String origin=aes.Decrypt(back,"1234567890123456");
        log.info("origin:"+origin);
        */
        String appkey="kexieyjiaapp",appsecret="kexieyijia-smart";
        aes.key_generate(appkey);
        //de1a98dcf5a32ff7fddfbcfb795c518a
        aes.key_generate(appsecret);
        //527305d2b60356e0e498c056a1c610d1

    }

}
