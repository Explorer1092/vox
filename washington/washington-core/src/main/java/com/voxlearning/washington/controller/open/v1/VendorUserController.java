package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.GansuTelecomService;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "v1/vendor/user")
@Slf4j
public class VendorUserController extends AbstractApiController {

    //入参手机号个数最大限制
    private final static int MOBILE_NUMS = 10;

    @ImportService(interfaceClass = GansuTelecomService.class)
    private GansuTelecomService gansuTelecomService;

    /**
     * 给三方平台开通服务
     *
     * @return
     */
    @RequestMapping(value = "/open.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage openService() {
        MapMessage resultMap = new MapMessage();
        List<String> mobileList;
        try {
            mobileList = validateRequestParams();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Map<String, String> openResult = gansuTelecomService.openService(getRequestString(REQ_APP_KEY), mobileList);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_RESULT_DATA, openResult);

        return resultMap;
    }

    /**
     * 退订服务
     *
     * @return
     */
    @RequestMapping(value = "/close.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage closeService() {
        MapMessage resultMap = new MapMessage();
        List<String> mobileList;
        try {
            mobileList = validateRequestParams();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Map<String, String> openResult =gansuTelecomService.closeService(getRequestString(REQ_APP_KEY),mobileList);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_RESULT_DATA, openResult);

        return resultMap;
    }

    /**
     * 返回有效用户数
     *
     * @return
     */
    @RequestMapping(value = "/effective.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage effectiveUserNum() {
        MapMessage resultMap = new MapMessage();

        String appKey = getRequestString(REQ_APP_KEY);
        String date = getRequestString(REQ_VENDOR_DATE);
        try {
            validateRequired(REQ_VENDOR_DATE, "查询年月");

            Date checkDate = DateUtils.stringToDate(date, "yyyy-MM");
            if (checkDate==null) {
                throw new IllegalArgumentException("查询年月格式错误");
            }

            validateRequestNoSessionKey(REQ_VENDOR_DATE);

        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long openResult =gansuTelecomService.loadEffectiveUser(appKey, date);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_RESULT_DATA, openResult);

        return resultMap;

    }

    private List<String> validateRequestParams() {

        validateRequired(REQ_MOBILE, "电话号码");

        String mobiles = getRequestString(REQ_MOBILE);

        String[] mobileArr = mobiles.split(",");
        if (mobileArr.length > MOBILE_NUMS) {
            throw new IllegalArgumentException(VALIDATE_ERROR_MOBILE_EXCEED_LIMIT_MSG);
        }

        List<String> mobileList = Arrays.asList(mobileArr);
        mobileList.stream().forEach(mobile -> {
            validateMobileNumber(mobile);
        });

        validateRequestNoSessionKey(REQ_MOBILE);

        return mobileList;
    }
}
