package com.voxlearning.utopia.service.ai.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.api.cyclops.Cyclops;
import com.voxlearning.alps.api.cyclops.CyclopsType;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.com.alibaba.dubbo.common.URL;
import com.voxlearning.utopia.service.ai.data.OcrImageDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.*;

@Named
@Slf4j
public class OcrComponent {


    private static final String APP_KEY = "6a4b3f99fdc14b72bceeadcb96909a7f";

    private static final HttpRequestExecutor httpRequestExecutor = HttpRequestExecutor.instance(HttpClientType.POOLING);

    private static final String MATH_OCR_API = RuntimeMode.current().gt(Mode.TEST) ? "http://ac-math.17zuoye.com/compute" : "http://120.92.210.100:8087/compute";
    private static final String MATH_BOT_API = RuntimeMode.current().gt(Mode.TEST) ? "http://megrez-api.17zuoye.net/ocr_formula_symptom" : "http://10.7.12.92:5000/ocr_formula_symptom";
    private static final String LATEX_SVG_API = "https://mathjax.hjfile.cn/?tex=%s";

    private static final int SO_TIMEOUT = 60000;

    public MapMessage mathOcrApi(byte[] bytes,String openId, Long userId, String sys) {
        return mathOcrApi(bytes, openId, userId, "", sys);
    }


    public MapMessage mathOcrApi(byte[] bytes, Long userId, String sys) {
        return mathOcrApi(bytes, "", userId, "", sys);
    }


    public MapMessage mathOcrApi(byte[] bytes, String deviceId, Long userId, String sessionId, String sys) {
        return callOcrApi(bytes, deviceId, userId, sessionId, sys, "math", "E");
    }

    private MapMessage callOcrApi(byte[] bytes, String deviceId, Long userId, String sessionId, String sys, String dtype, String mode) {
        Instant start = Instant.now();
        try {
            return doCallOcrApi(bytes, deviceId, userId, sessionId, sys, dtype, mode);
        } finally {
            Instant stop = Instant.now();
            long duration = stop.toEpochMilli() - start.toEpochMilli();
            Cyclops.builder()
                    .id("utopia")
                    .type(CyclopsType.INVOCATION)
                    .time(stop.toEpochMilli())
                    .measurement("utopia-ai-provider.business")
                    .duration(duration)
                    .tag("mode", "MATH_OCR_API")
                    .send();
        }
    }

    private MapMessage doCallOcrApi(byte[] bytes, String deviceId, Long userId, String sessionId, String sys, String dtype, String mode) {
        Map<String, String> hs = new HashMap<>();
        hs.put("appkey", APP_KEY);
        hs.put("protocol", "http");
        hs.put("device-id", deviceId);
        hs.put("user-id", SafeConverter.toString(userId));
        hs.put("session-id", sessionId);
        hs.put("sys", sys);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("dtype", dtype);
        builder.addTextBody("mode", mode);

        builder.addBinaryBody("image", bytes, ContentType.MULTIPART_FORM_DATA, "image");

        try {

            AlpsHttpResponse resp = httpRequestExecutor
                    .post(MATH_OCR_API).headers(hs).entity(builder.build()).socketTimeout(SO_TIMEOUT)
                    .execute();

            String result = resp.getResponseString();

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", deviceId,
                    "mod2", sessionId,
                    "mod3", sys,
                    "mod4", dtype,
                    "mod5", mode,
                    "mod6", MATH_OCR_API,
                    "mod7", result,
                    "op", "ocrApi"
            ));


            if (200 != resp.getStatusCode()) {
                log.error("Tobbit program OCR failed, code: {} ,message: {}", resp.getStatusCode(), resp.getResponseString());
                return MapMessage.errorMessage("空间站异常,正在努力抢修中!");
            }

            _MathOcrResults ret = JsonUtils.fromJson(result, _MathOcrResults.class);

            if (ret == null) {
                log.error("Tobbit program OCR parsing failed,message: {}", resp.getResponseString());
                return MapMessage.errorMessage("空间站异常,正在努力抢修中!");
            }
            //error code.0 is correct response, others will be setOcrCache when error happened:1 is can't recognize any formula, 2 is no valid formula.
            int code = SafeConverter.toInt(ret.code);

            if (0 == code) {
                OcrImageDto dto = _location(ret);
                if (dto != null) {
                    dto.setOrigin_json(result);
                }
                return MapMessage.successMessage().add("data", dto);

            } else {
                log.error("Tobbit program OCR failed, code: {} ,message: {}", ret.code, ret.message);
                return MapMessage.errorMessage("图片好像有些问题，再试试");
            }
        } catch (Exception e) {
            log.error("Tobbit program OCR Error, message: {}", e.getMessage());
            return MapMessage.errorMessage("图片好像有些问题，再试试");
        }
    }


    public MapMessage mathBotApi(Long uid, List<String> latexs) {
        Assertions.notEmpty(latexs);

        // Escape
//        latex = latex.stream().map(x -> x.replaceAll("\\\\","\\\\\\\\")).collect(Collectors.toList());

        Map<String, Object> param = new HashMap<>();
        param.put("uid", uid);
        param.put("equations", latexs);

        String json = JsonUtils.toJson(param);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        try {

            AlpsHttpResponse resp = httpRequestExecutor
                    .post(MATH_BOT_API).entity(entity).socketTimeout(SO_TIMEOUT)
                    .execute();

            String result = resp.getResponseString();

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", uid,
                    "mod1", json,
                    "mod2", MATH_BOT_API,
                    "mod3", result,
                    "op", "mathBotApi"
            ));

            if (200 != resp.getStatusCode()) {
                return MapMessage.errorMessage("空间站异常,正在努力抢修中!");
            }

            _MathBotResp mbr = JsonUtils.fromJson(result, _MathBotResp.class);

            if (mbr == null) {
                mbr = _MathBotResp.defaultInstance();
            }
            // hidden uid
            mbr.setUid("");


            return MapMessage.successMessage().setInfo(JsonUtils.toJson(mbr));
        } catch (Exception e) {
            log.error("MathBot api error,message: {}", e.getMessage());
            return MapMessage.errorMessage("空间站异常,正在努力抢修中!");
        }
    }

    public String latexSvg(String latex) {
        Assertions.notBlank(latex);

        String uri = String.format(LATEX_SVG_API, URL.encode(latex));

        // Fake headers
        Map<String, String> hs = new HashMap<>();
        hs.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        hs.put("Accept-Charset", "UTF-8,*;q=0.5");
        hs.put("Accept-Encoding", "gzip,deflate,sdch");
        hs.put("Accept-Language", "en-US,en;q=0.8");
        hs.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:60.0) Gecko/20100101 Firefox/60.0");

        AlpsHttpResponse resp = httpRequestExecutor
                .get(uri).headers(hs).execute();

        String result = resp.getResponseString();

        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "mod1", latex,
                "mod2", uri,
                "mod3", result,
                "op", "latexSvgApi"
        ));

        if (200 != resp.getStatusCode()) {
            return "";
        }

        return result;

    }


    private OcrImageDto _location(_MathOcrResults ret) {
        OcrImageDto dto = null;
        try {
            dto = new OcrImageDto();
            dto.img_height = ret.img_height;
            dto.img_width = ret.img_width;
            dto.img_url = ret.img_url;
            List<OcrImageDto.OcrForms> forms = new ArrayList<>();
            dto.setForms(forms);
            for (_MathOcrResults._Forms form : ret.forms) {

                OcrImageDto.OcrForms tmp = new OcrImageDto.OcrForms();
                List<_MathOcrResults._Answers> answers = form.answers;
                if (answers.size() < 1) {
                    return null;
                }
                _MathOcrResults._Answers answer = answers.get(0);

                tmp.judge = answer.judge;
                tmp.text = answer.text;


                List<_MathOcrResults._Coordinate> cd = answer.coordinate;
                _MathOcrResults._Coordinate p1 = cd.get(0);
                _MathOcrResults._Coordinate p2 = cd.get(1);
                _MathOcrResults._Coordinate p3 = cd.get(2);
                _MathOcrResults._Coordinate p4 = cd.get(3);

                OcrImageDto.OcrPosition pos = new OcrImageDto.OcrPosition();
                pos.x = p1.x;
                pos.y = p1.y;
                pos.w = p3.x - p1.x;
                pos.h = p3.y - p1.y;
                tmp.position = pos;

                List<OcrImageDto.OrcCoordinate> coordinate = new ArrayList<>();
                coordinate.add(new OcrImageDto.OrcCoordinate(p1.getX(), p1.getY()));
                coordinate.add(new OcrImageDto.OrcCoordinate(p2.getX(), p2.getY()));
                coordinate.add(new OcrImageDto.OrcCoordinate(p3.getX(), p3.getY()));
                coordinate.add(new OcrImageDto.OrcCoordinate(p4.getX(), p4.getY()));

                tmp.coordinate = coordinate;

                forms.add(tmp);
            }
        } catch (Exception e) {
            log.error("Tobbit program OCR parsing location failed,message: {}", ret.message);
        }

        return dto;
    }


    //~ DTO Object private

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    static class _MathBotResp {
        String uid;
        List<_MathBotResult> results;

        public static _MathBotResp defaultInstance() {
            _MathBotResp instance = new _MathBotResp();
            instance.uid = "";
            instance.results = new ArrayList<>();
            _MathBotResult tmp = new _MathBotResult();
            tmp.analysis = "";
            tmp.symptom = "";
            instance.results.add(tmp);
            return instance;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    private static class _MathBotResult {
        String analysis;
        String symptom;
        String latex_svg;
    }

    @Data
    private static class _MathOcrResults {
        String version;
        int code;
        String message;
        String img_id;
        String img_height;
        String img_width;
        String img_url;
        String number;
        List<_Forms> forms;
        String srv_type;

        @Data
        static class _Forms {
            List<_Coordinate> coordinate;
            List<_Answers> answers;
        }

        @Data
        static class _Answers {
            List<_Coordinate> coordinate;
            int judge;
            String text;
        }

        @Data
        static class _Coordinate {
            int x;
            int y;
        }

    }


}
