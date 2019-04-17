package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 上报我的自学进度接口
 *
 * @author jiangpeng
 * @since 2016-12-01 上午11:53
 **/
@Controller
@RequestMapping(value = "/v1/myselfstudy")
@Slf4j
public class MySelfStudyProgressApiController extends AbstractApiController {

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    /**
     * 更新我的自学 进度
     *
     * @return
     */
    @RequestMapping(value = "/studyprogress/notify.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage updateProgress() {
        try {
            validateRequired(REQ_PROGRESS, "进度");
            validateRequest(REQ_PROGRESS);
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }
        User user = getApiRequestUser();
        if (user == null || !user.isStudent())
            return failMessage("error user!");
        String appKey = getRequestString(REQ_APP_KEY);
        OrderProductServiceType orderProductServiceType = OrderProductServiceType.valueOf(appKey);
        if (orderProductServiceType == OrderProductServiceType.Unknown)
            return failMessage("error app");
        SelfStudyType selfStudyType = SelfStudyType.fromOrderType(orderProductServiceType);
        if (selfStudyType == SelfStudyType.UNKNOWN)
            return failMessage("error app");
        mySelfStudyService.updateSelfStudyProgress(user.getId(), selfStudyType, getRequestString(REQ_PROGRESS));
        return successMessage();
    }

    private MapMessage failedResult(String errMessage) {
        MapMessage resultMap = new MapMessage();
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, errMessage);
        return resultMap;
    }
}
