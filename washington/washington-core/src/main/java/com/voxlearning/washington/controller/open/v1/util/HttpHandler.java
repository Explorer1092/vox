package com.voxlearning.washington.controller.open.v1.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by Hailong Yang on 2015/10/9.
 */
public abstract class HttpHandler {

    public static HttpResponse sendXMLDataByPost(String url, String xmlData) throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(xmlData);
        post.setEntity(entity);
        post.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
        return client.execute(post);
    }

}
