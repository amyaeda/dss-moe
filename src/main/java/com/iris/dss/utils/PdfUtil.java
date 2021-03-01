package com.iris.dss.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfUtil {

	private static final Logger log = LoggerFactory.getLogger(PdfUtil.class);
	 
	public static final int SIGN_FAILED = 0;
	public static final int SIGN_SUCCESS = 1;
	public static final int NEW_FILE = 2;

	public static void main(String[] args) throws Exception {    	
        try  
        {              	
        	byte[] input = Files.readAllBytes(Paths.get("C:/Users/paridah/Desktop/DupName1_originalPdfP2.pdf"));
        	
        	System.out.println("SHA256: " + getSHA256Checksum(input));
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 * Calculate checksum value and retun in HEX format
	 * 
	 * @param input
	 * @return hex string of the SHA256 checksum value
	 * @throws NoSuchAlgorithmException
	 */
	public static String getSHA256Checksum(byte[] input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		//md.update(input);
		md.update(input, 0, input.length);
		byte[] digest = md.digest();
		
		return Hex.toHexString(digest);
	}
    /**
     * Split pages into new one for preview purpose (e-ITPS case)
     * 
     * @param pages
     * @param previewFile
     * @throws Exception 
     * @throws IOException
     */
    public static byte[] generatePreviewPdf(int[] previewPages, PDDocument document) throws Exception {
    	//PDDocument document = PDDocument.load(input);   	
    	
    	List<PDPage> pages = new ArrayList<PDPage>();
    	for(int number : previewPages) {
    		if(0 == number) 
    			continue;
    		
    		pages.add(document.getPage(number - 1)); 
    	}    	
    	byte[] doc = rebuildPdfFromGivenPages(pages); //, outputFile);
    	
    	document.close();
    	
    	return doc;
    }
    
    public static int[] getPreviewPages(String pageNo, int numberOfPages) {
    	//if(null != pageNo) {
		String[] pages = pageNo.trim().split(",");
		
		int[] previewPages = new int[pages.length];
		
		int i = 0;
		for(String page : pages) { 
			try {
				
				int num = Integer.parseInt(page.trim());
				if(num > numberOfPages) {
					log.warn("getPreviewPages>Configuration error.  Preview page number '"+ num +"' exceeded the total pages of the document.");
					continue;
				}
				
				if(0 == num) {
	    			continue;
				}
				
				previewPages[i] = num;
				i++;
			} catch(NumberFormatException nfe) {
				log.warn("getPreviewPages>preview page setting NumberFormatException: "+ nfe.getMessage()); // + "|doc checksum:"+ pdfDoc.getSha256Checksum());
				continue;
			}												
		}
		
		// remove additional array element
		for(int j = previewPages.length - 1; j > i - 1; j--) {
			previewPages = ArrayUtils.remove(previewPages, j);
		}
		//}
		
		return previewPages;
	
    }
    
    static byte[] rebuildPdfFromGivenPages(List<PDPage> pages) throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	PDDocument doc = new PDDocument();    	
    	for(PDPage page : pages) {
    		doc.addPage(page);
    	}
    	doc.save(baos);
    	doc.close();
    	
    	return baos.toByteArray();
    }
    
    
	public static byte[] generatePreviewPdfImage(int pageNo, byte[] input, String extension) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDDocument document = PDDocument.load(input); //new File(filename));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNo - 1, 100);
		ImageIOUtil.writeImage(bim, extension, baos, 100);
		document.close();
		
		return baos.toByteArray();
	}
	
	public static byte[] fileToByteArray(File file) throws IOException {
		byte[] bytes = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(bytes);
		fis.close();
		
		return bytes;
	}
    
    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    public static void checkSignature(byte[] pdfToBeCheck, X509Certificate signerCert) throws Exception {
    	Security.addProvider(new BouncyCastleProvider());
    	try (PDDocument document = PDDocument.load(pdfToBeCheck))
    	{
    		List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
    		if (signatureDictionaries.isEmpty())
    		{
    			throw new Exception("no signature found");
    		}
    		
    		for (PDSignature sig : document.getSignatureDictionaries())
    		{
    			COSString contents = (COSString) sig.getCOSObject().getDictionaryObject(COSName.CONTENTS);

    			byte[] buf = sig.getSignedContent(new ByteArrayInputStream(pdfToBeCheck));

    			// reference:
    			// http://stackoverflow.com/a/26702631/535646
    			// http://stackoverflow.com/a/9261365/535646
    			CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
    			
    			Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
    			SignerInformation signerInformation = signers.iterator().next();
    			System.out.println(">validatePDF> PublicKey algorithm: " + signerCert.getPublicKey().getAlgorithm());
    			
    			if(signerCert.getPublicKey().getAlgorithm() == "EC")
    			{	
		            Signature signature = Signature.getInstance("SHA256WITHECDSA", "BC");
		            signature.initVerify(signerCert); 
		            signature.update(signerInformation.getEncodedSignedAttributes());
		            boolean valid = signature.verify(signerInformation.getSignature());
		            if(!valid)
		            {
		            	throw new Exception("Signature verification failed");
		            }
    			}
    			else if(!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(signerCert)))
    			{
    				throw new Exception("Signature verification failed");
    			}
    			
    			break;
    		} //end of for loop
    	}
    }
    
}
