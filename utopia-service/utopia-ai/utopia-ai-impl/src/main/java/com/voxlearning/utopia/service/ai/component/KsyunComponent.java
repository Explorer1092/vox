package com.voxlearning.utopia.service.ai.component;

import com.voxlearning.alps.api.cyclops.Cyclops;
import com.voxlearning.alps.api.cyclops.CyclopsType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Named
@Slf4j
public class KsyunComponent {


    private static final String ACCESS_KEY = "AKLTqxKc-Po4RMuwKWjIfX9KUA";
    private static final String SECRET_KEY = "OK/f6lOETY+ZIa/9YvVMT0hKO8cZVWlRN6bDFFHPrOazL+AI2dgpX7M6+NUjBz5Urw==";

    private static final HttpRequestExecutor httpRequestExecutor = HttpRequestExecutor.instance(HttpClientType.POOLING);

    private static final String KIR_API = "http://kir.api.ksyun.com";

    private static final int SO_TIMEOUT=60000;


    public boolean passImage(List<String> imageUrls) {


        Instant start = Instant.now();
        try {
            return _passImage(imageUrls);
        } finally {
            Instant stop = Instant.now();
            long duration = stop.toEpochMilli() - start.toEpochMilli();
            Cyclops.builder()
                    .id("utopia")
                    .type(CyclopsType.INVOCATION)
                    .time(stop.toEpochMilli())
                    .measurement("utopia-ai-provider.business")
                    .duration(duration)
                    .tag("mode", "KIR_KSYUN_API")
                    .send();
        }

    }

    private boolean _passImage(List<String> imageUrls) {


        if (CollectionUtils.isEmpty(imageUrls)) {
            return true;
        }
        _ImageReq req = new _ImageReq();
        req.image_urls = imageUrls;

        String method = "POST";
        String service = "kir";
        String host = "kir.api.ksyun.com";
        String contenttype = "application/json";
        String region = "cn-beijing-6";
        String request_parameters = "Action=ClassifyImage&Version=2017-11-07";

        String postData = JsonUtils.toJson(req);

        try {


            Date t = new Date();
            SimpleDateFormat timeFormater = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            timeFormater.setTimeZone(TimeZone.getTimeZone("UTC"));

            String amzdate = timeFormater.format(t);

            SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
            timeFormater.setTimeZone(TimeZone.getTimeZone("UTC"));

            String datestamp = dateFormater.format(t);

            String canonical_uri = "/";
            String canonical_headers = "content-type:" + contenttype + "\n" + "host:" + host + "\n" + "x-amz-date:" + amzdate + "\n";
            String signed_headers = "content-type;host;x-amz-date";
            String payload_hash = toHex(hash(postData));
            String canonical_request = method + '\n' + canonical_uri + '\n' + request_parameters + '\n' + canonical_headers + '\n' + signed_headers + '\n' + payload_hash;

            String algorithm = "AWS4-HMAC-SHA256";
            String credential_scope = datestamp + '/' + region + '/' + service + '/' + "aws4_request";
            String string_to_sign = algorithm + '\n' + amzdate + '\n' + credential_scope + '\n' + toHex(hash(canonical_request));


            byte[] signing_key = getSignatureKey(SECRET_KEY, datestamp, region, service);
            String signature = toHex(hmacSHA256(string_to_sign, signing_key));

            String authorization_header = algorithm + ' ' + "Credential=" + ACCESS_KEY + '/' + credential_scope + ", " + "SignedHeaders=" + signed_headers + ", " + "Signature=" + signature;


            String api = KIR_API + '?' + request_parameters;


            Map<String, String> hs = new HashMap<>();
            hs.put("x-amz-date", amzdate);
            hs.put("Authorization", authorization_header);
            hs.put("Content-Type", contenttype);


            StringEntity entity = new StringEntity(postData);

            AlpsHttpResponse resp = httpRequestExecutor
                    .post(api).headers(hs).entity(entity).socketTimeout(SO_TIMEOUT)
                    .execute();

            String result = resp.getResponseString();

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", imageUrls,
                    "mod2", api,
                    "mod3", result,
                    "op", "ksyunApi"
            ));

            if (200 != resp.getStatusCode()) {
                return true;
            }

            _ImageResp ir = JsonUtils.fromJson(result, _ImageResp.class);

            if (ir == null) {
                return true;
            }

            if (ir.header.err_no == 200) {
                Map<String, Object> map = new HashMap<>();

                int npc = 0;
                for (_ImageResp._ImageBody x : ir.body) {

                    if ("normal".equals(x.suggest_summary)) {
                        map.put(x.image_url, true);
                    } else {
                        map.put(x.image_url, false);
                        npc++;
                    }
                }
                if (npc > 0) {
                    return false;
                }

            }

        } catch (Exception e) {
            log.error("Ksyun api error,cause:{}", e.getMessage());
        }

        return true;
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


    //~ private dto

    @Data
    private static class _ImageReq {
        List<String> business = Collections.singletonList("porn"); //业务类型  ---> porn 色情图片识别
        List<String> image_urls;
    }

    @Data
    private static class _ImageResp {
        _Header header;
        double cost; //cost为服务端耗时，单位秒
        String request_id;
        long request_time;
        List<_ImageBody> body;


        @Data
        static class _Header {
            int err_no; //err_no返回200，msg为success，否则返回对应错误码及错误信息
            String err_msg;
        }

        @Data
        static class _ImageBody {
            String data_id; //唯一标识该图片
            String image_url;
            int err_no; //仅图片格式出现错误有此信息
            String err_msg; //仅图片格式出现错误有此信息
            List<ImageResult> results;
            String suggest_summary; //confirm_reject:拒绝；suggest_review:疑似；normal:通过
            String suggest_summary_code;
            String suggest_summary_message;

            @Data
            static class ImageResult {
                int err_no; //仅此业务结果出现错误有此信息
                String err_msg; //仅此业务结果出现错误有此信息
                String business;
                String suggest;
                String label; //分类标签ID(int类型) ---->  porn: 1正常，2低俗，3色情
                String label_desc; //分类标签(string类型) ---->  porn: 1正常，2低俗，3色情
                double rate; //分类置信度 [0, 1.0]，值越大置信度越高
                int review; //0不需要人工复审，1需人工复审
                String task_id; //唯一标示该次检测任务
            }
        }
    }
}






