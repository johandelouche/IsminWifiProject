package fr.ismin;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
 

public class ISMINX509TrustManager implements X509TrustManager {
 
	
	/*
	 * A X509TrustManager which allow everyone and do not make any verification.
	 * Trusts everyone to ignore the obsolete ISMIN certificate.
	 * That is not safe but only limited to a specific used in the school.
	 */
    public ISMINX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
    }
 
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
       
    }
 
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
 
}