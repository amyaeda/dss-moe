package com.iris.dss.pdfbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSSignedData;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import com.iris.dss.pdfbox.core.CreateSignatureBase;
import com.iris.dss.pdfbox.core.DssSigningData;
import com.iris.dss.pdfbox.core.IrisCMSProcessableInputStream;
import com.iris.dss.pdfbox.core.IrisCMSSignedDataGenerator;
import com.iris.dss.pdfbox.core.IrisJcaSignerInfoGeneratorBuilder;
import com.iris.dss.pdfbox.core.IrisSignerInfoGenerator; 
import com.iris.dss.pdfbox.core.DssSigningData;
import com.iris.dss.pdfbox.core.TSAClient;
import com.iris.dss.pdfbox.util.PdfSigningHelper;


public class IrisPDFBox extends CreateSignatureBase {
	public static Calendar calendar = null; //Calendar.getInstance();
	
	private SignatureOptions options;
	private PDVisibleSignDesigner visibleSignDesigner;
	private final PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
			
	static final BouncyCastleProvider BC = new BouncyCastleProvider();
	
	static final String Base_Dir = PdfSigningHelper.Base_Dir;  
	
	static final String TimeStampServer_URL = "http://172.16.252.25:8080/signserver/process?workerId=104";
	
	static final String KEYSTORE_LOCATION = Base_Dir + "keystore/signers.p12";
	static final String USB_PKCS11_LIBRARY = "C:/EScroll/eTPKCS11.dll";
	
	public static final String DebugOnly_Internal_KeyStore1 = "MIIKRgIBAzCCCf8GCSqGSIb3DQEHAaCCCfAEggnsMIIJ6DCCBWwGCSqGSIb3DQEHAaCCBV0EggVZMIIFVTCCBVEGCyqGSIb3DQEMCgECoIIE+jCCBPYwKAYKKoZIhvcNAQwBAzAaBBQMRQJdYKNc30C94dIqVSFFGpeY/gICBAAEggTIKr25m3q5SXtsaFUS6SkK9ndOB+7lRH99qNLkAPqWv3dN0SzX3We4/Z9RVZtD5ffZ7lF0Ik5Cqr1a1fRm9Fo3jZdsh8I34yPTkzQACrnR/Y4EgBS8PBpehImHDPk0leKUfCFcEoaV7zjNtTygndWnWMBiZNk93br7dXQBeygiAjVVSBJGK48wyVJ5aowzKvsGXOF1S5MrjIZaBy0LThjNjOKog1mSc+EHFY+DPzqQKYZxOiPeE6amtGq8rg2RUsB1bJWrzY8FsZzxOnz7Kd+HY0gWEkvQEq1NJjmdm3bdGSgYEM8FsU7v3cCYle5UsI5r2jATu4aI7h/blmzCnH2eATpxOg34Wbg+hC6IhkweU3hL1MepYX0V0IxiaxfnHtfd0ZqgFpG22cTgCAYKvzwdECCmfHckmXbznllMzsn2ZIazrGtChANNeIZttx2IPcr4M/ZkJqTwIZ817ra8AF0mkdWKVw2biAXfQYBfWQAl98Kflv8X5EE6WexEPjyHgC0yxTe1TnCmFhSGFIRvGD9wIwwdIKGd8tQw5DKngSl2n/pLDp+dr5HFDZ7lXRhkFvEh9j+gFh/AcGQrwn+q9qoy/fKBZwE+6EZjiLp4GqXmmrceczzhldfh74fupQ3QqZ6Q+CGC6VlTCzre4NYiUlldgcbO8hgeW1Ghf4ud/R9l2CAiFl68jQ4daZAEvMOp21y0dZ4sxjXzXGCFazjxqALCXmVxxUIgg3NevYhBolDDZa0EDrUJ5/xUQlSprwSaxGTrIBzSpNqW/YiYqCnmwirxyvlQEY/rSPv5LYqQSL9S5dyH1vi3VgIjVitUu02yDJJmCkybOp4UWutTvlFa4G+bomfjND12evtRbVUHKNyTPmF4BooKLO1gNybgbceH3HYzh0gFvw/jDnVMdp4Bnwo25HEWw3pD+xljxs8unf4iMNzJbL0aQG5Dkowkwa8CU0tof/BdcJ9quLqVhKQrNX3psk8rfgW2csYkTWYmxEZUD6h/BYL7xkxOAClQXs01u+i0mivI3KwVZITu/Fl3uS7WmCzmn+t7vc7oMU8LxZi7tzqavnuQCWNwp8j0VWAMLf5HnRriJadhG56hOzLfeWCAKU8MaLeKW5nfOVPC+PAHzILmQTdLRhtZA+UoLrsN/35jopzi3JGLbrRpvff+Gjrn9r3k7N2zfGdO9/Ll4EcWe2ENbgRrTuIxkgAAUGWZwxewJhvMeu1ITCOA918jmtNyxIESWCwBvD+8oPEzvk+2f9JVLtcufAbwRGQFhHOnpakrwNrhIh0QOoH475ABPU1T1PPg07wC3v+Z4ByZoteczIx/OkayufxW8NORyqVs9cDCASpgeOLHbOQUvH3goV+gACsMcaC+NJT008YDTEEN/z1fUebSROZQqiwfaI2dEPfFiOkgbmjqTzH2+pRzr27YUfYXBKjOnxcoOYUxjEXZkltq0bdAubP/HFfvtj2RZIkKgomfojb/GsuV79I/leVmH+T0foRFiLyM+j1ePqGVa5S+Z4yP79EQmay6NxZR5LRyXly9FDfqjjyJ/L30DTAOUvtTHQcjOJdnuqEAqIlL2yekRNPDhy6FWSW/Q2zzgzypuQtmRc6dxd1CE0w9MPdjP6nIREe0TTzPMUQwHQYJKoZIhvcNAQkUMRAeDgBzAGkAZwBuAGUAcgAxMCMGCSqGSIb3DQEJFTEWBBQhiKGYe5+FePKmEihF9k0w2bub+TCCBHQGCSqGSIb3DQEHBqCCBGUwggRhAgEAMIIEWgYJKoZIhvcNAQcBMCkGCiqGSIb3DQEMAQYwGwQUmuuzRZYhuPPLTDNKO5pazWS3SAQCAwDDUICCBCDfbgUrj/lAShkstIjhtKvAotiGRjPWGzf5AQsBxqAh5GHKYwU5vEJmle9ZN6QqTG0/IEWxdMbByv/SokkMic2TUnD36tlNh2q3dfVFHxVXPim7vZlqN6PaRjMNnSKH7FqFc0zkuMRqSjn/WAQ72C7qWgdeYhrZ/1W/y8WGf9+bBv87nn6gHlrY6QW87bZXhExDVctZD9XX54rdSYpDs9C74pjFZDoSV/q0s342ZwdzzKPZpQVBbiE0yZQkrbvMfItY24lHWVQo3UY5o7VkiwY2sjZVxPrVkFCiO5qCCJwlNiCtKDqC5ACPIJ2PsZ5eeO6GJoKzeAwJ7B4FRA9jyZ2RtDFdytQqRPEvUp+1wxGiN1xomvq2PTzKcixdbqjbZtQ2rpwDOoNCORmhmjHKi4xo/BcVnPjJO8Ji0yP5oWoxdFcSTSXV1PC9KCTLx75qcG/8a1EHswuSYYcsCMWwFUTZrlPf5+DoKFhyGEwb2T33hXXS1Eg5zJ7qIXJQhEYH8owkB3klktCDhv0Ab5R0t1hKFXB8N6wxhH3mllt4By8fVLo7wqv1+Ir2izZaBrRTL4tlvGgdj3FhMvkPxQfy9IordahkLzhqP+AeRakpKtft+PlNF0nuDiq6iHBT6if/mssHCw3IlX9zH9+shQcNSaDL7LlfQmZoeWwm1kXIp3g1EZ5cc0aA/N8Taeiq+EAlIqnvHGBaDvLgyL4GIYfbCAyg0pds5MIEmwuy8/0pSAjRAtRU/tc4LwWgFjUmp64g/j2of9/WoCfskqy62jjOHkHbUxvopq/pt4DGwyl6bQ3GDXxOo24SVPOSPpN90KDl6m4lYzw0PWsh+BYWtNwAGEuEWge2fyRJN5fa1lbWp9tW/tWhcxpm0SWWfLwop1u3+4jiqXN8pQYR4y1Lvnhd2ZJq6BIBA26PaYG0TUVnQta9m0MSCp/PC7rDm4R0s6QzVeRCUxfSZKKYaxslcJ8xDdPCQNrO1Ir5HoqdakA36z8JKD82O+MmzYmURNOzkBtazgkVcxzWTUl0wG2jAXBBlelmxbZ+h1HHWJPBvO2CEC+cBID8XCWjiVkA6lN1dkhPgQ2u48h1dtL2gz8crnicAWGdXBs93Ba/A5Jt7Iodz1LYvl8NNrKHtV+K7NBtmmv02sXoSXp5U6kwITz6bpaAoWE+hOFPgboaih9FszRbG0Sa4jnCgcZn+b9Ppiw6o8Zxizs3A/0DLtp5N1Pe1zxViiQf0/5u1wLNS0+UOi/QpcDSIf0cRT3vNtYZ32egq0W7U3XV9t58Bn7cLYfwTgCbozFWeXerktvlZuNM4vJ3WYceRTX4rpzs61tg8c8d0qVxXtCgBA+gVuh7K7dBpFTCk5W9TKe/ZwUjHvuGMHyih0nT9hs24xzssPpb2b3RiJFMHPYwPjAhMAkGBSsOAwIaBQAEFA1Salb8JWqnDja19yOT8OgiOZRwBBQyl9cvNIYPJBekxnigRFqI4p4RPwIDAYag";
	public static final String DebugOnly_Internal_KeyStore2 = "MIIKFQIBAzCCCc4GCSqGSIb3DQEHAaCCCb8Eggm7MIIJtzCCBWsGCSqGSIb3DQEHAaCCBVwEggVYMIIFVDCCBVAGCyqGSIb3DQEMCgECoIIE+zCCBPcwKQYKKoZIhvcNAQwBAzAbBBR0FAplpN09rd9tMMDbhUUw7aJh8gIDAMNQBIIEyOoMol4vPelmq99YfTRTj2kx/svL3sFVUBpxhTvWxGWwQBJl6v0xDxbhfkyymTosNTKGlBdCi8FW6ShBCvTrCrEPvLjBVltaMqHoqRYhgyF/ISXuX/0Du1Um/NHkSOntDSdpBKYCllFL6CvFZsPXpfAPVZWAsIbW7kuxBuXmgaTENhyvDuTeIgDriyNVn10snuEyJGwTNEeg/KbZmQMcCt+uhPZUvoGTUUATE1uxdtMcsMqFe+mASKPqGTIMTNSwDdcXccYbxoeYH6zz2vAikwYnvzWAcYEFePqw3J8IsTYMKXw+RvAdoxkL7Oo+a2/LL2PCfP8DDdXVXto8mQBZZZRnGW4cSPhrxTctcmhQ2f6p0wc8oRhK01a0SQDYRBdDEEM9KjxGLMCqqaOCSwHSwy0zoLoVYYf7X5cUQZbX3GRGwoxrKTCsU2dyGM/LMQvpJDAZBHk1Tp9o+GD1Jcm+g7eIq/sXwdBUDFi+bXzl7a9yh8VTyQV6td51mvSu++5+mE7IHCHY14hs7OvLUITbUAnCAnGXsmJhsKkvo/vIcssAvB1KdwCwo0WiHBZMUnFacIc1GiGR1yqLDgBn1cp09fpCqzwL2ky8QtARqVfP//5rdgfDGbg9lQ8NnfOq8kOUXblrdLnsNAJM7g/T63ixqsXdd0CKg+LfNjLMRHb6qzRa71RymDRtkND1FkpWYSMd5i5YtFkq83L2BM5diEgzKc0YssgLTdvy8xpzei4pPCu8yYvPvHTmojGdB5NStUG/FLCVY93O+fR4ZuQDbHkRdpsXbgJmz55fWt9KS/fJHlzE1g7jRGX5Wop8Cq68Da2oK9voiI1tWzUtfDU3W29DzZB1K2nDElD8yyqSqnli8qkYCrpzM2MaCW4hwtzjp/UrqtPLqNQY+is9M7peHf/dWGpugPnkI51a3z/bCc9zBMQNCaPJfknpMWicYY/Kv9MSYpS2y7peniTiAQDUKM+/RS6Q7F2RwQBkFMhkVx/3TmooriMAqKmH6P5F59Hd7SPUMa/SAb9D7YndYfodXYCgvnrVftfR8lpVqi9Oxz7FQ+cm5ZVIsD6hjsHwTuGS1I+c0Vkbb7tsk3mw99pxTWlg5Pa0pxSPHdmLoO05LSLuu+3QN6uA+d38m2hfrZHkJpX0JSaaCsOsHVhGtTs7isnpwLvN+5SQEci26o8sfPMgLgraRRQdthpVD0X+1u/6mBSL+rPQyk8ehJtI442ASZObY+5vKYaKr3J6HYqk1IigobrNzwVVDYZAsYrDsvzpsK3J0zqOxxHV6UI7b2YbWOdMj7/YN6+RntDwOBX26TG7X/MCFRj4n51e8AUUgrZg/cYSrJFFJKzoI1+1/+AIpGDzbzQ/Up7XfO8/I1twQw9nmC4DVG3KyLraBSIB7lu7qz+PIZTDT8FJJzifcSuPmwNDOrG623LIB4UkPZmWo4WqJNIyh6mAJGiaEO0k/n6hrzCzPDzqZzOkYpCLviFRkWI6fvRjxGgAvtlzg7yEH6SRplLuIeClTNf42ZRCmU81Q6G4JbVw5KhW0Lr8Wgxk7k2J/RX7/w4pNzdG/2IGNr5j+ajaOpK5pRhWLxLNyxfSp8krD6Nl3LobFVZKnY8jBLYwui4SIXEV+fdgUzFCMB0GCSqGSIb3DQEJFDEQHg4AcwBpAGcAbgBlAHIAMTAhBgkqhkiG9w0BCRUxFAQSVGltZSAxNTQ2NDI2NjAxOTQ0MIIERAYJKoZIhvcNAQcGoIIENTCCBDECAQAwggQqBgkqhkiG9w0BBwEwKQYKKoZIhvcNAQwBBjAbBBROpnZRHJcJH3/VpiqJVkk+7buWQgIDAMNQgIID8Jn+fietpsHUT0u3J3X1DzphUBzdyFCLsXOGxmJmbXnMUUCZtYp9AsPmFt9/LxC+YUi2Ol3LUpDa1pmwryjPLdFSpulReG8YiX2n7o2LYNPyhqK9uHXX+R2/cdeYAfvs8vJK1i0fqO/c6dZ0wV8iaE4JBdSEfsa2VqOBlrdaEB1mmsjG6y1nCov0sQ5qazWdPANo70V2Xm5woPZtvOuXZ6rIiYgVxSKkZkxpxxKwOnziIQuKtxliA6xmDQJs6yfm2sMKEzHDVebq/XohlFPI6JIMCsWDi5FNZ8PMXAUfOZDRspkEKqjtY2TXMXOFRlBUDQRlDpDEte6KCG58M0OV16OTPlzYgxsx/6qM7GnuZ7XADP5yMxPxmryZA4UrgzYh/A8/pZVv+OOQG05figYOXeMvu8XGopoJOiNYQ28V1m4s35FNG5yV5MPJdLeQqfdOf2/N7nXVMxHqNDn1DqALCN/3WDmCmkeagXNnYPxxnXnxJjJ+G0WaJzN4zDULotrnCEIWcGmCHTkBWjDeCm0Mnj8hMSQ6sWOT9EQ5kIxSLZ6v3xSyORa+Ov9JHgATBRee9ocIGuq9Owjyhks+RbMW96PlI/zSrGwtBCb+FrpnAj8+c3v2eGPY9F3RzOU8N15i30J2ry3X0dRk0nZTmMeJ38f6njEGYoiUMV4PGX3LSI1shDnO7+LWEAaxoGRXP68jc0SY7alGZ5U0EHqPRxPbUhJIPtjAeryIfCynvXhTwTlZYE47Z4LYZ24ph3IBz82cHmNTE3OX61t2TNu03HJ/e+mO3cjyD6fdxfdhDCW+aHMkHmfBYX7lJWY6ibY83VIjxBVhLpjZy+S1rQUhMJq48tJsNLYTFjf9ly9SPBdpi9qcKFyLC60ycurckFwx+WTfjAP5l8GU3g5tvBqbK3RTUeH9/VOngVPUthsDfNkjY1qpazj1395bdTHvIUzP3qTvizgqpttC2rb/bXzxDfVO1dv3FyBGUOLSn6Bo8yh876vRrD5TQgPDVgYR+qPY+RpDtmTsY7+2joMVW/+uYsKxyluvk1GqPwMBhaj6Or94+0Z89tUaS0rdj08NwyPmjbS8dmcuqUjZdRY4ZoUdzxTYDbyuv9N2/cry0BKUq8iRN3tjGieixevpQPlOxEaf6l9mq4tRF9Aw4en6zaIXm5ByyOFWfdx/pFTJyCbs3OBW13vpbbsWoBRFinAfWrnDHrlHCaXWzVemjgAMJhS6BBQxxQyTuUn9aYV4SD3HbYl+exATE49bLtvtf+HJm/l6/5bnVK/B+38ewbNPaTFISsCbb1QEDoHhWJFSCnjfMCZ/Q96BnNeWhfK7HUUW+vBR87kpBzA+MCEwCQYFKw4DAhoFAAQU5B5+HEnDKC7jrkRGqgRAKm99kRoEFBRSQwyZ958j6YLASwHXj5nVrcjEAgMBhqA=";
	//public static final String DebugOnly_Internal_KeyStore_Soft = "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";
	
	public static final String KS_PASSWORD = "123456";
	public static final String SIGNER_KEY_ALIAS = "signer1"; 
		
	static final boolean DEBUG = true;
	static final boolean DebugOnly_Use_Internal_KeyStore = true;
	
	static final String DebugOnly_InputFile = Base_Dir + "plain.pdf";	
	static final String DebugOnly_PDF_Out_File = Base_Dir + "sign_final.pdf";
	
	static final String signatureAlgorithm = "SHA256WITHRSA";
	static final String digestAlgorithm = "SHA-256";
		
	public static final String SIGNING_TIME = "SIGNING_TIME";
	
	public DssSigningData dssSigningData = new DssSigningData();
	
	//public void syncSigningTime(Date date) {
	//	calendar.setTime(date);
	//}
	
	public void setVisibleSignatureProperties(
			String filename, int x, int y, int zoomPercent, FileInputStream image, int page) throws IOException	{
		
		visibleSignDesigner = new PDVisibleSignDesigner(filename, image, page);
		visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).signatureFieldName("signature");		
	}
	
	public void setVisibleSignatureProperties(
			String filename, int x, int y, float width, float height, FileInputStream image, int page) throws IOException	{
		
		visibleSignDesigner = new PDVisibleSignDesigner(filename, image, page);
		visibleSignDesigner.xAxis(x).yAxis(y).width(width).height(height).signatureFieldName("signature");		
	}

	public void setSignatureProperties(String name, String location, String reason, int preferredSize, 
			int page, boolean visualSignEnabled) throws IOException	{
		
		visibleSignatureProperties
		.signerName(name)
		.signerLocation(location)
		.signatureReason(reason)
		.preferredSize(preferredSize)
		.page(page)
		.visualSignEnabled(visualSignEnabled)
		.setPdVisibleSignature(visibleSignDesigner)
		.buildSignature();
		
	}

	public IrisPDFBox() throws Exception { 
		// Load default internal KeyStore
		KeyStore keystore = KeyStore.getInstance("PKCS12");		
		keystore.load(new ByteArrayInputStream(Base64.decode(DebugOnly_Internal_KeyStore1)), KS_PASSWORD.toCharArray());		
		setPrivateKey((PrivateKey) keystore.getKey(SIGNER_KEY_ALIAS, KS_PASSWORD.toCharArray()));
		setCertificate(keystore.getCertificateChain(SIGNER_KEY_ALIAS)[0]);		
		setCertificateChain(keystore.getCertificateChain(SIGNER_KEY_ALIAS));
		
		calendar = Calendar.getInstance();
	}
		
	public IrisPDFBox(KeyStore keystore, char[] pin, String alias) throws Exception
	{
		setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
		setCertificate(keystore.getCertificateChain(alias)[0]);		
		setCertificateChain(keystore.getCertificateChain(alias));
	}	
	

	/**
	 * Sign pdf file 
	 */
	public PDDocument signDocument(PDDocument doc, String signer, TSAClient tsa) throws Exception 
	{		
		setTsaClient(tsa);
		
		// create signature dictionary
		PDSignature signature = new PDSignature();
		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
		// subfilter for basic and PAdES Part 2 signatures
		signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
		signature.setName(signer);
		signature.setReason("Integrity And Authenticity");
		signature.setLocation("Wilayah Persekutuan");

		
		//calendar.set(2019, 1, 1, 1, 1, 1);
        
		// the signing date, needed for valid signature
		signature.setSignDate(calendar);
		
		// register signature dictionary and sign interface
		if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled())
		{
			options = new SignatureOptions();
			options.setVisualSignature(visibleSignatureProperties);
			options.setPage(visibleSignatureProperties.getPage() - 1);
			doc.addSignature(signature, this, options);
		}
		else
		{
			doc.addSignature(signature, this);
		}

		return doc;
	}

	public byte[] getTimeStampingSignedData(Provider p, boolean useTimeStamp, Date signingTime) throws Exception {
		MessageDigest digest1 = MessageDigest.getInstance(digestAlgorithm, "BC");        
        TSAClient  tsa = null;       
        
        if(useTimeStamp) {
        	tsa = new TSAClient( new URL(TimeStampServer_URL), "username", "password", digest1);
        }
        
        // set timestamp server client
        setTsaClient(tsa);        
        
		InputStream rawcontent = new FileInputStream(Base_Dir + "rawcontent.txt");

		InputStream digestStream = new FileInputStream(Base_Dir + "digest.txt");
		byte[] digest = IOUtils.toByteArray(digestStream);		
		
		InputStream signedBytesStream = new FileInputStream(Base_Dir + "mysignature_without_timestamp.txt");
		byte[] signedBytesBase64 = IOUtils.toByteArray(signedBytesStream);
		byte[] signedAttr = Base64.decode(signedBytesBase64);
		
		List<Certificate> certList = new ArrayList<Certificate>();
		//certList.add(super.getCertificate());
		for (Certificate cert : super.getCertificateChain())
		{
			certList.add(cert);
		}
		
		Store certs = new JcaCertStore(certList);
		IrisCMSSignedDataGenerator gen = new IrisCMSSignedDataGenerator();
		org.bouncycastle.asn1.x509.Certificate cert = org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(super.getCertificate().getEncoded()));
		ContentSigner sha1Signer = new JcaContentSignerBuilder(signatureAlgorithm).build(super.getPrivateKey());
		gen.addSignerInfoGenerator(new IrisJcaSignerInfoGeneratorBuilder(
				new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(cert)));
		gen.addCertificates(certs);
		
		IrisCMSProcessableInputStream msg = new IrisCMSProcessableInputStream(rawcontent);
		CMSSignedData signedData = gen.constructSignedData(msg, digest, signedAttr, signingTime);
    	if (super.getTsaClient() != null) {
            signedData = signTimeStamps(signedData);
        }                
    	
    	byte[] signedDataBytes = signedData.getEncoded();
    	String signature = new COSString(signedDataBytes).toHexString();
    	System.out.println(">>> user signature:\n"+ signature);
    	
    	FileWriter fw = new FileWriter(Base_Dir + "mysignature_with_timestamp_coshexstring.txt");
    	fw.write(signature);
    	fw.flush();
    	fw.close();
		
    	FileWriter os2 = new FileWriter(Base_Dir + "mysignature_with_timestamp_base64.txt");
		os2.write( Base64.toBase64String(signedDataBytes) );
		os2.flush();
		os2.close();
		
		
        return signedDataBytes;
	}
	
	public Provider readSigningKey(boolean soft, String softtokAlias, String slot, String pwd) throws Exception {
		
		if(soft) 
		{
			Provider p = new BouncyCastleProvider();
			Security.addProvider(p);	
			char[] pin = pwd.trim().toCharArray();
			File ksFile = new File(KEYSTORE_LOCATION);
			KeyStore ks = KeyStore.getInstance("PKCS12", p);			
			ks.load(new FileInputStream(ksFile), pin);			
			PrivateKey signerPrivK = (PrivateKey) ks.getKey(softtokAlias, pin);		
			Certificate cert = ks.getCertificate(softtokAlias);
			
			setPrivateKey(signerPrivK);
			setCertificate(cert);
			setCertificateChain(ks.getCertificateChain(softtokAlias));
			
			return p;
		}
		
		System.out.println(">>> Reading key from USB token... ");
	 	
		char[] pin = pwd.toCharArray();			
		Provider p = new sun.security.pkcs11.SunPKCS11( PdfSigningHelper.getKeyStoreInitStream(slot, USB_PKCS11_LIBRARY));			        
			
		Security.addProvider(p);	
		
		KeyStore ks = KeyStore.getInstance("PKCS11", p);			
		ks.load(null, pin);			
		
		String alias = PdfSigningHelper.getKeyLabel(ks, slot);
		
		PrivateKey signerPrivK = (PrivateKey) ks.getKey(alias, pin);		
		Certificate cert = ks.getCertificate(alias);
		
		//System.out.println(">>> USB token chairman Private Key: \n"+ signerPrivK);
		System.out.println(">>> Signer's cert: \n"+ ((X509Certificate) cert).getSubjectX500Principal().getName());
		
		setPrivateKey(signerPrivK);
		setCertificate(cert);
		setCertificateChain(ks.getCertificateChain(alias));
		
		return p;
	}

	public static List<Integer> getSignatureByteRange(byte[] buffers, String signerAlias) throws Exception {		
		List<Integer> ret = new ArrayList<Integer>();
		
		//PDDocument pdDocument = doc;
		//FileInputStream oldFileStream = new FileInputStream(input);
		PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(buffers)); //oldFileStream);

		COSDictionary trailer = pdDocument.getDocument().getTrailer();
		COSDictionary documentCatalog = (COSDictionary) trailer.getDictionaryObject(COSName.ROOT);			 
		COSDictionary acroForm = (COSDictionary) documentCatalog.getDictionaryObject(COSName.ACRO_FORM);		
		COSArray fields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);
		
		for (int fieldIdx = 0; fieldIdx < fields.size(); fieldIdx++) {
			COSDictionary field = (COSDictionary) fields.getObject(fieldIdx);
			String fieldType = field.getNameAsString("FT");
			
			if ("Sig".equals(fieldType)) {
				COSDictionary signatureDictionary = (COSDictionary) field.getDictionaryObject(COSName.V);		
				COSBase cosname = signatureDictionary.getDictionaryObject(COSName.NAME);
				String name = ((COSString) cosname).getString();
				if(name.trim().equalsIgnoreCase(signerAlias)) {
					//System.err.println("COS Name: " + name);
					COSBase byterange = signatureDictionary.getDictionaryObject(COSName.BYTERANGE);
					COSArray array = (COSArray) byterange;
//					System.err.println(name + ", Byte Range: " + array.getInt(1));
					ret.add(array.getInt(0));
					ret.add(array.getInt(1));
					ret.add(array.getInt(2));
					ret.add(array.getInt(3));
					
					return ret;
				}				
			}
		}        
		pdDocument.close();
		
		return null;
	}
		
	public static byte[] sha256WithRsaSign(Provider p, PrivateKey key, byte[] rawBytes) throws Exception {
				
		Signature sig = Signature.getInstance(signatureAlgorithm, p);     
        sig.initSign( key ); 
        sig.update(rawBytes);   
        byte[] buffers = sig.sign();
		
		return buffers;
	}
	
	public byte[] doSignaturePadding(byte[] unPaddedSignature) {
		return Arrays.copyOf(unPaddedSignature, 18944);
	}
		
	@Override
	public byte[] sign(InputStream content) throws IOException
    {
		try	{   	 
			
			final Date signingTime = IrisPDFBox.calendar.getTime();
			
			List<Certificate> certList = new ArrayList<Certificate>();
			//certList.add(super.getCertificate());			
			// Set your signer certificate here
			for (Certificate cert : super.getCertificateChain()) {
				certList.add(cert);
			}
			
			IrisCMSSignedDataGenerator signedDataGenerator = new IrisCMSSignedDataGenerator();
			
			org.bouncycastle.asn1.x509.Certificate cert = 
				org.bouncycastle.asn1.x509.Certificate.getInstance( ASN1Primitive.fromByteArray(super.getCertificate().getEncoded()));
						
			ContentSigner contentSigner =  //new JcaContentSignerBuilder(signatureAlgorithm).build(super.getPrivateKey());
				new ContentSigner() { //private SignatureOutputStream stream = new SignatureOutputStream(sig);
					private ByteArrayOutputStream stream = new ByteArrayOutputStream();					
	                public AlgorithmIdentifier getAlgorithmIdentifier() { return new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm); }	
	                public OutputStream getOutputStream() { return stream; }	
	                public byte[] 		getSignature() { return stream.toByteArray(); }
	            };
			
			signedDataGenerator.addSignerInfoGenerator(
					new IrisJcaSignerInfoGeneratorBuilder(
								new JcaDigestCalculatorProviderBuilder().build()
							).build(contentSigner, new X509CertificateHolder(cert)));
			
			signedDataGenerator.addCertificates(new JcaCertStore(certList));

			byte[] contentBuffers = PdfSigningHelper.toByteArray(content);
			//System.out.println("content: "+ Base64.toBase64String(contentBuffers));
			
			IrisCMSProcessableInputStream cmsTypedData = new IrisCMSProcessableInputStream(new ByteArrayInputStream(contentBuffers));           

			// store content in byte array            

			//CMSSignedData signedData = gen.generate3(msg, false);
			IrisSignerInfoGenerator signerInfoGen = signedDataGenerator.getUnsignedBuffers(cmsTypedData, false);
			byte[] pdfBytes = signerInfoGen.getSignedBuffers(cmsTypedData.getContentType(), signingTime);
			byte[] calcDigest = signerInfoGen.getCalculatedDigest();
			
			// TODO cache CA + user cert at local disk or DB
			// TODO create table Certificates - columns { id, userId, issuercert, usercert }
			// TODO create table tblPDFSigningTran -
            //      colums = { id, userId, content(blob), rawBytes(blob), digest(blob), signingTime(timestamp) }
			// TODO save content to DB
			// TODO save rawBytes to DB
			// TODO save digest to DB
			// TODO save signingTime to DB
						
			this.dssSigningData.setContentBytes( contentBuffers );
			this.dssSigningData.setRawBytes(pdfBytes);
			this.dssSigningData.setDigestBytes(calcDigest);
			this.dssSigningData.setSigningTime(signingTime);
			
			byte[] signature = { 0,0,0,0,0,0,0,0 }; 
			if(DEBUG && DebugOnly_Use_Internal_KeyStore) {
				// Simulate signing process. Actual process is perform signing in card 			
				signature = sha256WithRsaSign(BC, getPrivateKey(), this.dssSigningData.getRawBytes());	
			}
			
			this.dssSigningData.setSignature(signature);
					
			CMSSignedData signedData = signedDataGenerator.constructSignedData(cmsTypedData, this.dssSigningData.getDigestBytes(), this.dssSigningData.getSignature(), signingTime);
						
			// Skip time-stamping 
			//if (super.getTsaClient() != null) {
			//	signedData = signTimeStamps(signedData);
			//}         

			// TODO return a dummy CMSSignedData ?
			
			return signedData.getEncoded();

		}
		catch (Exception e)
		{
			throw new IOException("sign(...) failed. " + e.getMessage());
		}
    }
	
	public byte[] buildCMSSignedData(DssSigningData data) throws IOException
    {
		try	{   	 
			
			List<Certificate> certList = new ArrayList<Certificate>(); 
			
			// Set your signer certificate here
			for (Certificate cert : super.getSignerCertificateChain()) {
				certList.add(cert);
			}
			
			IrisCMSSignedDataGenerator signedDataGenerator = new IrisCMSSignedDataGenerator();
			
			org.bouncycastle.asn1.x509.Certificate cert = 
				org.bouncycastle.asn1.x509.Certificate.getInstance( ASN1Primitive.fromByteArray(super.getSignerCertificate().getEncoded()));
						
			ContentSigner contentSigner =  //new JcaContentSignerBuilder(signatureAlgorithm).build(super.getPrivateKey());
				new ContentSigner() { //private SignatureOutputStream stream = new SignatureOutputStream(sig);
					private ByteArrayOutputStream stream = new ByteArrayOutputStream();					
	                public AlgorithmIdentifier getAlgorithmIdentifier() { return new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm); }	
	                public OutputStream getOutputStream() { return stream; }	
	                public byte[] 		getSignature() { return stream.toByteArray(); }
	            };
			
			signedDataGenerator.addSignerInfoGenerator(
					new IrisJcaSignerInfoGeneratorBuilder(
								new JcaDigestCalculatorProviderBuilder().build()
							).build(contentSigner, new X509CertificateHolder(cert)));
			
			signedDataGenerator.addCertificates(new JcaCertStore(certList));

			IrisCMSProcessableInputStream cmsTypedData = new IrisCMSProcessableInputStream(new ByteArrayInputStream(data.getContentBytes()));           
			
			CMSSignedData signedData = signedDataGenerator.constructSignedData(cmsTypedData, data.getDigestBytes(), data.getSignature(), data.getSigningTime());
			
			return signedData.getEncoded();

		}
		catch (Exception e)
		{
			throw new IOException("sign(...) failed. " + e.getMessage());
		}
    }
				
	public void processDocument(byte[] buffers, String debugOutFile) throws Exception {		
		PDDocument doc = PDDocument.load(new ByteArrayInputStream(buffers)); 	
		// below will trigger the base class sign(...) function
		doc = signDocument(doc, "signer1", null);        			
        doc.saveIncremental( new FileOutputStream(debugOutFile)); 
        doc.close();        
	}
	
	public byte[] processDocument(byte[] buffers) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDDocument doc = PDDocument.load(new ByteArrayInputStream(buffers)); 	
		// below will trigger the base class sign(...) function
		doc = signDocument(doc, "signer1", null);        			
        doc.saveIncremental( baos ); 
        doc.close();   
        
        return baos.toByteArray();
	}
	
	public byte[] processDocument(PDDocument pdfDoc) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDDocument doc = pdfDoc; 	
		// below will trigger the base class sign(...) function
		doc = signDocument(doc, "signer1", null);        			
        doc.saveIncremental( baos ); 
        doc.close();   
        
        return baos.toByteArray();
	}
			
	public static void main(String[] args) throws Exception	{ 		
		Security.addProvider(BC);
		
		//final String signerCert = "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";
		//System.out.println( PdfSigningHelper.validatePDF( 
		//			PdfSigningHelper.toByteArray(new FileInputStream("C:/Users/trlok/Desktop/validate.pdf")),
		//			(X509Certificate) PdfSigningHelper.readDataAsCertificate(Base64.decode(signerCert) )
		//		));
				
		System.out.println(new Date().getTime());
		byte[] raw = Base64.decode("fSqRr1g091ilXm9DXsLPjxmm072svU/H049td5pJLGvD4GAbQD4mJmWWNRokCPi/T31xFG9XJdjqaXO9gUEFY8IpN8DgZkM62w/DKrdqOaRtMXS6w3bv8lqrHgMeM2onFzhOG6oBIi32j6IvdcR8/mbIEPn+JGSZ3/4z+wOteoZ/0QpszbMIzjiIUL5Ge9/b69fzcYaOzLHq2ES2MzQYgrUvdBu2z4cDw4cyI5uSlUuqIv5QBETwh+HVrd9/ZuSgO5b+ioONi+vIOBM89zqyqQKPdF9/SzU6uX1w6K7HSTrB+9X7dY8/HZRbwi8eWtd5ovtkTMCwFL0ZNcYztzVBrw==");
		System.out.println(raw.length);
		
		// Load KeyStore	
		KeyStore keystore = KeyStore.getInstance("PKCS12");		
		keystore.load(new ByteArrayInputStream(Base64.decode(DebugOnly_Internal_KeyStore2)), KS_PASSWORD.toCharArray());
		//keystore.load(new FileInputStream("C:/Users/trlok/Desktop/pdfsigner.p12"), KS_PASSWORD.toCharArray());
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//keystore.store(baos, KS_PASSWORD.toCharArray());
		//System.out.println( Base64.toBase64String(baos.toByteArray()) );
		
		// Sign document
//        IrisPDFBox api = new IrisPDFBox(keystore, KS_PASSWORD.toCharArray(), SIGNER1_KEY_ALIAS);         
//        api.processDocument( PdfSigningHelper.toByteArray(new FileInputStream(DebugOnly_InputFile)), DebugOnly_PDF_Out_File);	
        
        IrisPDFBox api22 = new IrisPDFBox(keystore, KS_PASSWORD.toCharArray(), SIGNER_KEY_ALIAS);   
        // TODO save api.signingData to DB
        byte[] signed = sha256WithRsaSign(BC, api22.getPrivateKey(), raw);
//        byte[] signed = sha256WithRsaSign(BC, api22.getPrivateKey(), api.signingData.getRawBytes());
        System.out.println( Base64.toBase64String(signed) );
//                
//        List<Integer> byteRange = getSignatureByteRange(PdfSigningHelper.toByteArray(new FileInputStream(DebugOnly_PDF_Out_File)), SIGNER1_KEY_ALIAS);
//        byteRange.forEach((i) -> { System.out.println(i); });
//        
//        byte[] out = 
//        PdfSigningHelper.replacePDFSignature(
//        		byteRange, 
//        		api22.buildCMSSignedData(api.signingData), 
//        		PdfSigningHelper.toByteArray(new FileInputStream(DebugOnly_PDF_Out_File)) );
//       
//        FileOutputStream fos = new FileOutputStream(Base_Dir + "after_replaced_signature.pdf");
//        fos.write(out);
//        fos.flush();
//        fos.close();
//        System.out.println(">>> ---- END ----");	
               
								
	}

}
