package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 学生APP灰度控制
 * Created by alex on 2017/3/13.
 */
@Controller
@RequestMapping("/studentMobile/gray")
public class MobileStudentGrayController extends AbstractMobileController {

    @RequestMapping(value = "check.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkGrayConfig() {
        String mainName = getRequestString("mainName");
        String subName = getRequestString("subName");
        boolean schoolLevel = getRequestBool("schoolLevel", false);

        StudentDetail detail = currentStudentDetail();

        if (detail == null || StringUtils.isBlank(mainName) || StringUtils.isBlank(subName)) {
            return MapMessage.errorMessage("未知的请求");
        }

        return MapMessage.successMessage().add("gray",
                grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(detail, mainName, subName, schoolLevel));
    }

}
