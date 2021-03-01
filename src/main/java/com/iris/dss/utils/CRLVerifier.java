/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.iris.dss.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

//import org.apache.http.HttpConnection;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CRLVerifier {
 
	private static final Logger log = LoggerFactory.getLogger(CRLVerifier.class);
	
    /**
     * Extracts the CRL distribution points from the certificate (if available)
     * and checks the certificate revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
     * 
     * @param cert
     *            the certificate to be checked for revocation
     * @throws CertificateVerificationException
     *             if the certificate is revoked
     */
    
    private CRLVerifier() {
        
    }
    
    public static void verifyCertificateCRLs(X509Certificate cert) throws CertificateEncodingException {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            for (String crlDP : crlDistPoints) {
                X509CRL crl = downloadCRL(crlDP);
                if (crl.isRevoked(cert)) {
                    throw new CertificateEncodingException(
                            "The certificate is revoked by CRL: " + crlDP);
                }
            }
        } catch (Exception ex) {
        	log.error(">verifyCertificateCRLs error. "+ ex.getMessage(), ex); 
            if (ex instanceof CertificateEncodingException) {
                throw (CertificateEncodingException) ex;
            } else {
                throw new CertificateEncodingException(
                        "Can not verify CRL for certificate: "
                                + cert.getSubjectX500Principal());
            }
        }
    }

    /**
     * Downloads CRL from given URL. Supports http, https, ftp and ldap based
     * URLs.
     */
    private static X509CRL downloadCRL(String crlURL) throws Exception {
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
                || crlURL.startsWith("ftp://")) {
            return downloadCRLFromWeb(crlURL);
        } else if (crlURL.startsWith("ldap://")) {
            return downloadCRLFromLDAP(crlURL);
        } else {
            throw new CertificateEncodingException(
                    "Can not download CRL from certificate "
                            + "distribution point: " + crlURL);
        }
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
     */
    private static X509CRL downloadCRLFromLDAP(String ldapURL) throws Exception {
        Map<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");

        DirContext ctx = new InitialDirContext((Hashtable) env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        byte[] val = (byte[]) aval.get();
        if ((val == null) || (val.length == 0)) {
            throw new CertificateEncodingException("Can not download CRL from: " + ldapURL);
        } else {
            InputStream inStream = new ByteArrayInputStream(val);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(inStream);
        }
        
        //System.out.println(ctx.lookup("cn=GPKISubca,ou=CRL,ou=TMCA,o=TM,c=my,dc=tmca,dc=com,dc=my"));        
        //return null;
    }

    /**
     * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
     * http://crl.infonotary.com/crl/identity-ca.crl
     */
    private static X509CRL downloadCRLFromWeb(String crlURL) throws Exception {    	        
        URL url = new URL(crlURL);    	 
    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");    	
    	//con.setDoOutput(true);    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
    		BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        	int result = bis.read();
        	while(result != -1) {
        		baos.write((byte) result);
        	    result = bis.read();
        	}
    	} else {
    		BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        	int result = bis.read();
        	while(result != -1) {
        		baos.write((byte) result);
        	    result = bis.read();
        	}
        	log.error(">downloadCRLFromWeb error. CRL -> \n", baos.toString("UTF-8"));        	
    	}
    	byte[] crlBuffers = null;
    	if(isBase64Encoded(baos.toString("UTF-8"))) {
    		crlBuffers = Base64.decode(baos.toString("UTF-8"));
    	} else {
    		crlBuffers = baos.toByteArray();
    	}
    	CertificateFactory cf = CertificateFactory.getInstance("X.509");
    	return (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlBuffers));
    }

    /**
     * Extracts all CRL distribution point URLs from the
     * "CRL Distribution Point" extension in a X.509 certificate. If CRL
     * distribution point extension is unavailable, returns an empty list.
     */
    public static List<String> 
    getCrlDistributionPoints(X509Certificate cert) throws CertificateParsingException, IOException {
        byte[] crldpExt = cert
                .getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
        if (crldpExt == null) {
            return new ArrayList<String>();
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(
                new ByteArrayInputStream(crldpExt));
        ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(
                new ByteArrayInputStream(crldpExtOctets));
        ASN1Primitive derObj2 = oAsnInStream2.readObject();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<String>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null
                && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(
                        dpn.getName()).getNames();
                // Look for an URI
                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = DERIA5String.getInstance(
                                genNames[j].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }
    
    public static X509Certificate getCert(String certPath) throws Exception {
   	 
    	CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new FileInputStream(certPath));
		return cert;
    }
    
    public static boolean isBase64Encoded(String input) {
    	try {
    		Base64.decode(input);
    		return true;
    	} catch(Exception ex) {
    		return false;
    	}
    }
    
    public static void main(String... args) throws Exception {
    	
    	System.setProperty("http.proxyHost", "172.16.252.31");
        System.setProperty("http.proxyPort", "3128");
        
        String certSurayaToken22019 = "C:\\Users\\trlok\\Desktop\\Materials from MAMPU\\SurayaToken22019.cer";
        X509Certificate cert = getCert(certSurayaToken22019);
    	CRLVerifier.verifyCertificateCRLs(cert);
    	System.err.println("Certificate [certSurayaToken22019] is fine.");
    	
        String certSurayaToken32019 = "C:\\Users\\trlok\\Desktop\\Materials from MAMPU\\SurayaToken32019.cer";
        cert = getCert(certSurayaToken32019);
    	CRLVerifier.verifyCertificateCRLs(cert);
    	System.err.println("Certificate [certSurayaToken32019] is fine.");
    	
    	String SurayaToken22019_CRL = "http://crl-test.msctrustgate.com/MAMPUGPKI/LatestCRL.crl";
    	String SurayaToken32019_CRL = "ldap://devca.tabgdc.com:389/cn=GPKISubca,ou=CRL,ou=TMCA,o=TM,c=my,dc=tmca,dc=com,dc=my";    	
    	
    	//X509CRL x509CRL = CRLVerifier.downloadCRL(SurayaToken22019_CRL);
    	//x509CRL = CRLVerifier.downloadCRL(SurayaToken32019_CRL);
    	//
    }

}