package com.iris.dss.pdfbox.core;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.SignerIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSSignatureEncryptionAlgorithmFinder;
import org.bouncycastle.cms.DefaultCMSSignatureEncryptionAlgorithmFinder;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;

/**
 * Builder for SignerInfo generator objects.
 */
public class IrisSignerInfoGeneratorBuilder
{
    private DigestCalculatorProvider digestProvider;
    private boolean directSignature;
    private CMSAttributeTableGenerator signedGen;
    private CMSAttributeTableGenerator unsignedGen;
    private CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder;

    /**
     *  Base constructor.
     *
     * @param digestProvider  a provider of digest calculators for the algorithms required in the signature and attribute calculations.
     */
    public IrisSignerInfoGeneratorBuilder(DigestCalculatorProvider digestProvider)
    {
        this(digestProvider, new DefaultCMSSignatureEncryptionAlgorithmFinder());
    }

        /**
     *  Base constructor.
     *
     * @param digestProvider  a provider of digest calculators for the algorithms required in the signature and attribute calculations.
     */
    public IrisSignerInfoGeneratorBuilder(DigestCalculatorProvider digestProvider, CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder)
    {
        this.digestProvider = digestProvider;
        this.sigEncAlgFinder = sigEncAlgFinder;
    }

    /**
     * If the passed in flag is true, the signer signature will be based on the data, not
     * a collection of signed attributes, and no signed attributes will be included.
     *
     * @return the builder object
     */
    public IrisSignerInfoGeneratorBuilder setDirectSignature(boolean hasNoSignedAttributes)
    {
        this.directSignature = hasNoSignedAttributes;

        return this;
    }

    /**
     *  Provide a custom signed attribute generator.
     *
     * @param signedGen a generator of signed attributes.
     * @return the builder object
     */
    public IrisSignerInfoGeneratorBuilder setSignedAttributeGenerator(CMSAttributeTableGenerator signedGen)
    {
        this.signedGen = signedGen;

        return this;
    }

    /**
     * Provide a generator of unsigned attributes.
     *
     * @param unsignedGen  a generator for signed attributes.
     * @return the builder object
     */
    public IrisSignerInfoGeneratorBuilder setUnsignedAttributeGenerator(CMSAttributeTableGenerator unsignedGen)
    {
        this.unsignedGen = unsignedGen;

        return this;
    }

    /**
     * Build a generator with the passed in certHolder issuer and serial number as the signerIdentifier.
     *
     * @param contentSigner  operator for generating the final signature in the SignerInfo with.
     * @param certHolder  carrier for the X.509 certificate related to the contentSigner.
     * @return  a SignerInfoGenerator
     * @throws OperatorCreationException   if the generator cannot be built.
     */
    public IrisSignerInfoGenerator build(ContentSigner contentSigner, X509CertificateHolder certHolder)
        throws OperatorCreationException
    {
        SignerIdentifier sigId = new SignerIdentifier(new IssuerAndSerialNumber(certHolder.toASN1Structure()));

        IrisSignerInfoGenerator sigInfoGen = createGenerator(contentSigner, sigId);

        sigInfoGen.setAssociatedCertificate(certHolder);

        return sigInfoGen;
    }

    /**
     * Build a generator with the passed in subjectKeyIdentifier as the signerIdentifier. If used  you should
     * try to follow the calculation described in RFC 5280 section 4.2.1.2.
     *
     * @param contentSigner  operator for generating the final signature in the SignerInfo with.
     * @param subjectKeyIdentifier    key identifier to identify the public key for verifying the signature.
     * @return  a SignerInfoGenerator
     * @throws OperatorCreationException if the generator cannot be built.
     */
    public IrisSignerInfoGenerator build(ContentSigner contentSigner, byte[] subjectKeyIdentifier)
        throws OperatorCreationException
    {
        SignerIdentifier sigId = new SignerIdentifier(new DEROctetString(subjectKeyIdentifier));

        return createGenerator(contentSigner, sigId);
    }

    private IrisSignerInfoGenerator createGenerator(ContentSigner contentSigner, SignerIdentifier sigId)
        throws OperatorCreationException
    {
        if (directSignature)
        {
            return new IrisSignerInfoGenerator(sigId, contentSigner, digestProvider, sigEncAlgFinder, true);
        }

        if (signedGen != null || unsignedGen != null)
        {
            if (signedGen == null)
            {
                signedGen = new DefaultSignedAttributeTableGenerator();
            }

            return new IrisSignerInfoGenerator(sigId, contentSigner, digestProvider, sigEncAlgFinder, signedGen, unsignedGen);
        }
        
        return new IrisSignerInfoGenerator(sigId, contentSigner, digestProvider, sigEncAlgFinder);
    }
}
