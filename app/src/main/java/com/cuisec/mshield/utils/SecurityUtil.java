package com.cuisec.mshield.utils;

import android.util.Base64;

import com.cuisec.mshield.config.Config;
import com.zhy.http.okhttp.utils.L;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {

    public static final String SIGN_ALGORITHMS = "SHA256WithRSA";

    private static final String desKey= "CXSHSHCREDIT2018";

    public static String signature(String privateKey, String data) {
        String result = null;
        try {
            byte[] signature = signature(privateKey, data.getBytes(Config.UTF_8));
            if (signature != null && signature.length > 0) {
                result = Base64.encodeToString(signature, Base64.NO_WRAP);
            }
        } catch (UnsupportedEncodingException e) {
            L.e(e.getLocalizedMessage());
        }
        return result;
    }

    public static String encrypt(String key, String src) {
        String result = null;
        try {
            byte[] rawKey = getRawKey(key.getBytes());
            byte[] enc = encrypt(rawKey, src.getBytes());
            if (enc != null && enc.length > 0) {
                result = Base64.encodeToString(enc, Base64.NO_WRAP);
            }
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
        return result;
    }

    public static String decrypt(String key, String encrypted) {
        String result = null;
        try {
            byte[] rawKey = getRawKey(key.getBytes());
            byte[] dec = decrypt(rawKey, Base64.decode(encrypted, Base64.NO_WRAP));
            if (dec != null && dec.length > 0) {
                result = Base64.encodeToString(dec, Base64.NO_WRAP);
            }
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
        return result;
    }

    public static String desencrypt(String src) {
        String result = null;
        try {
            byte[] desKeyByte = desKey.getBytes();
            byte[] desenc = desencrypt(desKeyByte, src.getBytes());
            if (desenc != null && desenc.length > 0) {
                result = Base64.encodeToString(desenc, Base64.NO_WRAP);
            }
        } catch(Exception e) {
        }
        return result;
    }

    public static String sha256(String msg) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytePwd = digest.digest(msg.getBytes());
        return Base64.encodeToString(bytePwd, Base64.NO_WRAP);
    }

    private static byte[] signature(String privateKey, byte[] bData) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.decode(privateKey, Base64.DEFAULT));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            Signature oSig = Signature.getInstance(SIGN_ALGORITHMS);
            oSig.initSign(priKey);
            oSig.update(bData);
            return oSig.sign();
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
            return null;
        }
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(256, sr); // 256 bits or 128 bits,192bits
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    private static byte[] encrypt(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("ECB");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(src);
    }

    private static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("ECB");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    private static byte[] desencrypt(byte[] key, byte[] src) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey deskey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, deskey, sr);
        return cipher.doFinal(src);
    }
}
