package com.iris.dss.pdfbox.core;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TSPException;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

public abstract class CreateSignatureBase implements SignatureInterface {    
	private PrivateKey privateKey;
    private Certificate certificate;
    private Certificate signerCert;
    private Certificate[] signerCertChain;
    private TSAClient tsaClient;
    
    private Certificate[] certificates;

    public Certificate getSignerCertificate() {
		return signerCert;
	}

	public void setSignerCertificate(Certificate signerCert) {
		this.signerCert = signerCert;
	}

	public Certificate[] getSignerCertificateChain() {
		return signerCertChain;
	}

	public void setSignerCertificateChain(Certificate[] signerCertChain) {
		this.signerCertChain = signerCertChain;
	}

	public void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    public void setCertificate(Certificate certificate)
    {
        this.certificate = certificate;
    }
    
    public void setCertificateChain(Certificate[] certs)
    {
        this.certificates = certs;
    }

    public void setTsaClient(TSAClient tsaClient)
    {
        this.tsaClient = tsaClient;
    }

    public TSAClient getTsaClient()
    {
        return tsaClient;
    }

    public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public Certificate getCertificate() {
		return certificate;
	}
	
	public Certificate[] getCertificateChain() {
		return certificates;
	}
    	
    /**
     * We are extending CMS Signature
     *
     * @param signer information about signer
     * @return information about SignerInformation
     */
    private SignerInformation signTimeStamp(SignerInformation signer)
            throws IOException, TSPException
    {
        AttributeTable unsignedAttributes = signer.getUnsignedAttributes();

        ASN1EncodableVector vector = new ASN1EncodableVector();
        if (unsignedAttributes != null)
        {
            vector = unsignedAttributes.toASN1EncodableVector();
        }

        byte[] signature = signer.getSignature();

        byte[] token = getTsaClient().getTimeStampToken(signature);
        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
        ASN1Encodable signatureTimeStamp = new Attribute(oid, new DERSet(ASN1Primitive.fromByteArray(token)));

        vector.add(signatureTimeStamp);
        Attributes signedAttributes = new Attributes(vector);

        SignerInformation newSigner = SignerInformation.replaceUnsignedAttributes(
                signer, new AttributeTable(vector));

        // TODO can this actually happen?
        if (newSigner == null)
        {
            return signer;
        }

        return newSigner;
    }
	
	/**
     * We just extend CMS signed Data
     *
     * @param signedData -Generated CMS signed data
     * @return CMSSignedData - Extended CMS signed data
     */
    protected CMSSignedData signTimeStamps(CMSSignedData signedData)
            throws IOException, TSPException
    {
        SignerInformationStore signerStore = signedData.getSignerInfos();
        List<SignerInformation> newSigners = new ArrayList<SignerInformation>();
        SignerInformation[] signerInfos = new SignerInformation[signerStore.getSigners().size()];
        signerStore.getSigners().toArray(signerInfos);
        
        for (SignerInformation signer : signerInfos)
        {
            newSigners.add(signTimeStamp(signer));
        }

        // TODO do we have to return a new store?
        return CMSSignedData.replaceSigners(signedData, new SignerInformationStore(newSigners));
    }
    
    /* 
    public byte[] sign(InputStream content) throws IOException  {
        try {
            List<Certificate> certList = new ArrayList<Certificate>();
            certList.add(certificate);
            Store certs = new JcaCertStore(certList);
            IrisCMSSignedDataGenerator gen = new IrisCMSSignedDataGenerator();
            org.bouncycastle.asn1.x509.Certificate cert = org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(certificate.getEncoded()));
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            gen.addSignerInfoGenerator(new IrisJcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(cert)));
            gen.addCertificates(certs);
            IrisCMSProcessableInputStream msg = new IrisCMSProcessableInputStream(content);

            CMSSignedData signedData = gen.generate(msg, false);
            
            if (tsaClient != null) {
                signedData = signTimeStamps(signedData);
            }
            
            return signedData.getEncoded();
        }        
        catch (Exception e) {
            throw new IOException(e);
        }
    }
*/
    
}
