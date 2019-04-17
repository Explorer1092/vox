package com.voxlearning.ucenter.controller.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Named;

/**
 * Created by zhouwei on 2018/9/17
 **/
@Named
@RequestMapping("/teacherTask/privilege")
public class TeacherTaskPrivilegeController extends AbstractWebController {

    /**
     * 获取用户的等级信息
     * @return
     */
    @RequestMapping(value = "/getTeacherLevel.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getTeacherLevel() {
        try {
            TeacherDetail td = currentTeacherDetail();
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());
            Integer level = 1;
            if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
                level = teacherExtAttribute.getNewLevel();
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (levelEnum == null) {
                return MapMessage.errorMessage("老师特权等级错误");
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.set("level_id", levelEnum.getLevel());
            mapMessage.set("level_name", levelEnum.getValue());
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

}
