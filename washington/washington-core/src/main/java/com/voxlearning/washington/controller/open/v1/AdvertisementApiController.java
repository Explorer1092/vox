package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.advertisement.constants.AdConstants;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementDetail;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.consumer.AdvertisementLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-11-13
 */
@Controller
@RequestMapping(value = "/v1/be")
public class AdvertisementApiController extends AbstractApiController {

    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private AdvertisementLoaderClient advertisementLoaderClient;


    /**
     * 单广告位接口，参数名参考{@link com.voxlearning.washington.controller.open.ApiConstants#REQ_AD_POSITION}
     *
     * @return
     */
    @RequestMapping(value = "info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adInfo() {
        try {
            validateRequired(REQ_AD_POSITION, "广告位ID");
            if (hasSessionKey()) {
                validateRequest(REQ_AD_POSITION);
            } else {
                validateRequestNoSessionKey(REQ_AD_POSITION);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String slotId = getRequestString(REQ_AD_POSITION);
        User requestUser = getApiRequestUser();
        Long userId = requestUser == null ? null : requestUser.getId();

        //根据广告位id查询广告信息
        List<Map<String, Object>> mapList = this.getAdById(userId, slotId);
        if(mapList == null){
            return successMessage();
        }

        int closeTime = 0;
        if ("220105".equals(slotId)) {
            String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "BALL_AD_CLOSE_TIME");
            closeTime = SafeConverter.toInt(configValue) * 60;
        }

        return successMessage().add(RES_RESULT_AD_INFO, mapList).add(RES_RESULT_AD_CLOSE_TIME, closeTime);
    }

    /**
     * 多广告位接口, 参数名参考{@link com.voxlearning.washington.controller.open.ApiConstants#REQ_AD_POSITIONS},多个广告位id以","分隔
     *
     * @return 广告位id作为key，对应广告信息作为value
     */
    @RequestMapping(value = "infos.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adInfos() {
        try {
            validateRequired(REQ_AD_POSITIONS, "广告位IDS");
            if (hasSessionKey()) {
                validateRequest(REQ_AD_POSITIONS);
            } else {
                validateRequestNoSessionKey(REQ_AD_POSITIONS);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String[] slotIds = getRequestString(REQ_AD_POSITIONS).split(",");
        User requestUser = getApiRequestUser();
        Long userId = requestUser == null ? null : requestUser.getId();
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        //依次查询每个广告位
        for (String slotId : slotIds) {
            List<Map<String, Object>> ad = this.getAdById(userId, slotId);
            if(ad != null){
                result.put(slotId, ad);
            }
        }

        return successMessage().add(RES_RESULT_AD_INFOS, result);
    }

    /**
     * 根据id获取广告位信息
     *
     * @param userId 用户id
     * @param slotId 广告位id
     * @return
     */
    private List<Map<String, Object>> getAdById(Long userId, String slotId){
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(userId, slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
        if (CollectionUtils.isEmpty(newAdMappers)) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < newAdMappers.size(); i++) {
            NewAdMapper newAdMapper = newAdMappers.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_AD_ID, newAdMapper.getId());

            map.put(RES_RESULT_AD_CODE, newAdMapper.getCode());
            map.put(RES_RESULT_AD_NAME, newAdMapper.getName());
            map.put(RES_RESULT_AD_DESC, newAdMapper.getDescription());
            map.put(RES_RESULT_AD_CONTENT, newAdMapper.getContent());
            map.put(RES_RESULT_AD_BTN_CONTENT, newAdMapper.getBtnContent());
            if (StringUtils.isNoneBlank(newAdMapper.getGif())) {
                map.put(RES_RESULT_AD_GIF, combineCdbUrl(newAdMapper.getGif()));
            }

            map.put(RES_RESULT_AD_IMG, combineCdbUrl(newAdMapper.getImg()));

            String linkUrl = newAdMapper.getUrl();
            if (StringUtils.isNoneBlank(linkUrl) && linkUrl.toLowerCase().startsWith("http")) {
                String link = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), i, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L);
                map.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + link);
            } else {
                map.put(RES_RESULT_AD_URL, linkUrl);
            }

            mapList.add(map);

            if (Boolean.FALSE.equals(newAdMappers.get(i).getLogCollected())) {
                continue;
            }
            //曝光打点
            LogCollector.info("sys_new_ad_show_logs",
                    MiscUtils.map(
                            "user_id", userId,
                            "env", RuntimeMode.getCurrentStage(),
                            "version", getRequestString("version"),
                            "aid", newAdMapper.getId(),
                            "acode", newAdMapper.getCode(),
                            "index", i,
                            "slotId", slotId,
                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                            "time", DateUtils.dateToString(new Date()),
                            "agent", getRequest().getHeader("User-Agent"),
                            "uuid", UUID.randomUUID().toString(),
                            "system", getRequestString(REQ_SYS),
                            "system_version", getRequestString("sysVer")
                    ));
        }
        return mapList;
    }

    @RequestMapping(value = "incuserview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage incUserViewCount() {
        try {
            validateRequired(REQ_AD_ID, "广告ID");
            validateRequiredNumber(REQ_AD_VIEW_COUNT, "查看次数");
            validateRequest(REQ_AD_ID, REQ_AD_VIEW_COUNT);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        User requestUser = getApiRequestUser();
        Long aid = getRequestLong(REQ_AD_ID);
        Long viewCount = getRequestLong(REQ_AD_VIEW_COUNT);

        if (requestUser == null || aid == 0L || viewCount == 0L) {
            return failMessage("错误的参数");
        }

        AdvertisementDetail detail = advertisementLoaderClient.loadAdDetail(aid);
        if (detail == null) {
            return failMessage("错误的参数");
        }

        userAdvertisementServiceClient.incUserViewCount(requestUser.getId(), detail.getAdCode(), viewCount, detail.getShowTimeEnd());

        // 家长端闪屏特殊处理
        if (Objects.equals(AdConstants.PARENT_WELCOME_AD_SLOT_ID, detail.getAdSlotId())) {
            userAdvertisementServiceClient.incAdUserViewCount(requestUser.getId(), detail.getId());
        }

        return successMessage();
    }

}
