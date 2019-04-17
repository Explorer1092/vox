package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkReportLoaderClient;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/container/basicreview/report")
public class BasicReviewHomeworkReportController extends AbstractTeacherController {
    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;

    //班级一份作业信息 h5
    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchReportToClazz() {
        String packageId = getRequestString("packageId");
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isAnyBlank(homeworkId, packageId)) {
            logger.error("/teacher/basicreview/report/detail failed : packageId {},homeworkId {}", packageId, homeworkId);
            return MapMessage.errorMessage("参数作业PACKAGE_ID,HOMEWOKR_ID不存在");
        }
        try {
            MapMessage mapMessage = basicReviewHomeworkReportLoaderClient.fetchReportToClazz(packageId, homeworkId);
            if (mapMessage.isSuccess()) {
                mapMessage.put("teacherImageUrl", getUserAvatarImgUrl(SafeConverter.toString(mapMessage.get("teacherImageUrl"))));
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error("/teacher/basicreview/report/detail failed : packageId {},homeworkId {}", packageId, homeworkId, e);
            return MapMessage.errorMessage();
        }
    }

}
