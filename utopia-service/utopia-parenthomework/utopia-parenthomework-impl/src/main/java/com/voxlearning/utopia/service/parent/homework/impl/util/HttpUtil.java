package com.voxlearning.utopia.service.parent.homework.impl.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * http工具类
 *
 * @author Wenlong Meng
 * @since Feb 24, 2019
 */
@Slf4j
public class HttpUtil {

    public static Map<String, String> levels = new LinkedHashMap<>();

    //Logic
    /**
     * 默认连接超时时间
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    /**
     * 默认读取超时时间
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;

    /**
     * 根据短连接获取链接
     *
     * @param surl 短连接
     */
    public static String url(String surl) {
        try{
            HttpClient client = new HttpClient();
            HttpMethod method = new HeadMethod(surl);
            HttpParams params = client.getParams();
            params.setParameter(AllClientPNames.HANDLE_REDIRECTS, false);
            client.executeMethod(method);
            return method.getURI().getURI();
        }catch (Exception e){
            log.error("url({})", surl, e);
            return "";
        }
    }

    /**
     * 使用post请求获取url内容
     *
     * @param url 请求地址
     * @param params 参数
     */
    public static String post(String url, Map<String, String> params) {
        try{
            HttpPost httpPost = buildPost(url, params);
            return process(httpPost, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
        }catch (Exception e){
            log.error("post({}, {})", url, params, e);
            throw new RuntimeException(e);
        }
    }

    private static HttpPost buildPost(String url, Map<String, String> params) throws UnsupportedEncodingException {
        List<NameValuePair> nameValuePairs = Lists.newArrayList();
        if(params != null){
            for(Entry<String, String> entry : params.entrySet()){
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                nameValuePairs.add(pair);
            }
        }
        return getPost(url, nameValuePairs);
    }

    /**
     * 根据URL 组装 HTTP POST
     * @param url
     * @param nameValuePairs
     * @return
     * @throws UnsupportedEncodingException
     */
    private static HttpPost getPost(String url, List<NameValuePair> nameValuePairs) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf8"));
        return httpPost;
    }

    private static String process(final HttpRequestBase httpUriRequest, int connectTimeout, int socketTimeout) throws Exception {
        final CloseableHttpClient client = getClient(connectTimeout, socketTimeout);
        FutureTask<String> fu = new FutureTask<>(()-> {
            CloseableHttpResponse response = client.execute(httpUriRequest);
            HttpEntity entity = null;
            try {
                entity = response.getEntity();
                StatusLine status = response.getStatusLine();
                if (status != null && status.getStatusCode() == 200) {
                    return EntityUtils.toString(entity, "UTF-8");
                } else {
                    int statusCode = status == null ? 0 : status.getStatusCode();
                    log.error("http_fetch_error, request:{}, response status error, status:{}",
                            httpUriRequest.getURI().toString(), statusCode);
                    throw new Exception("http fetch response status error,status=" + statusCode);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                EntityUtils.consume(entity);
            }
        });
        Executors.newCachedThreadPool().execute(fu);
        String content = fu.get(connectTimeout + socketTimeout, TimeUnit.MILLISECONDS);
        return content;
    }

    private static CloseableHttpClient getClient(int connectTimeout, int socketTimeout) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
//        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
//        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
        return client;
    }

    public static Map<String, String> queryString(String url){
        if(ObjectUtils.anyBlank(url) || url.indexOf("?")< 1){
            return Collections.EMPTY_MAP;
        }
        try{
            String queryString = new URI(url).getQuery();
            if(ObjectUtils.anyBlank(queryString)){
                return Collections.EMPTY_MAP;
            }
            Map<String, String> params = Maps.newHashMap();
            for(String q : queryString.split("&")){
                if(q !=null && q.split("=").length==2){
                    String[] kv = q.split("=");
                    params.put(kv[0], kv[1]);
                }
            }
            return params;
        }catch (Exception e){
            log.error("parse({})", url, e);
            return Collections.EMPTY_MAP;
        }

    }

}
