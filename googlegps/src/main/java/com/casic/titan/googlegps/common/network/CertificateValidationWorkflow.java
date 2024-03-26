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
import android.os.Handler;

import com.casic.titan.googlegps.R;
import com.casic.titan.googlegps.common.slf4j.Logs;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.cert.Certificate;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class CertificateValidationWorkflow implements Runnable {

    Handler postValidationHandler;
    Activity activity;
    String host;
    int port;
    ServerType serverType;

    private static final Logger LOG = Logs.of(CertificateValidationWorkflow.class);

    CertificateValidationWorkflow(Activity activity, String host, int port, ServerType serverType, Handler postValidationHandler) {
        this.activity = activity;
        this.host = host;
        this.port = port;
        this.serverType = serverType;
        this.postValidationHandler = postValidationHandler;

    }

    @Override
    public void run() {
        try {

            LOG.debug("Beginning certificate validation - will connect directly to {} port {}", host, String.valueOf(port));

            try {
                LOG.debug("Trying handshake first in case the socket is SSL/TLS only");
                connectToSSLSocket(null);
                postValidationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onWorkflowFinished(activity, null, true);
                    }
                });
            } catch (final Exception e) {

                if (com.casic.titan.googlegps.common.network.Networks.extractCertificateValidationException(e) != null) {
                    throw e;
                }

                LOG.debug("Direct connection failed or no certificate was presented", e);

                if(serverType== ServerType.HTTPS){
                    postValidationHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onWorkflowFinished(activity, e, false);
                        }
                    });
                    return;
                }

                LOG.debug("Now attempting to connect over plain socket");
                Socket plainSocket = new Socket(host, port);
                plainSocket.setSoTimeout(30000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(plainSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(plainSocket.getOutputStream()));
                String line;

                if (serverType == ServerType.SMTP) {
                    LOG.debug("CLIENT: EHLO localhost");
                    writer.write("EHLO localhost\r\n");
                    writer.flush();
                    line = reader.readLine();
                    LOG.debug("SERVER: " + line);
                }

                String command = "", regexToMatch = "";
                if (serverType == ServerType.FTP) {

                    LOG.debug("FTP type server");
                    command = "AUTH SSL\r\n";
                    regexToMatch = "(?:234.*)";

                } else if (serverType == ServerType.SMTP) {

                    LOG.debug("SMTP type server");
                    command = "STARTTLS\r\n";
                    regexToMatch = "(?i:220 .* Ready.*)";

                }

                LOG.debug("CLIENT: " + command);
                LOG.debug("(Expecting regex {} in response)", regexToMatch);
                writer.write(command);
                writer.flush();
                while ((line = reader.readLine()) != null) {
                    LOG.debug("SERVER: " + line);
                    if (line.matches(regexToMatch)) {
                        LOG.debug("Elevating socket and attempting handshake");
                        connectToSSLSocket(plainSocket);
                        postValidationHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                onWorkflowFinished(activity, null, true);
                            }
                        });
                        return;
                    }
                }

                LOG.debug("No certificates found.  Giving up.");
                postValidationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onWorkflowFinished(activity, null, false);
                    }
                });
            }

        } catch (final Exception e) {

            LOG.debug("",e);
            postValidationHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWorkflowFinished(activity, e, false);
                }
            });
        }

    }

    private void connectToSSLSocket(Socket plainSocket) throws IOException {
        SSLSocketFactory factory = com.casic.titan.googlegps.common.network.Networks.getSocketFactory(activity);
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        if(plainSocket!=null){
            socket = (SSLSocket)factory.createSocket(plainSocket,host,port,true);
        }

        if(serverType == ServerType.SMTP){
            socket.setUseClientMode(true);
            socket.setNeedClientAuth(true);
        }

        socket.setSoTimeout(5000);
        LOG.debug("Starting handshake...");
        socket.startHandshake();
        SSLSession session = socket.getSession();
        Certificate[] servercerts = session.getPeerCertificates();

    }

    private static void onWorkflowFinished(final Activity activity, Exception e, boolean isValid) {


    }
}
