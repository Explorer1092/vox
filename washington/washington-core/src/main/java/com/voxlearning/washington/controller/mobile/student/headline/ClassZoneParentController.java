package com.voxlearning.washington.controller.mobile.student.headline;

import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.zone.client.ClassCircleServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author chensn
 * @date 2018-12-24 10:57
 */
@ControllerMetric
@Controller
@RequestMapping(value = "/class/zone/parent")
public class ClassZoneParentController extends AbstractMobileController {
    @Inject
    private ClassCircleServiceClient classCircleServiceClient;
    /**
     * 订单确认
     */
    @RequestMapping(value = "/order/commit.vpage")
    @ResponseBody
    public MapMessage orderCommit(@RequestParam(defaultValue = "1", required = false) Integer type, @RequestParam(required = false) String subject) {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        return classCircleServiceClient.getClassCricleParentService().orderCommit(currentUserId(), type, subject);
    }
    /**
     * 订单查询
     */
    @RequestMapping(value = "/order/check.vpage")
    @ResponseBody
    public MapMessage orderCheck(@RequestParam(defaultValue = "1",required = false) Integer type) {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        StudentDetail studentDetail = fetchStudent();
        Integer clazzLevel;
        Long childUserId;
        if (studentDetail == null) {
            clazzLevel = 4;
            childUserId = null;
        } else {
            if (studentDetail.getClazz() == null || studentDetail.getClazz().getClazzLevel() == null) {
                clazzLevel = 4;
            } else {
                clazzLevel = studentDetail.getClazz().getClazzLevel().getLevel();
            }
            childUserId = studentDetail.getId();
        }
        MapMessage mapMessage = classCircleServiceClient.getClassCricleParentService().loadOrderRecord(currentUserId(), type);
        mapMessage.add("clazzLevel", clazzLevel);
        mapMessage.add("childUserId", childUserId);
        mapMessage.add("userId", currentUserId());
        return mapMessage;
    }

}
