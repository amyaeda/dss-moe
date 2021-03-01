package com.iris.dss.pdfbox.core;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.SignerIdentifier;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignatureEncryptionAlgorithmFinder;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.TeeOutputStream;

import com.iris.dss.pdfbox.IrisPDFBox;

public class IrisSignerInfoGenerator
{
    private final SignerIdentifier signerIdentifier;
    private final CMSAttributeTableGenerator sAttrGen;
    private final CMSAttributeTableGenerator unsAttrGen;
    private final ContentSigner signer;
    private final DigestCalculator digester;
    private final DigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
    private final CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder;

    private byte[] calculatedDigest = null;
    private X509CertificateHolder certHolder;

    IrisSignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider,
        CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder)
        throws OperatorCreationException
    {
        this(signerIdentifier, signer, digesterProvider, sigEncAlgFinder, false);
    }

    IrisSignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider,
        CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder,
        boolean isDirectSignature)
        throws OperatorCreationException
    {
        this.signerIdentifier = signerIdentifier;
        this.signer = signer;

        if (digesterProvider != null)
        {
            this.digester = digesterProvider.get(digAlgFinder.find(signer.getAlgorithmIdentifier()));
        }
        else
        {
            this.digester = null;
        }

        if (isDirectSignature)
        {
            this.sAttrGen = null;
            this.unsAttrGen = null;
        }
        else
        {
            this.sAttrGen = new IrisDefaultSignedAttributeTableGenerator();
            this.unsAttrGen = null;
        }

        this.sigEncAlgFinder = sigEncAlgFinder;
    }

    public IrisSignerInfoGenerator(
        IrisSignerInfoGenerator original,
        CMSAttributeTableGenerator sAttrGen,
        CMSAttributeTableGenerator unsAttrGen)
    {
        this.signerIdentifier = original.signerIdentifier;
        this.signer = original.signer;
        this.digester = original.digester;
        this.sigEncAlgFinder = original.sigEncAlgFinder;
        this.sAttrGen = sAttrGen;
        this.unsAttrGen = unsAttrGen;
    }

    IrisSignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider,
        CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder,
        CMSAttributeTableGenerator sAttrGen,
        CMSAttributeTableGenerator unsAttrGen)
        throws OperatorCreationException
    {
        this.signerIdentifier = signerIdentifier;
        this.signer = signer;

        if (digesterProvider != null)
        {
            this.digester = digesterProvider.get(digAlgFinder.find(signer.getAlgorithmIdentifier()));
        }
        else
        {
            this.digester = null;
        }

        this.sAttrGen = sAttrGen;
        this.unsAttrGen = unsAttrGen;
        this.sigEncAlgFinder = sigEncAlgFinder;
    }

    public SignerIdentifier getSID()
    {
        return signerIdentifier;
    }

    public int getGeneratedVersion()
    {
        return signerIdentifier.isTagged() ? 3 : 1;
    }

    public boolean hasAssociatedCertificate()
    {
        return certHolder != null;
    }

    public X509CertificateHolder getAssociatedCertificate()
    {
        return certHolder;
    }
    
    public AlgorithmIdentifier getDigestAlgorithm()
    {
        if (digester != null)
        {
            return digester.getAlgorithmIdentifier();
        }

        return digAlgFinder.find(signer.getAlgorithmIdentifier());
    }
    
    public OutputStream getCalculatingOutputStream()
    {
        if (digester != null)
        {
            if (sAttrGen == null)
            {
                return new TeeOutputStream(digester.getOutputStream(), signer.getOutputStream());    
            }
            return digester.getOutputStream();
        }
        else
        {
            return signer.getOutputStream();
        }
    }

    public List<String> getDigestAndUnsignedBase64DataList(ASN1ObjectIdentifier contentType, Date signingTime) throws CMSException {    	
    	
    	List<String> ret = new ArrayList<String>();
    	try
        {
            ASN1Set signedAttr = null;
            AlgorithmIdentifier digestAlg = null;
            if (sAttrGen != null)
            {
                digestAlg = digester.getAlgorithmIdentifier();
                calculatedDigest = digester.getDigest(); // call -> dig.digest()
                Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
                AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                signedAttr = getAttributeSet(signed);                               
                
                String digestKey = Base64.toBase64String(calculatedDigest);
                // sig must be composed from the DER encoding. 
                String unsignedDataValue = Base64.toBase64String( signedAttr.getEncoded( ASN1Encoding.DER ) );
                ret.add(digestKey);
                ret.add(unsignedDataValue);
                
                return ret;
            }
        }
        catch (IOException e)
        {
            throw new CMSException("get unsigned data error.", e);
        }
        
    	return null;
    }
    
    public SignerInfo generate(ASN1ObjectIdentifier contentType, Date signingTime)
        throws CMSException
    {
        try
        {
            /* RFC 3852 5.4
             * The result of the message digest calculation process depends on
             * whether the signedAttrs field is present.  When the field is absent,
             * the result is just the message digest of the content as described
             *
             * above.  When the field is present, however, the result is the message
             * digest of the complete DER encoding of the SignedAttrs value
             * contained in the signedAttrs field.
             */
            ASN1Set signedAttr = null;

            AlgorithmIdentifier digestAlg = null;

            if (sAttrGen != null)
            {
                digestAlg = digester.getAlgorithmIdentifier();
                calculatedDigest = digester.getDigest(); // call -> dig.digest()
                Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
                AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                signedAttr = getAttributeSet(signed);

                // sig must be composed from the DER encoding.
                OutputStream sOut = signer.getOutputStream();

                // invoke 
                // JcaContentSignerBuilder.SignatureOutputStream.write(byte[] bytes)
                sOut.write(signedAttr.getEncoded(ASN1Encoding.DER)); 

                sOut.close();
            }
            else
            {
                if (digester != null)
                {
                    digestAlg = digester.getAlgorithmIdentifier();
                    calculatedDigest = digester.getDigest();
                }
                else
                {
                    digestAlg = digAlgFinder.find(signer.getAlgorithmIdentifier());
                    calculatedDigest = null;
                }
            }

            byte[] sigBytes = signer.getSignature(); // call -> sig.sign()

            ASN1Set unsignedAttr = null;
            if (unsAttrGen != null)
            {
                Map parameters = getBaseParameters(contentType, digestAlg, calculatedDigest, signingTime);
                parameters.put(CMSAttributeTableGenerator.SIGNATURE, Arrays.clone(sigBytes));

                AttributeTable unsigned = unsAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                unsignedAttr = getAttributeSet(unsigned);
            }

            AlgorithmIdentifier digestEncryptionAlgorithm = sigEncAlgFinder.findEncryptionAlgorithm(signer.getAlgorithmIdentifier());

            return new SignerInfo(signerIdentifier, digestAlg,
                signedAttr, digestEncryptionAlgorithm, new DEROctetString(sigBytes), unsignedAttr);
        }
        catch (IOException e)
        {
            throw new CMSException("encoding error.", e);
        }
    }
    
    public SignerInfo constructSignerInfo(ASN1ObjectIdentifier contentType, byte[] mydigest, byte[] signedBytes, Date signingTime) throws CMSException {
    	try
        {          
            ASN1Set signedAttr = null;
            AlgorithmIdentifier digestAlg = null;

            if (sAttrGen != null)
            {
                digestAlg = digester.getAlgorithmIdentifier();
                calculatedDigest = mydigest; //digester.getDigest(); // call -> dig.digest()
                Map parameters = getBaseParameters(
                		contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
                AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                signedAttr = getAttributeSet(signed);
                // sig must be composed from the DER encoding.
//                OutputStream sOut = signer.getOutputStream();
                // invoke - JcaContentSignerBuilder.SignatureOutputStream.write(byte[] bytes)
//                sOut.write(signedAttr.getEncoded(ASN1Encoding.DER)); 
//                sOut.close();

            }
//            else
//            {
//                if (digester != null)
//                {
//                    digestAlg = digester.getAlgorithmIdentifier();
//                    calculatedDigest = digester.getDigest();
//                }
//                else
//                {
//                    digestAlg = digAlgFinder.find(signer.getAlgorithmIdentifier());
//                    calculatedDigest = null;
//                }
//            }

            byte[] sigBytes = signedBytes; // signer.getSignature(); // call -> sig.sign()

            ASN1Set unsignedAttr = null;
            if (unsAttrGen != null)
            {
                Map parameters = getBaseParameters(contentType, digestAlg, calculatedDigest, signingTime);
                parameters.put(CMSAttributeTableGenerator.SIGNATURE, Arrays.clone(sigBytes));

                AttributeTable unsigned = unsAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                unsignedAttr = getAttributeSet(unsigned);
            }

            AlgorithmIdentifier digestEncryptionAlgorithm = sigEncAlgFinder.findEncryptionAlgorithm(signer.getAlgorithmIdentifier());

            return new SignerInfo(signerIdentifier, digestAlg,
                signedAttr, digestEncryptionAlgorithm, new DEROctetString(sigBytes), unsignedAttr);
        }
        catch (Exception e)
        {
            throw new CMSException("encoding error.", e);
        }
    }
    
    public byte[] getSignedBuffers(ASN1ObjectIdentifier contentType, Date signingTime) throws CMSException
    {
    	try
    	{    		
    		ASN1Set signedAttr = null;

    		AlgorithmIdentifier digestAlg = null;

    		if (sAttrGen != null)
    		{
    			digestAlg = digester.getAlgorithmIdentifier();
    			calculatedDigest = digester.getDigest(); // call -> dig.digest()

//    			FileOutputStream os = new FileOutputStream("C:/Users/trlok/Desktop/digest.txt");
//    			os.write(calculatedDigest);
//    			os.flush();
//    			os.close();

    			Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
    			AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

    			signedAttr = getAttributeSet(signed);
    			// sig must be composed from the DER encoding.
//    			OutputStream sOut = signer.getOutputStream();
    			// invoke - JcaContentSignerBuilder.SignatureOutputStream.write(byte[] bytes)
//    			sOut.write(signedAttr.getEncoded(ASN1Encoding.DER)); 
//    			sOut.close();

    			return signedAttr.getEncoded(ASN1Encoding.DER);
//    			FileWriter fw1 = new FileWriter("C:/Users/trlok/Desktop/rawsignedAttr.txt");
//    			fw1.write(Base64.toBase64String(signedAttr.getEncoded(ASN1Encoding.DER)));
//    			fw1.flush();
//    			fw1.close();
    		}    		

    	}
    	catch (IOException e)
    	{
    		throw new CMSException("encoding error.", e);
    	}
    	
    	return null;
    }
    
    public SignerInfo generate3(ASN1ObjectIdentifier contentType, Date signingTime)
    throws CMSException
    {
    	try
    	{
    		/* RFC 3852 5.4
    		 * The result of the message digest calculation process depends on
    		 * whether the signedAttrs field is present.  When the field is absent,
    		 * the result is just the message digest of the content as described
    		 *
    		 * above.  When the field is present, however, the result is the message
    		 * digest of the complete DER encoding of the SignedAttrs value
    		 * contained in the signedAttrs field.
    		 */
    		ASN1Set signedAttr = null;

    		AlgorithmIdentifier digestAlg = null;

    		if (sAttrGen != null)
    		{
    			digestAlg = digester.getAlgorithmIdentifier();
    			calculatedDigest = digester.getDigest(); // call -> dig.digest()

    			FileOutputStream os = new FileOutputStream("C:/Users/trlok/Desktop/digest.txt");
    			os.write(calculatedDigest);
    			os.flush();
    			os.close();

    			Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
    			AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

    			signedAttr = getAttributeSet(signed);
    			// sig must be composed from the DER encoding.
    			OutputStream sOut = signer.getOutputStream();
    			// invoke - JcaContentSignerBuilder.SignatureOutputStream.write(byte[] bytes)
    			sOut.write(signedAttr.getEncoded(ASN1Encoding.DER)); 
    			sOut.close();

    			FileWriter fw1 = new FileWriter("C:/Users/trlok/Desktop/rawsignedAttr.txt");
    			fw1.write(Base64.toBase64String(signedAttr.getEncoded(ASN1Encoding.DER)));
    			fw1.flush();
    			fw1.close();
    		}
    		else
    		{
    			if (digester != null)
    			{
    				digestAlg = digester.getAlgorithmIdentifier();
    				calculatedDigest = digester.getDigest();
    			}
    			else
    			{
    				digestAlg = digAlgFinder.find(signer.getAlgorithmIdentifier());
    				calculatedDigest = null;
    			}
    		}

    		byte[] sigBytes = signer.getSignature(); // call -> sig.sign()

    		ASN1Set unsignedAttr = null;
    		if (unsAttrGen != null)
    		{
    			Map parameters = getBaseParameters(contentType, digestAlg, calculatedDigest, signingTime);
    			parameters.put(CMSAttributeTableGenerator.SIGNATURE, Arrays.clone(sigBytes));

    			AttributeTable unsigned = unsAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

    			unsignedAttr = getAttributeSet(unsigned);
    		}

    		AlgorithmIdentifier digestEncryptionAlgorithm = sigEncAlgFinder.findEncryptionAlgorithm(signer.getAlgorithmIdentifier());

    		return new SignerInfo(signerIdentifier, digestAlg,
    				signedAttr, digestEncryptionAlgorithm, new DEROctetString(sigBytes), unsignedAttr);
    	}
    	catch (IOException e)
    	{
    		throw new CMSException("encoding error.", e);
    	}
    }
    
    
    public SignerInfo generate2 (ASN1ObjectIdentifier contentType, byte[] digest, byte[] sigBytes, Date signingTime) throws CMSException {

    	try {

    		ASN1Set signedAttr = null;
    		AlgorithmIdentifier digestAlg = null;

    		if (sAttrGen != null)
    		{
    		
    			digestAlg = digester.getAlgorithmIdentifier();
 
    			calculatedDigest = digest; // digester.getDigest(); // call -> dig.digest()
    			
    			System.out.println(">>> hash1: "+Base64.toBase64String(digester.getDigest()));
				System.out.println(">>> hash2: "+Base64.toBase64String(digest));
				
    			Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest, signingTime);
    			AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

    			signedAttr = getAttributeSet(signed);
   
    			// sig must be composed from the DER encoding.
    			OutputStream sOut = signer.getOutputStream();
    			// invoke - JcaContentSignerBuilder.SignatureOutputStream.write(byte[] bytes)
    			sOut.write(signedAttr.getEncoded(ASN1Encoding.DER)); 
    			sOut.close();
 /**/
    		}
    		else
    		{
    			if (digester != null)
    			{
    				digestAlg = digester.getAlgorithmIdentifier();    				
    				calculatedDigest = digest; //digester.getDigest();
    			}
    			else
    			{
    				digestAlg = digAlgFinder.find(signer.getAlgorithmIdentifier());
    				calculatedDigest = null;
    			}
    		}

    		System.out.println(">>> sig1: "+Base64.toBase64String(sigBytes));
			System.out.println(">>> sig2: "+Base64.toBase64String(signer.getSignature()));
			
//    		byte[] sigBytes = signer.getSignature(); // call -> sig.sign()

    		ASN1Set unsignedAttr = null;
    		if (unsAttrGen != null)
    		{
    			Map parameters = getBaseParameters(contentType, digestAlg, digest, signingTime);
    			parameters.put(CMSAttributeTableGenerator.SIGNATURE, Arrays.clone(sigBytes));
    			AttributeTable unsigned = unsAttrGen.getAttributes(Collections.unmodifiableMap(parameters));
    			unsignedAttr = getAttributeSet(unsigned);
    		}

    		AlgorithmIdentifier digestEncryptionAlgorithm = sigEncAlgFinder.findEncryptionAlgorithm(signer.getAlgorithmIdentifier());

    		return new SignerInfo(signerIdentifier, digestAlg,
    				signedAttr, digestEncryptionAlgorithm, new DEROctetString(sigBytes), unsignedAttr);
    	}
    	catch (Exception e)
    	{
    		throw new CMSException("encoding error.", e);
    	}
    }

    void setAssociatedCertificate(X509CertificateHolder certHolder)
    {
        this.certHolder = certHolder;
    }

    private ASN1Set getAttributeSet(
        AttributeTable attr)
    {
        if (attr != null)
        {
            return new DERSet(attr.toASN1EncodableVector());
        }

        return null;
    }

    private Map getBaseParameters(ASN1ObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash, Date signingTime)
    {
        Map param = new HashMap();

        if (contentType != null)
        {
            param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        }

        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  Arrays.clone(hash));
        param.put(IrisPDFBox.SIGNING_TIME,  signingTime);
        return param;
    }

    public byte[] getCalculatedDigest()
    {
        if (calculatedDigest != null)
        {
            return Arrays.clone(calculatedDigest);
        }

        return null;
    }

    public CMSAttributeTableGenerator getSignedAttributeTableGenerator()
    {
        return sAttrGen;
    }

    public CMSAttributeTableGenerator getUnsignedAttributeTableGenerator()
    {
        return unsAttrGen;
    }
}
