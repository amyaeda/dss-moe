package com.iris.dss.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

public class AESKeyUtil {

	public static final byte[] Initialization_Vector = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	public static byte[] generateIV() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);		
		return iv;
	}
	
	public static SecretKey getSecretKeyFromBytes(byte[] keyData) {
		return new SecretKeySpec(keyData, "AES");
	}
	
	public static SecretKey generateKey(int keySize) throws Exception {	    	
    	try {	    		
	    	KeyGenerator generator = KeyGenerator.getInstance("AES");
	        generator.init(keySize, new SecureRandom());
	        SecretKey key = generator.generateKey();	
    		return key;
    	} catch (Exception e) {    		
    		throw e;
    	}     	
	}
	
	public static byte[] encrypt(SecretKey skey, byte[] iv, byte[] bytesToBeEncrypt) throws Exception {
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		
		Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding"); //, "BC");
		ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
		
		return ci.doFinal(bytesToBeEncrypt);
	}
	
	public static byte[] decrypt(SecretKey skey, byte[] iv, byte[] bytesToBeDecrypt) throws Exception {
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		
		Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding"); //, "BC");
		ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
		
		return ci.doFinal(bytesToBeDecrypt);
	}
	
	public static void main(String... args) throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		//SecretKey sKey = generateKey(256);
		byte[] iv = Initialization_Vector;
		SecretKey sKey = getSecretKeyFromBytes(Base64.decode("460k3JRERGs747qaU7xeDhqQvX9qthWPE4FqZUPB0Sw=")); //sKey.getEncoded());

		byte[] bytesToBeEncrypt1 = Files.readAllBytes(Paths.get("c:/Users/trlok/Desktop/CSharp_ENC.pdf"));
		byte[] bytesToBeEncrypt2 = Files.readAllBytes(Paths.get("c:/Users/trlok/Desktop/PHP_Enc.pdf"));
		
		byte[] decrypted1 = decrypt(sKey, iv, bytesToBeEncrypt1);
		byte[] decrypted2 = decrypt(sKey, iv, bytesToBeEncrypt2);
		System.out.println("Compare both Base64 value: " + 
				Base64.toBase64String(decrypted1).equals(Base64.toBase64String(decrypted2))
		);
	}

}
