package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import com.voxlearning.utopia.service.action.api.document.UserGrowthRewardLog;
import com.voxlearning.utopia.service.action.api.support.UserGrowthReward;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_FEATURE_LIST;

/**
 * 个人中心相关
 * Created by Shuai Huan on 2015/11/5.
 */
@Controller
@RequestMapping(value = "/v1/student/center")
@Slf4j
public class StudentCenterApiController extends AbstractStudentApiController {

    @Inject private ActionLoaderClient actionLoaderClient;

    @RequestMapping(value = "/featurelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage featureList() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        List<Map<String, Object>> resultList = new LinkedList<>();

        String config = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "native_center_feature_list");
        config = config.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> configMap = JsonUtils.fromJson(config);
        if (MapUtils.isNotEmpty(configMap)) {
            StudentDetail studentDetail = getCurrentStudentDetail();
            String key = "feature_list";
            // 这里初高中先走同一个配置，以后有需求再加
            if (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
                key = "junior_feature_list";
            }
            List<Map> featureList = (List) configMap.get(key);
            if (CollectionUtils.isNotEmpty(featureList)) {
                String currentVer = getRequestString(REQ_APP_NATIVE_VERSION);
                featureList.stream().forEach(e -> {
                    String fromVer = ConversionUtils.toString(e.get("fromVer"));
                    String endVer = ConversionUtils.toString(e.get("endVer"));

                    if (("".equals(fromVer) || VersionUtil.compareVersion(currentVer, fromVer) >= 0)
                            && ("".equals(endVer) || VersionUtil.compareVersion(currentVer, endVer) < 0)) {

                        Map<String, Object> map = new HashMap<>();
                        map.put(RES_NAME, e.get("name"));
                        map.put(RES_LINK, e.get("link"));
                        map.put(RES_ICON, e.get("icon"));
                        resultList.add(map);
                    }
                });
            }
        }

        resultMap.add(RES_FEATURE_LIST, resultList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    /**
     * 个人中心升级奖励红点提示
     */
    @RequestMapping(value = "/remind.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage remind() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(currentUserId());
        if (null == userGrowth) {
            resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        //当前等级可领的奖励,包含已领、未领(都是已经获得的奖励)
        List<Integer> canRewardLevels = UserGrowthReward.getLevelsCanReceive(userGrowth.toLevel());
        if (CollectionUtils.isEmpty(canRewardLevels)) {
            resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        Map<String, Object> rewardMap = new HashMap<>();
        rewardMap.put(RES_UCENTER_GROWTH_UP_TYPE, 0);//0红点　1红点+数字
        //用户已经领取的奖励
        List<UserGrowthRewardLog> logs = actionLoaderClient.getRemoteReference().getUserGrowthLevelRewards(currentUserId());
        if (CollectionUtils.isEmpty(logs)) {
            rewardMap.put(RES_UCENTER_GROWTH_UP_COUNT, canRewardLevels.size());
            resultMap.put(RES_UCENTER_GROWTH_UP_REMIND, rewardMap);
        } else if (canRewardLevels.size() > logs.size()) {
            rewardMap.put(RES_UCENTER_GROWTH_UP_COUNT, canRewardLevels.size() - logs.size());
            resultMap.put(RES_UCENTER_GROWTH_UP_REMIND, rewardMap);
        }

        resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}
