package com.voxlearning.washington.controller.mobile.student.headline;

import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 黑名单相关
 * @author chensn
 * @date 2018-12-04 12:07
 */
@ControllerMetric
@Controller
@RequestMapping(value = "/class/zone/black")
public class ClassZoneBlackController extends AbstractMobileController {


    /**
     * 获取黑名单信息
     */
    @RequestMapping(value = "loadBlackByType.vpage")
    @ResponseBody
    public MapMessage loadBlackByType(Integer type) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        Map<Integer, Boolean> resMap = new HashMap<>();
        resMap.put(type, grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(currentStudentDetail(), "ClassZoneBlackType" + type, "BlackList"));
        return MapMessage.successMessage().add("result", resMap);
    }


    /**
     * 获取班级年级信息
     */
    @RequestMapping(value = "loadClazzLevel.vpage")
    @ResponseBody
    public MapMessage loadClazzDetail() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        return MapMessage.successMessage().add("clazzLevel", clazz.getClazzLevel().getLevel()).add("userId", currentUserId());
    }
    /**
     * 获取当前学生所在班级
     */
    private Clazz getCurrentClazz() {
        return currentStudentDetail() == null ? null : currentStudentDetail().getClazz();
    }
}
