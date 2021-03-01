package com.iris.dss.pdfbox.util;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Matrix;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPUtil;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import com.iris.dss.pdfbox.core.Pkcs11Wrapper;

public class PdfSigningHelper {

	public static final String Base_Dir = "C:/Users/trlok/Desktop/";
	
	static final String signatureAlgorithm = "SHA256WITHRSA";
	static final String digestAlgorithm = "SHA-256";
	
	static final String OcspServer_Url = "http://172.16.252.43:8080/ejbca/publicweb/status/ocsp";
	//static final String TimeStampServer_URL = "http://172.16.252.25:8080/signserver/process?workerId=104";
	
	public static Certificate readDataAsCertificate(byte[] data) throws Exception {
		try{
			Security.addProvider(new BouncyCastleProvider());
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(new ByteArrayInputStream(data));
			return cert;
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}

	}
	
	public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[4096];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
		
	public static byte[] readInputStreamtoBuffer(final InputStream in) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
	    int len = 0;
	    final byte[] buf = new byte[1024];
	
	    while ((len = in.read(buf)) > 0) {
	        os.write(buf, 0, len);
	    }
	
	    in.close();
	    os.close();
	
	    return os.toByteArray();
	}
	
	public static byte[] replacePDFSignature(List<Integer> byteRange, byte[] rawsignature, byte[] buffers) throws Exception {
		
		//FileInputStream fis = new FileInputStream(input);
		//PDDocument pdDocument = PDDocument.load(fis);
		
		String signatureHex = new COSString(rawsignature).toHexString();
		byte[] signature = signatureHex.getBytes(Charsets.ISO_8859_1);
		
		final byte[] TAG1 = { 0x3C }; // <
		final byte[] TAG2 = { 0x3E }; // >
				
		//FileOutputStream os = new FileOutputStream(outFile);
		ByteArrayOutputStream os = 	new ByteArrayOutputStream();
		
		int length = byteRange.get(2) - byteRange.get(1) - 2;
		int posBefore = byteRange.get(1);
		int posAfter = byteRange.get(2);
		
		//byte[] buffer = toByteArray(fis);	
				
		// Pad or trim signature buffer
		byte[] newsignature = new byte[length]; //Arrays.copyOf("0".getBytes(Charsets.ISO_8859_1), length);	
		byte[] zero = "0".getBytes(Charsets.ISO_8859_1);
		System.arraycopy(signature, 0, newsignature, 0, signature.length);
		Arrays.fill(newsignature, signature.length, length, zero[0]);
		
		COSString string = new COSString(newsignature);
        string.setForceHexForm(true);
        newsignature = string.getBytes();
//		System.out.println("1: "+ newsignature.length);
//		System.out.println("2: "+ buffers.length);
		
		// Overwrite signature into buffer	
		byte[] newbuffer = new byte[buffers.length];
		System.arraycopy(buffers, 0, newbuffer, 0, posBefore);
		// set '<' tag
		System.arraycopy(TAG1, 0, newbuffer, posBefore, 1);		
		System.arraycopy(newsignature, 0, newbuffer, posBefore+1, newsignature.length);
		// set '>' tag
		System.arraycopy(TAG2, 0, newbuffer, (posBefore + newsignature.length + 1), 1);
		System.arraycopy(buffers, posAfter, newbuffer, (posBefore + 1) + (newsignature.length +1), buffers.length-(posAfter));
		//System.out.println(new String(newbuffer));	
		
		os.write(newbuffer);
		os.flush();		
		os.close();	
		
		//fis.close();	
		return os.toByteArray();
	}
	
	public static void readPDF(PDDocument doc) throws Exception {
		
		Security.addProvider(new BouncyCastleProvider());
		
		PDDocument pdDocument = doc;		
		// Reading the signature using Apache PDFBox.	
		COSDictionary trailer = pdDocument.getDocument().getTrailer();
		trailer.setNeedToBeUpdated(true);
		/*
		 * PDF Reference - third edition - Adobe Portable Document Format -
		 * Version 1.4 - 3.6.1 Document Catalog
		 */
		COSDictionary documentCatalog = (COSDictionary) trailer.getDictionaryObject(COSName.ROOT);		
		// 8.6.1 Interactive Form Dictionary		 
		COSDictionary acroForm = (COSDictionary) documentCatalog.getDictionaryObject(COSName.ACRO_FORM);		
		COSArray fields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);
		
		for (int fieldIdx = 0; fieldIdx < fields.size(); fieldIdx++) {
			COSDictionary field = (COSDictionary) fields.getObject(fieldIdx);
			String fieldType = field.getNameAsString("FT");
			
			if ("Sig".equals(fieldType)) {
				COSDictionary signatureDictionary = (COSDictionary) field.getDictionaryObject(COSName.V);
				PDSignature pdsig = new PDSignature(signatureDictionary);		
				
				InputStream pdfInputStream = new FileInputStream(Base_Dir + "sign_final.pdf");
				byte[] pdfbuffers = toByteArray(pdfInputStream);
				byte[] signedContent = pdsig.getContents(pdfbuffers);
				//ASN1Dump.dumpAsString(DERSequence.fromByteArray(signedContent));
				COSString contentCosStr = new COSString(signedContent); 
				//ASN1Dump.dumpAsString(DERSequence.fromByteArray(contentCosStr.getBytes()));
				CMSSignedData signedData1 = new CMSSignedData(contentCosStr.getBytes());
												
				//Create a CMSProcessable object, specify any encoding, I have used mine 
	            CMSProcessable signedContent2 = new CMSProcessableByteArray(pdsig.getSignedContent(pdfbuffers) );
	            //Create a InputStream object
	            InputStream is = new ByteArrayInputStream(contentCosStr.getBytes());
	            //Pass them both to CMSSignedData constructor
	            CMSSignedData signedData2 = new CMSSignedData(signedContent2, is);
	            
	            
				// Verify signature
		        Store store = signedData2.getCertificates(); 
		        SignerInformationStore signers = signedData2.getSignerInfos(); 
		        Collection c = signers.getSigners(); 
		        Iterator it = c.iterator();
		        while (it.hasNext()) { 
		            SignerInformation signerInfo = (SignerInformation) it.next(); 
		            Collection certCollection = store.getMatches(signerInfo.getSID()); 
		            Iterator certIt = certCollection.iterator();
		            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
		            X509Certificate signer1cert = new JcaX509CertificateConverter().getCertificate(certHolder);
		           
		            Signature sig = Signature.getInstance(signatureAlgorithm, "BC");
		            sig.initVerify(signer1cert);
		            sig.update(signerInfo.getEncodedSignedAttributes());
		            boolean valid = sig.verify(signerInfo.getSignature());
		            System.out.println("Signature verified 1 - "+ valid);		                       
		            
		            if(signerInfo.verify(
		            		new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(signer1cert)) ) 
		            {
		                System.out.println("Signature verified 2");
		            } else {
		                System.out.println("Signature verification 2 failed");
		            }
		        }
		        
		        
		        
				// TABLE 8.60 Entries in a signature dictionary				 
//				COSString signatoryName = (COSString) signatureDictionary.getDictionaryObject(COSName.NAME);
//				if (null != signatoryName) {
//					System.err.println("signatory name: " + signatoryName.getString());
//				}	
		        
//				COSBase sigContent = signatureDictionary.getDictionaryObject(COSName.CONTENTS);
//				System.err.println("Signature: " + new String(((COSString) sigContent).getString()));
//				COSBase byterange = signatureDictionary.getDictionaryObject(COSName.BYTERANGE);
//				System.err.println("byte range: " + ((COSArray) byterange).getInt(1));
			}
		}        
		doc.close();
	}
	
	public static boolean validatePDF(byte[] buffers, X509Certificate signerCert) throws Exception {		
		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);
		
		PDDocument pdDocument = PDDocument.load( new ByteArrayInputStream(buffers) ); 			
		COSDictionary trailer = pdDocument.getDocument().getTrailer();
		//trailer.setNeedToBeUpdated(true);
		COSDictionary documentCatalog = (COSDictionary) trailer.getDictionaryObject(COSName.ROOT);	
		COSDictionary acroForm = (COSDictionary) documentCatalog.getDictionaryObject(COSName.ACRO_FORM);		
		COSArray fields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);
		
		for (int fieldIdx = 0; fieldIdx < fields.size(); fieldIdx++) {
			COSDictionary field = (COSDictionary) fields.getObject(fieldIdx);
			String fieldType = field.getNameAsString("FT");
			
			if ("Sig".equals(fieldType)) 
			{
				COSDictionary signatureDictionary = (COSDictionary) field.getDictionaryObject(COSName.V);
				PDSignature pdsig = new PDSignature(signatureDictionary);		
				byte[] contents = pdsig.getContents(buffers); 
				//ASN1Dump.dumpAsString(DERSequence.fromByteArray(signedContent));						
				//Create a CMSProcessable object, specify any encoding
	            CMSProcessable csmp = new CMSProcessableByteArray( pdsig.getSignedContent(buffers) ); 	        
	            //Pass them both to CMSSignedData constructor
	            CMSSignedData cmsSignedData = new CMSSignedData(csmp, new ByteArrayInputStream(new COSString(contents).getBytes()));
	            
		        Store store = cmsSignedData.getCertificates(); 
		        SignerInformationStore signers = cmsSignedData.getSignerInfos(); 
		        Collection c = signers.getSigners(); 
		        Iterator it = c.iterator();
		        while (it.hasNext()) { 
		            SignerInformation signerInfo = (SignerInformation) it.next(); 
		            boolean valid = signerInfo.verify( new JcaSimpleSignerInfoVerifierBuilder().setProvider(BC).build(signerCert));  
		            System.out.println(">validatePDF> valid:"+ valid +"|SubjectDN:"+ signerCert.getSubjectDN().getName());	
	            	return valid;
	            	
	            	// TODO - Read signer cert from signer info object
		            //Collection certCollection = store.getMatches(signerInfo.getSID()); 
		            //Iterator certIt = certCollection.iterator();
		            //X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
		            //X509Certificate signer1cert = new JcaX509CertificateConverter().getCertificate(certHolder);
		           
		            // TODO Method 1 - Verify PDF signature
		            //Signature sig = Signature.getInstance(signatureAlgorithm, BC);
		            //sig.initVerify(signer1cert);
		            //sig.update(signerInfo.getEncodedSignedAttributes());
		            //boolean valid = sig.verify(signerInfo.getSignature());
		            //System.out.println(">validatePDF> Signature valid: "+ valid);
		            
		            // TODO Method 2 - Verify that the given verifier can successfully verify the signature on this SignerInformation object.
		            		            
		            //try {
		            	// TODO - Check Time-Stamp		            	
		            	//Collection<TimeStampToken> tokens = TSPUtil.getSignatureTimestamps(signerInfo, new JcaDigestCalculatorProviderBuilder().build());
		            	//Iterator<TimeStampToken> tokenIter = tokens.iterator();
		            	//TimeStampToken token1 = null;
		            	//if(tokenIter.hasNext()) {
		            	//	token1 = tokenIter.next();
		            	//}		            	
		            	//Date timeStampDate = getSignatureTimestamp(token1);		            	
		            	//System.out.println(">>> TimeStamp Date: " + timeStampDate);
		            	//Collection<org.bouncycastle.cert.X509CertificateHolder> tsCerts = getTimestampCert(token1);		            	
		            	//X509Certificate tsCert1 = new JcaX509CertificateConverter().getCertificate(tsCerts.iterator().next());
		            	//System.out.println(">>>  TimeStamp Cert: " + tsCert1.getSubjectDN().getName());
		            			            	
		            	//boolean isCertValid = isValidTimeStampingCert(tsCert1);
		            	//System.out.println(">>>  Cert Valid: " + isCertValid);		            	
		            	//boolean isTokenValid = validateTimestampToken(token1, tsCert1, BC);
		            	//System.out.println(">>>  Token Valid: " + isTokenValid);		            			 
		            	// TODO - OCSP checking (REQUIRED a valid OCSP Server URL)		            	
		            	//X509Certificate issuerCert = 
		            	//		(X509Certificate) CertificateFactory.getInstance("X.509")
		            	//		.generateCertificate(new FileInputStream(Base_Dir + "ISClientMANAGER.cacert.crt"));		            			            	
		            	//OCSPClient ocspClient = OCSPClient.getInstance();
		            	//boolean ret = ocspClient.request(issuerCert, tsCert1, OcspServer_Url);
		            	//System.out.println("Ocsp - Return: " + ret);
		            	//System.out.println("Ocsp - Status: " + ocspClient.getStatus());
		            	//System.out.println("Ocsp - Error: " + ocspClient.getError());		            			            	
		            //} catch(CMSException e) {	            		
		            //}
		        } // END WHILE LOOP		        
			} // END IF
		} // END FOR 
		
		pdDocument.close();
		
		return false;
	}
	
	public static boolean validateTimestampToken(TimeStampToken token, X509Certificate x509Cert, Provider prov) {		
		try {
			SignerInformationVerifier signerInfoVerifier = 
				new JcaSimpleSignerInfoVerifierBuilder().setProvider(prov).build(x509Cert);							
			token.validate(signerInfoVerifier);			
			return true;
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		} 
	}
	
	public static Date getSignatureTimestamp(TimeStampToken token) {		
		try {
			// Fetches the signature time-stamp attributes from a SignerInformation object. 
			//Collection<TimeStampToken> cols = TSPUtil.getSignatureTimestamps(info, prov);
			//for(TimeStampToken token : cols) {				
			return token.getTimeStampInfo().getGenTime();
			//}			
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return null; 
	}
	
	public static Collection<org.bouncycastle.cert.X509CertificateHolder> getTimestampCert(TimeStampToken token) {		
		try {			
			return token.getCertificates().getMatches(token.getSID());					
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return null;
	}
	
	public static boolean isValidTimeStampingCert(Certificate cert) {
		try {
			X509CertificateHolder holder = new X509CertificateHolder(cert.getEncoded());
			// Validate the passed in certificate as being of the correct type to be used for time stamping.
			TSPUtil.validateCertificate(holder);			
			return true;
		} catch(Exception e1) {
			e1.printStackTrace();
			return false;
		}
	}
		
	public static String getKeyLabel(KeyStore ks, String pin) throws Exception {
		boolean DEBUG_MODE = true;		
		String strAlias = "";		
		Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			String alias1 = aliases.nextElement();	
			if( null != ks.getKey(alias1, pin.toCharArray()) ) {				
				strAlias = alias1;			    	
				// Stop at the first private key we found		    	
				if(DEBUG_MODE) {		    		
					System.out.println(">Found key for " + alias1 + "\n");						
				}					
			}
		}		
		return strAlias;
	}
	
	public static InputStream getKeyStoreInitStream(String slot, String sharedLibrary) throws Exception {
		try {	    
	        StringBuffer sb = new StringBuffer();
	        sb.append("name = pkcs11.cryptoki.slot."+slot.trim());sb.append("\n");
	        sb.append("library = "+ sharedLibrary.trim());sb.append("\n"); 	
	        sb.append("slot = "+slot.trim());sb.append("\n");
	        return new ByteArrayInputStream(sb.toString().getBytes());	       
		} catch (Exception e) {
			throw e;
		}
	}
		
	public static String getPkcs11Slot(String sharedLibFilePath) throws Exception {
		String slotNum = "-1";
		
		File sharedLib = new File(sharedLibFilePath);
		
		Pkcs11Wrapper p11Wrapper = null;
		
		try {
			p11Wrapper = Pkcs11Wrapper.getInstance(sharedLib);			
		} catch(Exception e1) {
			System.err.println(">Connect to USB Token failed. \n");
			System.err.println(">Please ensure the USB Token is connected properly. \n");
			throw e1;
		}
		
		long[] slots = p11Wrapper.getSlotList();
		
		System.out.println("*** Slot List *** ");
		if(null != slots && slots.length > 0) {
			for(long slot : slots) {
				System.out.println(
						"Number:" + slot + " \nLabel:" + String.valueOf(p11Wrapper.getTokenLabel(slot)) );
				
				slotNum = String.valueOf(slot);
				
				break;
			} 
		} else {
			//btnSign.setEnabled(true);
			throw new Exception("No slot found. Please ensure USB token is connected.");
		}		
		
		return slotNum;
		
	}
	
	public static byte[] doSigning(Provider p, PrivateKey key) throws Exception {
		
		InputStream rawSignedAttrStream = new FileInputStream(Base_Dir + "rawsignedAttr.txt");
		byte[] rawBase64SignedAttr = IOUtils.toByteArray(rawSignedAttrStream);
		byte[] rawBytes = Base64.decode(rawBase64SignedAttr);
		
		Signature sig = Signature.getInstance(signatureAlgorithm, p);     
        sig.initSign( key ); //(PrivateKey) keystore.getKey(ALIAS, pin) );
        sig.update(rawBytes);   
        byte[] buffers = sig.sign();
        
        FileWriter fw1 = new FileWriter(Base_Dir + "mysignature_without_timestamp.txt");
        fw1.write(Base64.toBase64String(buffers));
        fw1.flush();
        fw1.close();
		
		return buffers;
	}
	
	public static void createTestDocument(String pdfFile) throws Exception {
		PDDocument doc = new PDDocument(); 
		PDPage page = new PDPage(PDRectangle.A4);		
		doc.addPage(page);		
		PDPageContentStream pdfContent = new PDPageContentStream(doc, page, true, true);
		pdfContent.beginText();		
		pdfContent.setFont( PDType1Font.TIMES_ITALIC, 22 );
		pdfContent.setNonStrokingColor(Color.BLACK);
		pdfContent.setTextMatrix(Matrix.getTranslateInstance( 100, 700 ));
		pdfContent.showText("Hi, this is a signed document.");
		pdfContent.endText();	
		pdfContent.close();
        doc.save( new FileOutputStream(pdfFile) );      
        doc.close();
	}
	
	public static void main(String... args) {
		
	}

	
}
