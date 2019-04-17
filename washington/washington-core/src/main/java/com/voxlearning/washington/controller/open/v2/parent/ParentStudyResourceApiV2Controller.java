package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author xinxin
 * @since 7/3/18
 */
@Controller
@RequestMapping(value = "/v2/parent/studyresource")
public class ParentStudyResourceApiV2Controller extends AbstractParentApiController {

    @RequestMapping(value = "/be.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ad() {
        User parent = getCurrentParent();
        if (null == parent) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "未登录");
        }

        try {

            List<Map<String, Object>> adList = new ArrayList<>();

            List<Map<String, Object>> ad3List = getAdList(parent.getId(), "221306");
            if (CollectionUtils.isNotEmpty(ad3List)) {
                adList.add(ad3List.get(0));
            }

            List<Map<String, Object>> ad2List = getAdList(parent.getId(), "221305");
            if (CollectionUtils.isNotEmpty(ad2List)) {
                adList.add(ad2List.get(0));
            }

            List<Map<String, Object>> ad1List = getAdList(parent.getId(), "221304");
            if (CollectionUtils.isNotEmpty(ad1List)) {
                adList.add(ad1List.get(0));
            }

            if (CollectionUtils.isNotEmpty(adList)) {
                List<Map<String, Object>> defaultAd = getAdList(parent.getId(), "221303");
                if (CollectionUtils.isNotEmpty(defaultAd)) {
                    adList.add(defaultAd.get(0));
                }
            }

            return successMessage().add("ads", adList);
        } catch (Exception ex) {
            logger.error("pid:{}", parent.getId(), ex);
            return failMessage("系统异常");
        }
    }

    private List<Map<String, Object>> getAdList(Long parentId, String adId) {
        List<Map<String, Object>> adList = new ArrayList<>();
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(parentId, adId, getClientVersion());
        if (CollectionUtils.isEmpty(newAdMappers)) {
            return adList;
        }

        for (int i = 0; i < newAdMappers.size(); i++) {
            NewAdMapper mapper = newAdMappers.get(i);
            if (Boolean.TRUE.equals(mapper.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", parentId,
                                "env", RuntimeMode.getCurrentStage(),
                                "version", getRequestString("version"),
                                "aid", mapper.getId(),
                                "acode", mapper.getCode(),
                                "index", i,
                                "slotId", adId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "uuid", UUID.randomUUID().toString(),
                                "system", getRequestString("sys"),
                                "system_version", getRequestString("sysVer")
                        ));
            }

            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_AD_IMG, combineCdbUrl(mapper.getImg()));
            String adLink = AdvertiseRedirectUtils.redirectUrl(mapper.getId(), 0, getClientVersion(), getRequestString(REQ_SYS), "", getRequestLong(REQ_STUDENT_ID));
            map.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + adLink);
            map.put(RES_TITLE, mapper.getName());
            map.put(RES_SUB_TITLE, mapper.getContent());
            map.put(RES_AD_ID, mapper.getId());
            adList.add(map);
        }
        return adList;
    }
}
