/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of gpslogger.
 *
 * gpslogger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * gpslogger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gpslogger.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.casic.titan.googlegps.common.network;


import android.app.Activity;
import android.content.Context;

import com.casic.titan.googlegps.R;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.casic.titan.googlegps.loggers.Files;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class Networks {

    private static final Logger LOG = Logs.of(Networks.class);

    static String LOCAL_TRUSTSTORE_FILENAME = "knownservers.bks";
    static String LOCAL_TRUSTSTORE_PASSWORD = "politelemon";

    public static KeyStore getKnownServersStore(Context context)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        KeyStore mKnownServersStore = KeyStore.getInstance(KeyStore.getDefaultType());
        File localTrustStoreFile = new File(Files.storageFolder(context), LOCAL_TRUSTSTORE_FILENAME);

        LOG.debug("Getting local truststore - " + localTrustStoreFile.getAbsolutePath());
        if (localTrustStoreFile.exists()) {
            InputStream in = new FileInputStream(localTrustStoreFile);
            try {
                mKnownServersStore.load(in, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
            } finally {
                in.close();
            }
        } else {
            // next is necessary to initialize an empty KeyStore instance
            mKnownServersStore.load(null, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
        }

        return mKnownServersStore;
    }


    public static void addCertToKnownServersStore(Certificate cert, Context context)
            throws  KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        File localTrustStoreFile = new File(Files.storageFolder(context), LOCAL_TRUSTSTORE_FILENAME);

        KeyStore knownServers = Networks.getKnownServersStore(context);
        LOG.debug("Adding certificate - HashCode: " + cert.hashCode());
        knownServers.setCertificateEntry(Integer.toString(cert.hashCode()), cert);

        FileOutputStream fos = null;

        try {
            //fos = context.openFileOutput(localTrustStoreFile.getName(), Context.MODE_PRIVATE);
            fos = new FileOutputStream(localTrustStoreFile);
            knownServers.store(fos, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
        }
        catch(Exception e)
        {
            LOG.error("Could not save certificate", e);
        }
        finally {
            fos.close();
        }
    }

    public static com.casic.titan.googlegps.common.network.CertificateValidationException extractCertificateValidationException(Exception e) {

        if (e == null) { return null ; }

        com.casic.titan.googlegps.common.network.CertificateValidationException result = null;

        if (e instanceof com.casic.titan.googlegps.common.network.CertificateValidationException) {
            return (com.casic.titan.googlegps.common.network.CertificateValidationException)e;
        }
        Throwable cause = e.getCause();
        Throwable previousCause = null;
        while (cause != null && cause != previousCause && !(cause instanceof com.casic.titan.googlegps.common.network.CertificateValidationException)) {
            previousCause = cause;
            cause = cause.getCause();
        }
        if (cause != null && cause instanceof com.casic.titan.googlegps.common.network.CertificateValidationException) {
            result = (CertificateValidationException)cause;
        }
        return result;
    }

    public static SSLSocketFactory getSocketFactory(Context context){
        try {

            SSLContext sslContext = SSLContext.getInstance("TLS");
            LocalX509TrustManager atm = null;

            atm = new LocalX509TrustManager(getKnownServersStore(context));

            TrustManager[] tms = new TrustManager[] { atm };
            sslContext.init(null, tms, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            LOG.error("Could not get SSL Socket factory ", e);
        }

        return null;
    }

    public static void beginCertificateValidationWorkflow(Activity activity, String host, int port, ServerType serverType) {
//        Handler postValidationHandler = new Handler();
//        Dialogs.progress((FragmentActivity) activity, activity.getString(R.string.please_wait));
//        new Thread(new CertificateValidationWorkflow(activity, host, port, serverType, postValidationHandler)).start();
    }


    public static TrustManager getTrustManager(Context context)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, CertStoreException {
        return new LocalX509TrustManager(getKnownServersStore(context));
    }
}
