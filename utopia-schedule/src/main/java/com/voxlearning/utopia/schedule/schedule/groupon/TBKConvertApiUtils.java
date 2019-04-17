package com.voxlearning.utopia.schedule.schedule.groupon;

import com.voxlearning.alps.core.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by xiang.lv on 2016/10/8.
 *
 * @author xiang.lv
 * @date 2016/10/8   14:01
 */
public class TBKConvertApiUtils {
    /**
     * 淘宝客测试示例
     */
        //官方示例:http://open.taobao.com/doc2/detail.htm?articleId=130&docType=1&treeId=null
        private static final String SIGN_METHOD_MD5 = "md5";
        private static final String SIGN_METHOD_HMAC = "hmac";
        private static final String CHARSET_UTF8 = "utf-8";
        private static final String CONTENT_ENCODING_GZIP = "gzip";

        // TOP服务地址，正式环境需要设置为http://gw.api.taobao.com/router/rest,沙箱地址　http://gw.api.tbsandbox.com/router/rest
        private static final String SERVER_URL = "http://gw.api.taobao.com/router/rest";
        private static final String APP_KEY = "23454119"; //淘宝开放平台家长通IOS版AppKey
        private static final String APP_SECRET = "6ff1629f29f8ec729c1416b34bc84963"; // 可替换为您的沙箱环境应用的appSecret
        private static final String ADZONE_ID="61706360";//广告位ID
        public static void main(String[] args) throws Exception {
            System.out.println(getSellerItem("40902375241,39337539310"));
        }


        public  static String getSellerItem(String numIids)throws IOException{
                Map<String, String> params = new HashMap<String, String>();
                // 公共参数
                params.put("method", "taobao.tbk.item.convert");
                params.put("app_key", APP_KEY);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                params.put("timestamp", df.format(new Date()));
                params.put("format", "json");
                params.put("v", "2.0");
                params.put("adzone_id", ADZONE_ID);
                params.put("platform", "2");
                params.put("sign_method", SIGN_METHOD_HMAC);
                // 业务参数
                params.put("fields", "num_iid,click_url");//需要返回字段
                params.put("num_iids", numIids);//天猫商品id
                // 签名参数
                params.put("sign", signTopRequest(params, APP_SECRET, SIGN_METHOD_HMAC));
                // 请用API
                return callApi(new URL(SERVER_URL), params);
        }

        /**
         * 对TOP请求进行签名。
         */
        private static String signTopRequest(Map<String, String> params, String secret, String signMethod) throws IOException {
            // 第一步：检查参数是否已经排序
            String[] keys = params.keySet().toArray(new String[0]);
            Arrays.sort(keys);

            // 第二步：把所有参数名和参数值串在一起
            StringBuilder query = new StringBuilder();
            if (SIGN_METHOD_MD5.equals(signMethod)) {
                query.append(secret);
            }
            for (String key : keys) {
                String value = params.get(key);
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                    query.append(key).append(value);
                }
            }

            // 第三步：使用MD5/HMAC加密
            byte[] bytes;
            if (SIGN_METHOD_HMAC.equals(signMethod)) {
                bytes = encryptHMAC(query.toString(), secret);
            } else {
                query.append(secret);
                bytes = encryptMD5(query.toString());
            }

            // 第四步：把二进制转化为大写的十六进制
            return byte2hex(bytes);
        }

        /**
         * 对字节流进行HMAC_MD5摘要。
         */
        private static byte[] encryptHMAC(String data, String secret) throws IOException {
            byte[] bytes = null;
            try {
                SecretKey secretKey = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), "HmacMD5");
                Mac mac = Mac.getInstance(secretKey.getAlgorithm());
                mac.init(secretKey);
                bytes = mac.doFinal(data.getBytes(CHARSET_UTF8));
            } catch (GeneralSecurityException gse) {
                throw new IOException(gse.toString());
            }
            return bytes;
        }

        /**
         * 对字符串采用UTF-8编码后，用MD5进行摘要。
         */
        private static byte[] encryptMD5(String data) throws IOException {
            return encryptMD5(data.getBytes(CHARSET_UTF8));
        }

        /**
         * 对字节流进行MD5摘要。
         */
        private static byte[] encryptMD5(byte[] data) throws IOException {
            byte[] bytes = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                bytes = md.digest(data);
            } catch (GeneralSecurityException gse) {
                throw new IOException(gse.toString());
            }
            return bytes;
        }

        /**
         * 把字节流转换为十六进制表示方式。
         */
        private static String byte2hex(byte[] bytes) {
            StringBuilder sign = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xFF);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex.toUpperCase());
            }
            return sign.toString();
        }

        private static String callApi(URL url, Map<String, String> params) throws IOException {
            String query = buildQuery(params, CHARSET_UTF8);
            byte[] content = {};
            if (query != null) {
                content = query.getBytes(CHARSET_UTF8);
            }

            HttpURLConnection conn = null;
            OutputStream out = null;
            String rsp = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Host", url.getHost());
                conn.setRequestProperty("Accept", "text/xml,text/javascript");
                conn.setRequestProperty("User-Agent", "top-sdk-java");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET_UTF8);
                out = conn.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(conn);
            } finally {
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return rsp;
        }

        private static String buildQuery(Map<String, String> params, String charset) throws IOException {
            if (params == null || params.isEmpty()) {
                return null;
            }

            StringBuilder query = new StringBuilder();
            Set<Map.Entry<String, String>> entries = params.entrySet();
            boolean hasParam = false;

            for (Map.Entry<String, String> entry : entries) {
                String name = entry.getKey();
                String value = entry.getValue();
                // 忽略参数名或参数值为空的参数
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
                    if (hasParam) {
                        query.append("&");
                    } else {
                        hasParam = true;
                    }

                    query.append(name).append("=").append(URLEncoder.encode(value, charset));
                }
            }

            return query.toString();
        }

        private static String getResponseAsString(HttpURLConnection conn) throws IOException {
            String charset = getResponseCharset(conn.getContentType());
            if (conn.getResponseCode() < 400) {
                String contentEncoding = conn.getContentEncoding();
                if (CONTENT_ENCODING_GZIP.equalsIgnoreCase(contentEncoding)) {
                    return getStreamAsString(new GZIPInputStream(conn.getInputStream()), charset);
                } else {
                    return getStreamAsString(conn.getInputStream(), charset);
                }
            } else {// Client Error 4xx and Server Error 5xx
                throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        }

        private static String getStreamAsString(InputStream stream, String charset) throws IOException {
            try {
                Reader reader = new InputStreamReader(stream, charset);
                StringBuilder response = new StringBuilder();

                final char[] buff = new char[1024];
                int read = 0;
                while ((read = reader.read(buff)) > 0) {
                    response.append(buff, 0, read);
                }

                return response.toString();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private static String getResponseCharset(String ctype) {
            String charset = CHARSET_UTF8;
            if (StringUtils.isNotBlank(ctype)) {
                String[] params = ctype.split(";");
                for (String param : params) {
                    param = param.trim();
                    if (param.startsWith("charset")) {
                        String[] pair = param.split("=", 2);
                        if (pair.length == 2) {
                            if (StringUtils.isNotBlank(pair[1])) {
                                charset = pair[1].trim();
                            }
                        }
                        break;
                    }
                }
            }

            return charset;
        }

    }

