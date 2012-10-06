package com.yammer.metrics.reporting;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class HttpTransport implements Transport {
    private final HttpClient client;
    private final String seriesUrl;

    public HttpTransport(String host, String apiKey, String appKey) {
        this.client = new DefaultHttpClient(new PoolingClientConnectionManager());
        String url = String.format("https://%s/api/v1/series?api_key=%s", host, apiKey);
        if (appKey!=null){
        	url+="&application_key="+appKey;
        }
        this.seriesUrl=url;
    }

    public HttpRequest prepare() throws IOException {
        HttpPost post = new HttpPost(seriesUrl);
        return new HttpRequest(client, post);
    }

    public static class HttpRequest implements Transport.Request {
        private final HttpPost postRequest;
        private final ByteArrayOutputStream out;
        private final HttpClient requestClient;

        public HttpRequest(HttpClient requestClient, HttpPost postRequest) throws IOException {
            this.requestClient = requestClient;
            this.postRequest = postRequest;
            this.postRequest.addHeader("Content-Type", "application/json");
            this.out = new ByteArrayOutputStream();
        }

        public OutputStream getBodyWriter() {
            return out;
        }

        public void send() throws Exception {
            try {
                out.flush();
                out.close();
                postRequest.setEntity(new ByteArrayEntity(out.toByteArray()));
                HttpResponse response = requestClient.execute(postRequest);
                EntityUtils.consumeQuietly(response.getEntity());
            } finally {
                postRequest.reset();    // We don't reuse the postRequest between metrics pushes - but this
            }
        }
    }
}