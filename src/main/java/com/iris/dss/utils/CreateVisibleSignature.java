package com.iris.dss.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;

/**
 * This is an example for visual signing a pdf with bouncy castle.

 * @see CreateSignature
 * @author trlok
 */
public class CreateVisibleSignature extends CreateSignatureBase { 

    private SignatureOptions options;
    private PDVisibleSignDesigner visibleSignDesigner;
    private PDVisibleSigProperties visibleSignatureProperties = null;

    public void setVisibleSignatureProperties(InputStream filename, int x, int y, int zoomPercent, 
            InputStream image, int page)  throws IOException
    {
        visibleSignDesigner = new PDVisibleSignDesigner(filename, image, page);
        visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).signatureFieldName("signature");
    }
    
    public void setSignatureProperties(String name, String location, String reason, int preferredSize, 
            int page, boolean visualSignEnabled) throws IOException
    {
    	visibleSignatureProperties = new PDVisibleSigProperties();
        visibleSignatureProperties.signerName(name).signerLocation(location).signatureReason(reason).
                preferredSize(preferredSize).page(page).visualSignEnabled(visualSignEnabled).
                setPdVisibleSignature(visibleSignDesigner).buildSignature();
    }

    /**
     * Initialize the signature creator with a keystore (pkcs12) and pin that
     * should be used for the signature.
     *
     * @param keystore is a pkcs12 keystore.
     * @param pin is the pin for the keystore / private key
     */
    public CreateVisibleSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException
    {
        // grabs the first alias from the keystore and get the private key. An
        // alternative method or constructor could be used for setting a specific
        // alias that should be used.
        Enumeration<String> aliases = keystore.aliases();
        String alias = null;
        if (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
        }
        else  {
            throw new IOException("Could not find alias");
        }
        setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
        setCertificate(keystore.getCertificateChain(alias)[0]);
    }

    public byte[] signDoc(byte[] inputFile, String signerName, String location, String purposeOfSigning) throws IOException
    {
        if (inputFile == null) {
            throw new IOException("Document for signing does not exist");
        }

        // creating output document and prepare the IO streams.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // load document
        PDDocument doc = PDDocument.load(inputFile);

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
        // subfilter for basic and PAdES Part 2 signatures
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName(signerName);
        signature.setLocation(location);
        signature.setReason(purposeOfSigning);

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled()) {
            options = new SignatureOptions();
            options.setVisualSignature(visibleSignatureProperties);
            doc.addSignature(signature, this, options);
        }
        else 
        {
            doc.addSignature(signature, this);
        }

        // write incremental (only for signing purpose)
        doc.saveIncremental(baos);
        baos.flush();
        
        doc.close();
        
        // do not close options before saving, because some COSStream objects within options 
        // are transferred to the signed document.
        if(options!=null) 
        	IOUtils.closeQuietly(options);
        
        return baos.toByteArray();
    }

   
}