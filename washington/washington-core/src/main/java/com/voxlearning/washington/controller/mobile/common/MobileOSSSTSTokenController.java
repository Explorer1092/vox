package com.voxlearning.washington.controller.mobile.common;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/5/14
 */
@Controller
@RequestMapping(value = "/userMobile/sts/")
public class MobileOSSSTSTokenController extends AbstractMobileController {


    // 参数
    private static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    private static final String STS_API_VERSION = "2015-04-01";
    private static final String STS_DEFAULT_POLICY = "{\"Statement\":[{\"Action\":[\"oss:*\"],\"Effect\":\"Allow\",\"Resource\":[\"acs:oss:*:*:*\"]}],\"Version\":\"1\"}";


    @RequestMapping(value = "token.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSTSToken() {
        String bucketName = getRequestString(REQ_BUCKET_CONFIG_NAME);
        if (StringUtils.isBlank(bucketName)) {
            return MapMessage.errorMessage("bucketName 不能为空");
        }
        User user = currentUser();
        if (user == null) {
            return noLoginResult;
        }


        try {
            AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
            AliyunOSSConfig config = configManager.getAliyunOSSConfig(bucketName);
            Objects.requireNonNull(config);

            ProtocolType protocolType = ProtocolType.HTTPS;
            final AssumeRoleResponse stsResponse = assumeRole(
                    config.getAccessId(),
                    config.getAccessKey(),
                    config.getStsRoleArn(),
                    bucketName,
                    STS_DEFAULT_POLICY,
                    protocolType,
                    SafeConverter.toLong(config.getStsExpiration()));
            if (stsResponse == null) {
                return MapMessage.errorMessage("");
            }
            return MapMessage.successMessage()
                    .add(RES_RESULT_ACCESSKEY_ID, stsResponse.getCredentials().getAccessKeyId())
                    .add(RES_RESULT_ACCESSKEY_SECRET, stsResponse.getCredentials().getAccessKeySecret())
                    .add(RES_RESULT_SECURITY_TOKEN, stsResponse.getCredentials().getSecurityToken())
                    .add(RES_RESULT_EXPIRATION, stsResponse.getCredentials().getExpiration())
                    .add(RES_RESULT_RTS_BUCKET_NAME, config.getBucket())
                    .add(RES_RESULT_RTS_ENDPOINT, config.getPublicEndpoint());
        } catch (Exception e) {
            logger.error("get rts error:{}", e);
            return MapMessage.errorMessage("");
        }
    }

    private AssumeRoleResponse assumeRole(String accessKeyId, String accessKeySecret, String roleArn,
                                          String roleSessionName, String policy, ProtocolType protocolType, long durationSeconds) throws ClientException {
        try {
            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU, accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 创建一个 AssumeRoleRequest 并设置请求参数
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion(STS_API_VERSION);
            request.setMethod(MethodType.POST);
            request.setProtocol(protocolType);

            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds);

            // 发起请求，并得到response
            final AssumeRoleResponse response;
            response = client.getAcsResponse(request);

            return response;
        } catch (ClientException e) {
            return null;
        }
    }

}
