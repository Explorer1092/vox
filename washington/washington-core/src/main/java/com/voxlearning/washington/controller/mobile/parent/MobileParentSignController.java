package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.AppParentSignRecord;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_BAD_REQUEST_CODE;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_WRONG_STUDENT_USER_ID_MSG;

/**
 * @author malong
 * @since 2016/5/23
 */
@Controller
@RequestMapping(value = "/parentMobile/parent")
@Slf4j
public class MobileParentSignController extends AbstractMobileParentController {
    //父母签到
    @RequestMapping(value = "/sign.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentSign(){
        User parent = currentParent();
        try {
            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            Long parentId = parent.getId();

            AppParentSignRecord appParentSignRecord = vendorLoaderClient.loadAppParentRecordByUserId(parentId);
            if (appParentSignRecord == null) {
                return vendorServiceClient.updateOrInsertAppParentSignRecord(parentId);
            } else {
                if (appParentSignRecord.hasSigned(MonthRange.current())) {
                    return MapMessage.errorMessage("您本月已经签到过了，不能重复签到");
                }
                return vendorServiceClient.updateOrInsertAppParentSignRecord(parentId);
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("签到失败");
        }
    }

    @RequestMapping(value = "/getParentSignInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentSignInfo() {
        Long studentId = getRequestLong(ApiConstants.REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        List<StudentParentRef> studentParentRefList = studentLoaderClient.loadStudentParentRefs(studentId);
        Map<Long, StudentParentRef> studentParentRefMap = new HashMap<>();
        studentParentRefList.stream().forEach(e -> studentParentRefMap.put(e.getParentId(), e));
        Set<Long> parentIds = studentParentRefMap.keySet();
        List<AppParentSignRecord> parentSignRecordList = vendorLoaderClient.loadAppParentSignRecordByUserIds(parentIds);
        Map<String, AppParentSignRecord> parentSignRecordMap = parentSignRecordList.stream().collect(Collectors.toMap(AppParentSignRecord::getId, Function.identity()));

        String studentName = studentDetail.fetchRealname();
        MonthRange currentMonth = MonthRange.current();
        MonthRange lastMonth = MonthRange.current().previous();

        //两个map分别记录上个月和这个月的签到信息
        Map<String, Object> lastMap = new HashMap<>();
        Map<String, Object> currentMap = new HashMap<>();
        lastMap.put("month", lastMonth.getMonth());
        currentMap.put("month", currentMonth.getMonth());

        List<Map<String, Object>> lastList = new ArrayList<>();
        List<Map<String, Object>> currentList = new ArrayList<>();
        parentIds.stream().forEach(e -> {
            Map<String, Object> lastInfoMap = new HashMap<>();
            Map<String, Object> currentInfoMap = new HashMap<>();
            String name = studentName + studentParentRefMap.get(e).getCallName();
            String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(e);
            boolean lastMonthSign = parentSignRecordMap.get(e.toString()) != null && parentSignRecordMap.get(e.toString()).hasSigned(lastMonth);
            boolean currentMonthSign = parentSignRecordMap.get(e.toString()) != null && parentSignRecordMap.get(e.toString()).hasSigned(currentMonth);

            lastInfoMap.put("name", name);
            lastInfoMap.put("mobile", mobile);
            lastInfoMap.put("is_sign", lastMonthSign);
            lastList.add(lastInfoMap);

            currentInfoMap.put("name", name);
            currentInfoMap.put("mobile", mobile);
            currentInfoMap.put("is_sign", currentMonthSign);
            currentList.add(currentInfoMap);

        });
        lastMap.put("info", lastList);
        currentMap.put("info", currentList);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(lastMap);
        list.add(currentMap);

        return MapMessage.successMessage().add("list", list);
    }

}
