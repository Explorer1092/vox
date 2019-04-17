package com.voxlearning.washington.controller.open.v1;

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
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-9-22
 */
@Controller
@RequestMapping(value = "/v1/sts/")
public class OSSSTSTokenController extends AbstractApiController {

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    // 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
    private static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    private static final String STS_API_VERSION = "2015-04-01";
    private static final String STS_DEFAULT_POLICY = "{\"Statement\":[{\"Action\":[\"oss:*\"],\"Effect\":\"Allow\",\"Resource\":[\"acs:oss:*:*:*\"]}],\"Version\":\"1\"}";

    @RequestMapping(value = "token.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSTSToken() {
        try {
            validateRequired(REQ_BUCKET_CONFIG_NAME, "bucket名称");
            if (hasSessionKey()) {
                validateRequest(REQ_BUCKET_CONFIG_NAME);
            } else {
                validateRequestNoSessionKey(REQ_BUCKET_CONFIG_NAME);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String stopClientSts = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STOP_CLIENT_STS");
        Boolean requestIsBaned = StringUtils.equalsIgnoreCase(stopClientSts, "true");
        if (requestIsBaned) {
            //提示信息客户端也不显示。直接返回空
            return failMessage("");
        }
        try {
            String bucketName = getRequestString(REQ_BUCKET_CONFIG_NAME);
            VendorApps requestApp = getApiRequestApp();

            AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
            AliyunOSSConfig config = configManager.getAliyunOSSConfig(bucketName);
            //没拿到配置
            if (config == null) {
                logger.error("get oss config error={}",bucketName);
                return failMessage("");
            }

            String session = (requestApp == null ? "" : requestApp.getAppKey()) + bucketName;
            ProtocolType protocolType = ProtocolType.HTTPS;
            final AssumeRoleResponse stsResponse = assumeRole(
                    config.getAccessId(),
                    config.getAccessKey(),
                    config.getStsRoleArn(),
                    session,
                    STS_DEFAULT_POLICY,
                    protocolType,
                    SafeConverter.toLong(config.getStsExpiration()));
            if (stsResponse == null) {
                return failMessage("");
            }
            return successMessage()
                    .add(RES_RESULT_ACCESSKEY_ID, stsResponse.getCredentials().getAccessKeyId())
                    .add(RES_RESULT_ACCESSKEY_SECRET, stsResponse.getCredentials().getAccessKeySecret())
                    .add(RES_RESULT_SECURITY_TOKEN, stsResponse.getCredentials().getSecurityToken())
                    .add(RES_RESULT_EXPIRATION, stsResponse.getCredentials().getExpiration())
                    .add(RES_RESULT_RTS_BUCKET_NAME, config.getBucket())
                    .add(RES_RESULT_RTS_ENDPOINT, config.getPublicEndpoint());
        } catch (Exception e) {
            logger.error("get rts error:{}", e);
            return failMessage("");
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
