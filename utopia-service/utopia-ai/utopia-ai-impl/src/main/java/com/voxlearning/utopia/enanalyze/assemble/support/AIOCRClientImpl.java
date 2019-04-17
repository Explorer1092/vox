package com.voxlearning.utopia.enanalyze.assemble.support;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.enanalyze.assemble.AIConstant;
import com.voxlearning.utopia.enanalyze.assemble.AIOCRClient;
import com.voxlearning.utopia.enanalyze.exception.support.ThirdPartyServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Service;

/**
 * 实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Service
public class AIOCRClientImpl extends AIAbstractClient implements AIOCRClient {

    /**
     * 请求ai侧数据
     *
     * @param request 请求
     * @return 结果
     */
    @Override
    public Result ocr(Request request) {
        Result result;
        POST post = super.post(ServiceDefinition.AI_OCR);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        builder.addBinaryBody("image", request.getBytes(), ContentType.MULTIPART_FORM_DATA, "image");
        post.entity(builder.build());
        AlpsHttpResponse _response = post.execute();
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", 0L,
                "mod1", _response.getStatusCode(),
                "mod2", _response.getResponseString(),
                "mod3", ServiceDefinition.AI_OCR.name(),
                "op", "aiUserOCRNLP"
        ));
        int statusCode = _response.getStatusCode();
        switch (statusCode) {
            case 200: {
                String responseText = _response.getResponseString();
                result = JSON.parseObject(responseText, AIOCRClient.Result.class);
                AIConstant.Code code = AIConstant.Code.of(result.getCode());
                if (code.SUCCESS)
                    return result;
                else {
                    throw new ThirdPartyServiceException(code.TEXT);
                }
            }
            case 400: {
                throw new ThirdPartyServiceException("诶？好像和基地失去了联系!");
            }
            case -1: {
                throw new ThirdPartyServiceException("诶？好像和基地失去了联系!");
            }
            default:
                throw new ThirdPartyServiceException("诶？好像和基地失去了联系!");
        }
    }
}
