package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.service.ai.api.ChipsActivityService;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.entity.ChipsMiniProgramQR;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsMiniProgramQRDao;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Named
@ExposeService(interfaceClass = ChipsActivityService.class)
public class ChipsActivityServiceImpl implements ChipsActivityService {
    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Inject
    private ChipsMiniProgramQRDao chipsMiniProgramQRDao;

    @Inject
    private ChipsContentService chipsContentService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    private static String MMINI_PROGRAM_QR_PATH = "aiteacher/miniprogram/qr";

    private static String HOST = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host"));

    private static String MINI_PROGRAM_QR_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={}";

    @Override
    public MapMessage processLeadPageVisit(Long userId) {
        userPageVisitCacheManager.addRecord(userId, ConstantSupport.WECHAT_BUSINESS_LEAD_AD_PAGE_KEY);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processAddMiniProgramQR(String content, String path) {
        String token = chipsContentService.getMiniProgramToken();
        if (StringUtils.isBlank(token)) {
            return MapMessage.errorMessage("获取token失败");
        }
        String url = StringUtils.formatMessage(MINI_PROGRAM_QR_URL, token);
        String fileUrl = "";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("scene", content);
            map.put("page", path);
            map.put("width", 280);

            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                    .post(url)
                    .json(JsonUtils.toJson(map))
                    .socketTimeout(10000)
                    .execute();
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", 0L,
                    "mod1", response.getStatusCode(),
                    "mod2", response.getContentType(),
                    "mod3", url,
                    "mod4", response.getResponseString(),
                    "op", "chipsMiniProgramGetQRCode"
            ));
            switch (response.getStatusCode()) {
                case 200:
                    if (response.getContentType().getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        return MapMessage.errorMessage("获取小程序码失败");
                    }
                    String fileName = content + "_" + RandomUtils.nextObjectId();
                    fileUrl = HOST + MMINI_PROGRAM_QR_PATH + "/" + fileName;
                    byte[] bytes = response.getOriginalResponse();
                    @Cleanup InputStream in = new ByteArrayInputStream(bytes);
                    StorageMetadata metadata = new StorageMetadata();
                    metadata.setContentType(response.getContentType().getMimeType());
                    storageClient.upload(in, fileName, MMINI_PROGRAM_QR_PATH, metadata);
                    break;
            }
        } catch (Exception e) {
            log.error("get mini program error. url:{}, content:{}, path", url, content, path, e);
            return MapMessage.errorMessage("获取小程序码失败");
        }

        ChipsMiniProgramQR qr = new ChipsMiniProgramQR();
        qr.setContent(content);
        qr.setCreateDate(new Date());
        qr.setUpdateDate(new Date());
        qr.setDisabled(false);
        qr.setImage(fileUrl);
        qr.setPagePath(path);
        chipsMiniProgramQRDao.insert(qr);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processDeleteMiniProgramQR(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数为空");
        }
        chipsMiniProgramQRDao.disabled(id);
        return MapMessage.successMessage();
    }


}
