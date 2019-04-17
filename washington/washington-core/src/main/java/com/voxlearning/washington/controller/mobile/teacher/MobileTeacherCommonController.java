package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/teacherMobile/common/")
public class MobileTeacherCommonController extends AbstractMobileTeacherController {


    @ResponseBody
    @RequestMapping(value = "upload_base64.vpage", method = RequestMethod.POST)
    public MapMessage uploadBase64Img(@RequestBody String base64v) {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return MapMessage.errorMessage();
        }

        try {
            if (StringUtils.isBlank(base64v)) {
                return MapMessage.errorMessage();
            }

            Map<String, Object> stringObjectMap = JsonUtils.fromJson(base64v);
            String base64 = MapUtils.getString(stringObjectMap, "base64");
            if (StringUtils.isBlank(base64)) {
                return MapMessage.errorMessage();
            }
            String url = OSSManageUtils.uploadBase64Image(base64, "new_term_plan", "png");
            if (StringUtils.isNotBlank(url)) {
                return MapMessage.successMessage().add("url", url);
            }
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return MapMessage.errorMessage();
    }
}
