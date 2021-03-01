package com.iris.dss.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CertificateUtil {

	public static byte[] p12KeystoreToByteArray(String password, byte[] p12Keystore) throws Exception {
		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);
		
		char[] pin = password.toCharArray();
		KeyStore ks = KeyStore.getInstance("PKCS12", BC);	
		if(p12Keystore!=null) { 
			ks.load(new ByteArrayInputStream(p12Keystore), pin);
		
		} else {			
			throw new Exception("Keystore not found.");
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		ks.store(baos, pin);	
		return baos.toByteArray();
	}
	
	public static KeyStore byteArrayToP12Keystore(String password, byte[] keystoreData) throws Exception {
		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);
		
		char[] pin = password.toCharArray();
		KeyStore ks = KeyStore.getInstance("PKCS12", BC);	 
		 
		if(keystoreData!=null) { 
			ks.load(new ByteArrayInputStream(keystoreData), pin);
		} else {			
			throw new Exception("Keystore data not valid.");
		}
		 
		//ks.store(new FileOutputStream(keystoreOutputPath), pin);
		return ks;	  
	}
	
}
