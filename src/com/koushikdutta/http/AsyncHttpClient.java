package com.koushikdutta.http;

import android.net.Uri;
import android.os.AsyncTask;

import com.codebutler.android_websockets.WebSocketClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * 
 * Created by Vinay S Shenoy on 07/09/2013
 */
public class AsyncHttpClient {

    public AsyncHttpClient() {

    }

    public static class SocketIORequest {

        private String mUri;
        private String mEndpoint;

        public SocketIORequest(String uri) {
            this(uri, null);
        }

        public SocketIORequest(String uri, String endpoint) {

            mUri = Uri.parse(uri).buildUpon().encodedPath("/socket.io/1/").build().toString();
            mEndpoint = endpoint;
        }

        public String getUri() {

            return mUri;
        }

        public String getEndpoint() {

            return mEndpoint;
        }
    }

    public static interface StringCallback {
        public void onCompleted(final Exception e, String result);
    }

    public static interface WebSocketConnectCallback {
        public void onCompleted(Exception ex, WebSocketClient webSocket);
    }

    public void executeString(final SocketIORequest socketIORequest, final StringCallback stringCallback) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... p) {

                SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("https", sf, 443));

                HttpParams params = new BasicHttpParams();

                SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
                HttpClient httpClient = new DefaultHttpClient(mgr, params);

                try {

                    HttpPost post = new HttpPost(socketIORequest.getUri());

                    HttpResponse res = httpClient.execute(post);
                    String responseString = readToEnd(res.getEntity().getContent());

                    if (stringCallback != null) {
                        stringCallback.onCompleted(null, responseString);
                    }

                } catch (Exception e) {

                    if (stringCallback != null) {
                        stringCallback.onCompleted(e, null);
                    }
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
                return null;
            }
        }.execute();
    }

    private byte[] readToEndAsArray(InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);
        byte[] stuff = new byte[1024];
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        int read = 0;
        while ((read = dis.read(stuff)) != -1) {
            buff.write(stuff, 0, read);
        }

        return buff.toByteArray();
    }

    private String readToEnd(InputStream input) throws IOException {
        return new String(readToEndAsArray(input));
    }

}
