package com.voxlearning.utopia.core.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.utopia.core.helper.classify.images.ClassifyImagesReponseBody;
import com.voxlearning.utopia.core.helper.classify.images.JinShanClassifyImagesPostBody;
import org.slf4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 11:49 AM
 * \* Description:金山鉴黄工具类
 * \
 */
public class ClassifyImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassifyImageUtils.class);

    /**
     * 检查图片涉黄
     *
     * @param imageUrls
     * @return
     */
    public static ClassifyImagesReponseBody checkImage(List<String> imageUrls) throws Exception {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return null;
        }
        JinShanClassifyImagesPostBody postBody = new JinShanClassifyImagesPostBody();
        postBody.setImageUrls(imageUrls);
        ClassifyImagesReponseBody reponseBody = null;
        String method = "POST";
        String service = "kir";
        String host = "kir.api.ksyun.com";
        String contenttype = "application/json";
        String region = "cn-beijing-6";
        String endpoint = "http://kir.api.ksyun.com";
        String request_parameters = "Action=ClassifyImage&Version=2017-11-07";
        final String accessKey;
        final String secretKey;
        accessKey = ConfigManager.instance().getCommonConfig().getConfigs().get("jinshan_app_key");
        if (StringUtils.isEmpty(accessKey)) {
            throw new ConfigurationException("No 'jinshan_app_key' defined");
        }
        secretKey = ConfigManager.instance().getCommonConfig().getConfigs().get("jinshan_secret_key");
        if (StringUtils.isEmpty(secretKey)) {
            throw new ConfigurationException("No 'jinshan_secret_key' defined");
        }
        String postData = JSONObject.toJSONString(postBody);
        Date t = new Date();
        SimpleDateFormat timeFormater = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        timeFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        String amzdate = timeFormater.format(t);
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
        timeFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        String datestamp = dateFormater.format(t);
        String canonical_uri = "/";
        String canonical_querystring = request_parameters;
        String canonical_headers = "content-type:" + contenttype + "\n" + "host:" + host + "\n" + "x-amz-date:" + amzdate + "\n";
        String signed_headers = "content-type;host;x-amz-date";
        String payload_hash = toHex(hash(postData));
        String canonical_request = method + '\n' + canonical_uri + '\n' + canonical_querystring + '\n' + canonical_headers + '\n' + signed_headers + '\n' + payload_hash;
        String algorithm = "AWS4-HMAC-SHA256";
        String credential_scope = datestamp + '/' + region + '/' + service + '/' + "aws4_request";
        String string_to_sign = algorithm + '\n' + amzdate + '\n' + credential_scope + '\n' + toHex(hash(canonical_request));
        byte[] signing_key = getSignatureKey(secretKey, datestamp, region, service);
        String signature = toHex(hmacSHA256(string_to_sign, signing_key));
        String authorization_header = algorithm + ' ' + "Credential=" + accessKey + '/' + credential_scope + ", " + "SignedHeaders=" + signed_headers + ", " + "Signature=" + signature;
        String request_url = endpoint + '?' + canonical_querystring;
        HttpURLConnection connection = (HttpURLConnection) new URL(request_url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("x-amz-date", amzdate);
        connection.setRequestProperty("Authorization", authorization_header);
        connection.setRequestProperty("Content-Type", contenttype);
        connection.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(postData);
        wr.flush();
        try {
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                String content = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                reader.close();
                reponseBody = JSON.parseObject(content, ClassifyImagesReponseBody.class);
                return reponseBody;
            } else {
                logger.error("ClassifyImageUtils_checkImage reponseMsg : {} ,reponseCoce : {} "
                        , connection.getResponseMessage(), connection.getResponseCode());
            }
        } catch (Exception e) {
            logger.error("ClassifyImageUtils_checkImage reponseMsg : {} ,reponseCoce : {} "
                    , connection.getResponseMessage(), connection.getResponseCode(), e);
        }
        return reponseBody;
    }

    public static String toHex(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    static byte[] hash(String text) throws Exception {
        if (text == null)
            text = "";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes("UTF8"));
        return md.digest();
    }

    static byte[] hmacSHA256(String data, byte[] key) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        byte[] kSigning = hmacSHA256("aws4_request", kService);
        return kSigning;
    }
}
